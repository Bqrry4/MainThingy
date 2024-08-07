#include <zephyr/logging/log.h>

#include <zephyr/net/coap.h>
#include <zephyr/net/socket.h>
#include <nrfc_dtls.h>
#include <zephyr/net/tls_credentials.h>

#include <zephyr/random/random.h>
#include <zcbor_common.h>
#include <zcbor_encode.h>
#include <zcbor_decode.h>
#include <zephyr/net/coap_client.h>

// events for polling
#include "led_state_event.h"
#include "buzzer_state_event.h"
#include "gnns_mode_state_event.h"

#include "server_exchange.h"

LOG_MODULE_REGISTER(server_exchange);

bool isInitialized = false;

int sock;
static struct pollfd fds;
struct sockaddr_storage server;

uint8_t coap_send_buf[1024];
uint8_t coap_recv_buf[1024];
#define KEEP_ALIVE_INTERVAL K_SECONDS(10)

/* Function for listen for responses thread */
void listen_for_responses_thread(void *, void *, void *);
K_THREAD_STACK_DEFINE(listen_for_responses_thread_stack_area, CONFIG_RESPONSE_THREAD_STACK_SIZE);
struct k_thread listen_for_responses_thread_id;

// static void keep_alive_timer_handler(struct k_timer *timer);
// void keep_alive_work_handler(struct k_work *work);
// static struct k_timer keep_alive_timer;
// static struct k_work keep_alive_work;

#define POLLING_INTERVAL K_SECONDS(300) // Polling interval
static struct k_timer polling_state_timer;
static struct k_work polling_work;
void polling_timer_fn(struct k_timer *timer_id);
void poll_requests(struct k_work *work);
struct coap_client coap_client = {0};

/* @brief Resolve the server's hostname
 * @return 0 on succes, <0 otherwise
 */
static int server_resolve(void);

// forward declarations
int send_observe_led();
int send_observe_buz();
int send_observe_gnssm();
int send_secret_change();
void start_polling();
void stop_polling();

int server_connect()
{
    int err;

    err = server_resolve();
    if (err)
    {
        LOG_ERR("Failed to resolve the server's hostname : %d\n", err);
        return err;
    }

// Creating a socket for connection with main server
#if defined(CONFIG_COAP_OVER_DTLS)
    LOG_DBG("DTLS enabled");

    sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_DTLS_1_2);
    LOG_DBG("sock = %d", sock);
    if (sock < 0)
    {
        LOG_ERR("Failed to create CoAP socket: %d.", -errno);
        return -errno;
    }

    // Setting as in nrfc_dtlc.c, but with relevant parameters
    LOG_DBG("Setting socket options:");

    LOG_DBG("  hostname: %s", CONFIG_SERVER_HOSTNAME);
    err = setsockopt(sock, SOL_TLS, TLS_HOSTNAME, CONFIG_SERVER_HOSTNAME,
                     sizeof(CONFIG_SERVER_HOSTNAME));
    if (err)
    {
        LOG_ERR("Error setting hostname: %d", -errno);
        return -errno;
    }

    sec_tag_t sec_tag = CONFIG_SOCK_SEC_TAG;
    LOG_DBG("  sectag: %d", sec_tag);
    err = setsockopt(sock, SOL_TLS, TLS_SEC_TAG_LIST, &sec_tag, sizeof(sec_tag));
    if (err)
    {
        LOG_ERR("Failed to setup socket security tag: %d", -errno);
        return -errno;
    }

    int cid_option = TLS_DTLS_CID_SUPPORTED;

    LOG_DBG("  Enable connection id");
    err = setsockopt(sock, SOL_TLS, TLS_DTLS_CID, &cid_option, sizeof(cid_option));
    if (!err)
    {
        LOG_INF("Connection ID enabled");
    }
    else if ((err != EOPNOTSUPP) && (err != EINVAL))
    {
        LOG_ERR("Error enabling connection ID: %d", -errno);
    }
    else
    {
        LOG_INF("Connection ID not supported by the provided socket");
    }

    int timeout = TLS_DTLS_HANDSHAKE_TIMEO_123S;

    LOG_DBG("  Set handshake timeout %d", timeout);
    err = setsockopt(sock, SOL_TLS, TLS_DTLS_HANDSHAKE_TIMEO,
                     &timeout, sizeof(timeout));
    if (!err)
    {
    }
    else if ((err != EOPNOTSUPP) || (err != EINVAL))
    {
        LOG_ERR("Error setting handshake timeout: %d", -errno);
    }

    int verify = TLS_PEER_VERIFY_REQUIRED;

    LOG_DBG("  Peer verify: %d", verify);
    err = setsockopt(sock, SOL_TLS, TLS_PEER_VERIFY, &verify, sizeof(verify));
    if (err)
    {
        LOG_ERR("Failed to setup peer verification, errno %d", -errno);
        return -errno;
    }

    int session_cache = TLS_SESSION_CACHE_ENABLED;

    err = setsockopt(sock, SOL_TLS, TLS_SESSION_CACHE, &session_cache, sizeof(session_cache));
    if (err)
    {
        LOG_ERR("Failed to enable session cache, errno: %d", -errno);
        return -errno;
    }

