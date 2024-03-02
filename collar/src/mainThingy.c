#include <zephyr/logging/log.h>

#include <zephyr/net/coap.h>
#include <zephyr/net/socket.h>
#include <nrfc_dtls.h>
#include <zephyr/net/tls_credentials.h>

#include <zephyr/random/rand32.h>
#include <zcbor_common.h>
#include <zcbor_encode.h>
#include <zcbor_decode.h>

LOG_MODULE_REGISTER(coAp_module, LOG_LEVEL_INF);

#define APP_COAP_VERSION 1

static int sock;
static struct sockaddr_storage server;

static uint16_t token;
static uint8_t coap_buf[1024];


/// @brief Resolve the server's hostname
/// @return 0 on succes, <0 otherwise
static int server_resolve(void);

int mainThingy_init()
{
    int err;

    err = server_resolve();
    if (err)
    {
        LOG_ERR("Failed to resolve the server's hostname : %d\n", err);
        return err;
    }

//Creating a socket for connection with main server
#if defined(CONFIG_COAP_OVER_DTLS)
    LOG_DBG("DTLS enabled");

	sock = socket(AF_INET, SOCK_DGRAM, IPPROTO_DTLS_1_2);
	LOG_DBG("sock = %d", sock);
	if (sock < 0) {
		LOG_ERR("Failed to create CoAP socket: %d.", -errno);
		return -errno;
	}

    //Setting as in nrfc_dtlc.c, but with relevant parameters
    LOG_DBG("Setting socket options:");

	LOG_DBG("  hostname: %s", CONFIG_SERVER_HOSTNAME);
	err = setsockopt(sock, SOL_TLS, TLS_HOSTNAME, CONFIG_SERVER_HOSTNAME,
			 sizeof(CONFIG_SERVER_HOSTNAME));
	if (err) {
		LOG_ERR("Error setting hostname: %d", -errno);
		return -errno;
	}

    sec_tag_t sec_tag = CONFIG_SOCK_SEC_TAG;
	LOG_DBG("  sectag: %d", sec_tag);
	err = setsockopt(sock, SOL_TLS, TLS_SEC_TAG_LIST, &sec_tag, sizeof(sec_tag));
	if (err) {
		LOG_ERR("Failed to setup socket security tag: %d", -errno);
		return -errno;
	}

	int cid_option = TLS_DTLS_CID_SUPPORTED;

	LOG_DBG("  Enable connection id");
	err = setsockopt(sock, SOL_TLS, TLS_DTLS_CID, &cid_option, sizeof(cid_option));
	if (!err) {
		LOG_INF("Connection ID enabled");
	} else if ((err != EOPNOTSUPP) && (err != EINVAL)) {
		LOG_ERR("Error enabling connection ID: %d", -errno);
	} else {
		LOG_INF("Connection ID not supported by the provided socket");
	}

	int timeout = TLS_DTLS_HANDSHAKE_TIMEO_123S;

	LOG_DBG("  Set handshake timeout %d", timeout);
	err = setsockopt(sock, SOL_TLS, TLS_DTLS_HANDSHAKE_TIMEO,
			 &timeout, sizeof(timeout));
	if (!err) {
	} else if ((err != EOPNOTSUPP) || (err != EINVAL)) {
		LOG_ERR("Error setting handshake timeout: %d", -errno);
	}

	int verify = TLS_PEER_VERIFY_REQUIRED;

	LOG_DBG("  Peer verify: %d", verify);
	err = setsockopt(sock, SOL_TLS, TLS_PEER_VERIFY, &verify, sizeof(verify));
	if (err) {
		LOG_ERR("Failed to setup peer verification, errno %d", -errno);
		return -errno;
	}

	int session_cache = TLS_SESSION_CACHE_ENABLED;

	err = setsockopt(sock, SOL_TLS, TLS_SESSION_CACHE, &session_cache, sizeof(session_cache));
	if (err) {
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

    err = coap_packet_init(&request, coap_buf, sizeof(coap_buf),
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
