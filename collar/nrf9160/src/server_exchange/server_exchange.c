#include <zephyr/logging/log.h>

#include <zephyr/net/coap.h>
#include <zephyr/net/socket.h>
#include <nrfc_dtls.h>
#include <zephyr/net/tls_credentials.h>

#include <zephyr/random/random.h>
#include <zcbor_common.h>
#include <zcbor_encode.h>
#include <zcbor_decode.h>

#include "server_exchange.h"

LOG_MODULE_REGISTER(server_exchange);

#define APP_COAP_VERSION 1

static int sock;
static struct pollfd fds;
static struct sockaddr_storage server;

static uint16_t token;
static uint8_t coap_send_buf[1024];
static uint8_t coap_recv_buf[1024];

/* Function for listen for responses thread */
void listen_for_responses_thread(void);
K_THREAD_STACK_DEFINE(listen_for_responses_thread_stack_area, CONFIG_RESPONSE_THREAD_STACK_SIZE);
struct k_thread listen_for_responses_thread_id;

/* @brief Resolve the server's hostname
 * @return 0 on succes, <0 otherwise
 */
static int server_resolve(void);

int server_exchange_init()
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

    // Randomize token.
    token = sys_rand32_get();

    // init fds
    fds.fd = sock;
    fds.events = POLLIN;

    // Start the listen for responses thread
    k_tid_t my_tid = k_thread_create(&listen_for_responses_thread_id, listen_for_responses_thread_stack_area,
                                     K_THREAD_STACK_SIZEOF(listen_for_responses_thread_stack_area),
                                     listen_for_responses_thread,
                                     NULL, NULL, NULL,
                                     CONFIG_RESPONSE_THREAD_PRIORITY, 0, K_NO_WAIT);

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

    // printk("CoAP response: code: 0x%x, token 0x%02x%02x, payload: %s\n",
    //        coap_header_get_code(&response), token[1], token[0], temp_buf);

    

    return 0;
}

void listen_for_responses_thread(void)
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

        err = client_handle_response(coap_recv_buf, bytes);
        if (err < 0)
        {
            LOG_ERR("Could not handle the response");
            break;
        }
    }
}

/* ---- Requests ---- */

int send_gps_data(void)
{
    int err;

    struct coap_packet request;
    uint8_t payload[128] = {0};

    ZCBOR_STATE_E(encoding_state, 0, payload, sizeof(payload), 0);
    err = zcbor_map_start_encode(encoding_state, 0) &&
          zcbor_tstr_put_lit(encoding_state, "lon") &&
          zcbor_float64_put(encoding_state, 2.2) &&
          zcbor_tstr_put_lit(encoding_state, "lat") &&
          zcbor_float64_put(encoding_state, 2.3) &&
          zcbor_tstr_put_lit(encoding_state, "acr") &&
          zcbor_float32_put(encoding_state, 2.4) &&
          zcbor_map_end_encode(encoding_state, 0);
    if (!err)
    {
        err = zcbor_peek_error(encoding_state);
        LOG_ERR("Encoding failed: %d", err);
        return -err;
    }

    // As the pointer in encoding state is incrementing a it goes, we can find the size of the final encoded content as follows
    int payload_len = encoding_state->payload - payload;
    // If this will fail in future releases of the zcbor library, try with strlen and send the size of buffer - 1, or check for a function

    token++;

    err = coap_packet_init(&request, coap_send_buf, sizeof(coap_send_buf),
                           APP_COAP_VERSION, COAP_TYPE_NON_CON,
                           sizeof(token), (uint8_t *)&token,
                           COAP_METHOD_POST, coap_next_id());
    if (err < 0)
    {
        LOG_ERR("Failed to create CoAP request, %d", err);
        return err;
    }

    err = coap_packet_append_option(&request, COAP_OPTION_URI_PATH,
                                    (uint8_t *)"loc", 3);
    if (err < 0)
    {
        LOG_ERR("Failed to encode CoAP path option, %d", err);
        return err;
    }

    err = coap_packet_append_option(&request, COAP_OPTION_CONTENT_FORMAT,
					(uint8_t *)COAP_CONTENT_FORMAT_APP_CBOR,
					strlen(COAP_CONTENT_FORMAT_APP_CBOR));
	if (err < 0) {
		printk("Failed to encode CoAP option, %d\n", err);
		return err;
	}

    err = coap_packet_append_payload_marker(&request);
    if (err < 0)
    {
        LOG_ERR("Failed to append the payload marker, %d", err);
        return err;
    }

    err = coap_packet_append_payload(&request, (uint8_t *)payload,
                                     payload_len);
    if (err)
    {
        LOG_ERR("Failed to add the payload, %d", err);
        return err;
    }

    err = send(sock, request.data, request.offset, 0);
    if (err < 0)
    {
        LOG_ERR("Failed to send CoAP request, %d, %d", errno, err);
        return -errno;
    }

    LOG_INF("CoAP request sent to /loc resource: token 0x%04x, bytes sent: %d", token, err);

    return 0;
}

int send_observe_led()
{
	int err;

	struct coap_packet request;
    token++;

	err = coap_packet_init(&request, coap_send_buf, sizeof(coap_send_buf),
			       APP_COAP_VERSION, COAP_TYPE_NON_CON,
			       sizeof(next_token), (uint8_t *)&next_token,
			       COAP_METHOD_GET, coap_next_id());
	if (err < 0) {
		printk("Failed to create CoAP request, %d\n", err);
		return err;
	}

err = coap_packet_append_option(&request, COAP_OPTION_URI_PATH,
					(uint8_t *)CONFIG_COAP_RESOURCE,
					strlen(CONFIG_COAP_RESOURCE));
	if (err < 0) {
		printk("Failed to encode CoAP option, %d\n", err);
		return err;
	}

err = coap_packet_append_option(&request, COAP_OPTION_OBSERVE,
					(uint8_t *)CONFIG_COAP_RESOURCE,
					strlen(CONFIG_COAP_RESOURCE));
	if (err < 0) {
		printk("Failed to encode CoAP option, %d\n", err);
		return err;
	}


}