#else
    sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_UDP);
    // sock = socket(AF_INET, SOCK_STREAM, IPPROTO_TCP);
    if (sock < 0)
    {
        LOG_ERR("Failed to create socket: %d.\n", errno);
        return -errno;
    }
#endif /*CONFIG_COAP_OVER_DTLS*/

    err = connect(sock, (struct sockaddr *)&server,
                  sizeof(struct sockaddr_in));
    if (err < 0)
    {
        LOG_ERR("Connect failed : %d\n", errno);
        return -errno;
    }

    return 0;
}

int server_exchange_init()
{
    int err;

    err = server_connect();
    if (err)
    {
        LOG_ERR("Failed to connect to server: %d\n", err);
        return err;
    }


    // // init fds
    // fds.fd = sock;
    // fds.events = POLLIN;

    // // Start the listen for responses thread
    // k_tid_t my_tid = k_thread_create(&listen_for_responses_thread_id, listen_for_responses_thread_stack_area,
    //                                  K_THREAD_STACK_SIZEOF(listen_for_responses_thread_stack_area),
    //                                  listen_for_responses_thread,
    //                                  NULL, NULL, NULL,
    //                                  CONFIG_RESPONSE_THREAD_PRIORITY, 0, K_NO_WAIT);

    // k_timer_init(&keep_alive_timer, keep_alive_timer_handler, NULL);
    // k_timer_start(&keep_alive_timer, KEEP_ALIVE_INTERVAL, KEEP_ALIVE_INTERVAL);

    // // Initialize work for keep-alive
    // k_work_init(&keep_alive_work, keep_alive_work_handler);

    err = coap_client_init(&coap_client, NULL);
    if (err)
    {
        LOG_ERR("Failed to initialize CoAP client: %d", err);
        return err;
    }

    isInitialized = true;

    k_timer_init(&polling_state_timer, polling_timer_fn, NULL);
    k_work_init(&polling_work, poll_requests);

    start_polling();

    // send_secret_change();
    return 0;
}

static int server_resolve(void)
{
    int err;
    struct addrinfo *result;
    struct addrinfo hints = {
        .ai_family = AF_INET,
        .ai_socktype = SOCK_DGRAM};
    char ipv4_addr[NET_IPV4_ADDR_LEN];

    err = getaddrinfo(CONFIG_SERVER_HOSTNAME, NULL, &hints, &result);
    if (err)
    {
        LOG_ERR("ERROR: getaddrinfo failed %d\n", err);
        return err;
    }

    /* IPv4 Address. */
    struct sockaddr_in *server4 = ((struct sockaddr_in *)&server);

    server4->sin_addr.s_addr =
        ((struct sockaddr_in *)result->ai_addr)->sin_addr.s_addr;
    server4->sin_family = AF_INET;
    server4->sin_port = htons(CONFIG_SERVER_PORT);

    // Convert network address from internal to numeric ASCII form
    err = inet_ntop(AF_INET, &server4->sin_addr.s_addr, ipv4_addr,
                    sizeof(ipv4_addr));
    if (err == NULL)
    {
        LOG_ERR("Failed to get the string representation of address \
        from it internal representation: %d.\n",
                errno);
        return -errno;
    }

    LOG_INF("IPv4 Address found %s\n", ipv4_addr);

    /* Free the address. */
    freeaddrinfo(result);

    return 0;
}

/*  @brief Wait infinitly till the fd has data
 *  @retval 0 if data is available.
 *  @retval -EIO Error condition on poll
 *  @retval -EBADF fd probably not open
 *  @retval -EAGAIN Could not check for data
 */
static int wait()
{
    // Poll with infinite timeout
    int err = poll(&fds, 1, -1);

    if (err < 0)
    {
        LOG_ERR("Poll error: %d", errno);
        return -errno;
    }

    if (fds.revents & POLLERR)
    {
        // Error condition on poll
        return -EIO;
    }

    if (fds.revents & POLLNVAL)
    {
        // fd probably not open
        return -EBADF;
    }

    if (!(fds.revents & POLLIN))
    {
        // No data in descriptor
        return -EAGAIN;
    }

    return 0;
}

/**@brief Handles responses
 * @param buf data buffer
 * @param len buffer length
 */
static int client_handle_response(uint8_t *buf, int len)
{
    int err;

    struct coap_packet response;
    const uint8_t *payload;
    uint16_t payload_len;
    uint8_t res_token[8];
    uint16_t res_token_len;
    uint8_t temp_buf[16];

    err = coap_packet_parse(&response, buf, len, NULL, 0);
    if (err)
    {
        LOG_ERR("Malformed response, %d", err);
        return err;
    }

    payload = coap_packet_get_payload(&response, &payload_len);
    res_token_len = coap_header_get_token(&response, res_token);

    // if ((res_token_len != sizeof(token)) ||
    //     (memcmp(&token, token, sizeof(res_token)) != 0))
    // {
    //     printk("Invalid token received: 0x%02x%02x\n",
    //            token[1], token[0]);
    //     return 0;
    // }

    // if (payload_len > 0)
    // {
    //     snprintf(temp_buf, MIN(payload_len, sizeof(temp_buf)), "%s", payload);
    // }
    // else
    // {
    //     strcpy(temp_buf, "EMPTY");
    // }

    LOG_INF("CoAP response: code: 0x%x, token 0x%02x%02x, payload: %s\n",
            coap_header_get_code(&response), res_token[1], res_token[0], temp_buf);

    return 0;
}

void listen_for_responses_thread(void *, void *, void *)
{
    int err;
    int bytes;

    while (true)
    {

        // Wait for data in socket
        err = wait();
        if (err)
        {
            if (err == -EAGAIN)
            {
                LOG_DBG("Could not check for data, %d", err);
                continue;
            }

            LOG_ERR("Error on poll, %d", err);
            break;
        }

        bytes = recv(sock, coap_recv_buf, sizeof(coap_recv_buf), MSG_DONTWAIT);
        if (bytes < 0)
        {
            if (errno == EAGAIN || errno == EWOULDBLOCK)
            {
                LOG_ERR("No data is waiting to be received, %d", -errno);
                continue;
            }
            else
            {
                LOG_ERR("Recv error, %d", -errno);
                break;
            }
        }

        if (bytes == 0)
        {
            LOG_DBG("Received an empty datagram");
            continue;
        }
        else
        {
            LOG_INF("Received packet %d", bytes);
        }

        err = client_handle_response(coap_recv_buf, bytes);
        if (err < 0)
        {
            LOG_ERR("Could not handle the response");
            break;
        }
    }
}

/* ---- Requests ---- */

// void keep_alive_work_handler(struct k_work *work)
// {

//     LOG_INF("KEEP ALIVE");
//     int err;

//     struct coap_packet request;
//     uint8_t coap_send_buf[1024];

//     coap_packet_init(&request, coap_send_buf, sizeof(coap_send_buf), COAP_VERSION_1,
//                      COAP_TYPE_CON, 0, NULL, COAP_METHOD_GET, coap_next_id());

//     err = send(sock, request.data, request.offset, 0);
//     if (err < 0) {
//         LOG_ERR("Failed to send keep-alive ping: %d", errno);
//     } else {
//         LOG_INF("Keep-alive ping sent");
//     }
// }

// static void keep_alive_timer_handler(struct k_timer *timer_id)
// {
//     k_work_submit(&keep_alive_work);
// }

void polling_timer_fn(struct k_timer *timer_id)
{
    k_work_submit(&polling_work);
}

void poll_requests(struct k_work *work)
{
    send_observe_gnssm();
    send_observe_led();
    send_observe_buz();
}

void start_polling()
{
    k_timer_start(&polling_state_timer, K_NO_WAIT, POLLING_INTERVAL);
}
void stop_polling()
{
    k_timer_stop(&polling_state_timer);
}

int decode_state(const uint8_t *payload, size_t len, bool *state)
{
    /* Create zcbor state variable for decoding. */
    ZCBOR_STATE_D(decoding_state, 1, payload, len, 1, 0);

    struct zcbor_string decoded_string;

    bool success = zcbor_map_start_decode(decoding_state) &&
                   zcbor_tstr_decode(decoding_state, &decoded_string) &&
                   zcbor_bool_decode(decoding_state, state) &&
                   zcbor_map_end_decode(decoding_state);
    if (!success)
    {
        LOG_ERR("Decoding failed: %d\r\n", zcbor_peek_error(decoding_state));
        return -1;
    }

    return 0;
}

bool led_state = false;
static void led_response_cb(int16_t code, size_t offset, const uint8_t *payload,
                            size_t len, bool last_block, void *user_data)
{
    if (code >= 0)
    {
        LOG_INF("CoAP response: code: 0x%x %d", code, len);

        // Content
        if (code == 0x45)
        {
            bool state;
            if (decode_state(payload, len, &state))
            {
                return;
            }

            // sumbit when state on server changed
            if (state != led_state)
            {
                // submit update event
                struct led_state_event *led_event = new_led_state_event();
                led_event->led_state = state;
                APP_EVENT_SUBMIT(led_event);

                led_state = state;
            }
        }
    }
    else
    {
        LOG_INF("Response received with error code for led polling: %d", code);
    }
}

int send_observe_led()
{
    int err;

    struct coap_client_request req = {
        .method = COAP_METHOD_GET,
        .confirmable = true,
        .fmt = COAP_CONTENT_FORMAT_TEXT_PLAIN,
        .payload = NULL,
        .cb = led_response_cb,
        .len = 0,
        .path = "devs/FF:EE:DD:CC:BB:AA/led",
        .options = NULL,
        .num_options = 0,
        .user_data = NULL};

    /* Send request */
    err = coap_client_req(&coap_client, sock, (struct sockaddr *)&server, &req, NULL);
    if (err)
    {
        LOG_ERR("Failed to send request: %d", err);
        return err;
    }

    LOG_INF("LED SEND");

    return 0;
}

bool buzz_state = false;
static void buzz_response_cb(int16_t code, size_t offset, const uint8_t *payload,
                             size_t len, bool last_block, void *user_data)
{
    if (code >= 0)
    {
        LOG_INF("CoAP response: code: 0x%x %d", code, len);

        // Content
        if (code == 0x45)
        {
            bool state;
            if (decode_state(payload, len, &state))
            {
                return;
            }

            // sumbit when state on server changed
            if (state != buzz_state)
            {
                // submit update event
                struct buzzer_state_event *buz_event = new_buzzer_state_event();
                buz_event->buzzer_state = state;
                APP_EVENT_SUBMIT(buz_event);

                buzz_state = state;
            }
        }
    }
    else
    {
        LOG_INF("Response received with error code for buz polling: %x", code);
    }
}

int send_observe_buz()
{
    int err;

    struct coap_client_request req = {
        .method = COAP_METHOD_GET,
        .confirmable = true,
        .fmt = COAP_CONTENT_FORMAT_TEXT_PLAIN,
        .payload = NULL,
        .cb = buzz_response_cb,
        .len = 0,
        .path = "devs/FF:EE:DD:CC:BB:AA/buz",
        .options = NULL,
        .num_options = 0,
        .user_data = NULL};

    /* Send request */
    err = coap_client_req(&coap_client, sock, (struct sockaddr *)&server, &req, NULL);
    if (err)
    {
        LOG_ERR("Failed to send request: %d", err);
        return err;
    }

    LOG_INF("BUZZ SEND");
    return 0;
}

bool gnss_state = false;
static void gnssm_response_cb(int16_t code, size_t offset, const uint8_t *payload,
                              size_t len, bool last_block, void *user_data)
{
    if (code >= 0)
    {
        LOG_INF("CoAP response: code: 0x%x %d", code, len);

        // Content
        if (code == 0x45)
        {
            bool state;
            if (decode_state(payload, len, &state))
            {
                return;
            }

            // sumbit when state on server changed
            if (state != gnss_state)
            {
                // submit update event
                struct gnns_mode_state_event *event = new_gnns_mode_state_event();
                event->state = state;
                APP_EVENT_SUBMIT(event);

                gnss_state = state;
            }
        }
    }
    else
    {
        LOG_INF("Response received with error code for gnssm polling: %x", code);
    }
}

int send_observe_gnssm()
{
    int err;

    struct coap_client_request req = {
        .method = COAP_METHOD_GET,
        .confirmable = true,
        .fmt = COAP_CONTENT_FORMAT_TEXT_PLAIN,
        .payload = NULL,
        .cb = gnssm_response_cb,
        .len = 0,
        .path = "devs/FF:EE:DD:CC:BB:AA/gnssm",
        .options = NULL,
        .num_options = 0,
        .user_data = NULL};

    /* Send request */
    err = coap_client_req(&coap_client, sock, (struct sockaddr *)&server, &req, NULL);
    if (err)
    {
        LOG_ERR("Failed to send request: %d", err);
        return err;
    }

    LOG_INF("GNSSM SEND");
    return 0;
}

static void secret_response_cb(int16_t code, size_t offset, const uint8_t *payload,
                               size_t len, bool last_block, void *user_data)
{
    if (code >= 0)
    {
        LOG_INF("CoAP response: code: 0x%x %d", code, len);

        // Changed
        if (code == 0x44)
        {
            // send event
        }
    }
    else
    {
        LOG_INF("Response received with error code for secret send: %x", code);
    }
}

int send_secret_change()
{
    int err;

    uint8_t payload[64] = {0};

    char *secret = coap_next_token();
    // LOG_INF("token 0x%04x", secret);
    for (size_t i = 0; i < 8; i++)
    {
        LOG_INF("%02x", secret[i]);
    }

    ZCBOR_STATE_E(encoding_state, 0, payload, sizeof(payload), 0);
    err = zcbor_map_start_encode(encoding_state, 0) &&
          zcbor_tstr_put_lit(encoding_state, "sec") &&
          zcbor_bstr_encode_ptr(encoding_state, secret, 8) &&
          zcbor_map_end_encode(encoding_state, 0);
    if (!err)
    {
        err = zcbor_peek_error(encoding_state);
        LOG_ERR("Encoding failed: %d", err);
        return -err;
    }
    int payload_len = encoding_state->payload - payload;

    struct coap_client_request req = {
        .method = COAP_METHOD_PUT,
        .confirmable = true,
        .fmt = COAP_CONTENT_FORMAT_TEXT_PLAIN,
        .payload = payload,
        .cb = secret_response_cb,
        .len = payload_len,
        .path = "devs/FF:EE:DD:CC:BB:AA",
        .options = NULL,
        .num_options = 0,
        .user_data = NULL};

    /* Send request */
    err = coap_client_req(&coap_client, sock, (struct sockaddr *)&server, &req, NULL);
    if (err)
    {
        LOG_ERR("Failed to send request: %d", err);
        return err;
    }

    LOG_INF("SECRET SEND");
    return 0;
}