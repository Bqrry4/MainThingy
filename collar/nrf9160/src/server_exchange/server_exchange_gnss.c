#include <zephyr/kernel.h>
#include <zephyr/sys/timeutil.h>

#include <zephyr/net/coap.h>
#include <zcbor_common.h>
#include <zcbor_encode.h>
#include <zcbor_decode.h>

#include "gnss_fix_event.h"

// External init
extern int sock;
extern uint8_t *coap_send_buf;

static time_t gnss_mktime(struct nrf_modem_gnss_datetime *date)
{
    struct tm tm = {
        .tm_sec = date->seconds,
        .tm_min = date->minute,
        .tm_hour = date->hour,
        .tm_mday = date->day,
        .tm_mon = date->month - 1,
        .tm_year = date->year - 1900};
    return timeutil_timegm(&tm);
}

/**
 * @brief Send gnss data in CBOR format to the POST: devs/{mac-adress}/loc route on the server
 */
int send_gnss_data(struct nrf_modem_gnss_pvt_data_frame *pvt_data)
{
    int err;

    struct coap_packet request;
    uint8_t payload[128] = {0};

    // time_t timestamp = gnss_mktime(&pvt_data->datetime);

    ZCBOR_STATE_E(encoding_state, 0, payload, sizeof(payload), 0);
    err = zcbor_map_start_encode(encoding_state, 0) &&
          zcbor_tstr_put_lit(encoding_state, "lon") &&
          zcbor_float64_put(encoding_state, pvt_data->longitude) &&
          zcbor_tstr_put_lit(encoding_state, "lat") &&
          zcbor_float64_put(encoding_state, pvt_data->latitude) &&
          zcbor_tstr_put_lit(encoding_state, "acr") &&
          zcbor_float32_put(encoding_state, pvt_data->accuracy) &&
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

    uint8_t *token = coap_next_token();

    err = coap_packet_init(&request, coap_send_buf, sizeof(coap_send_buf),
                           CONFIG_COAP_APP_VERSION, COAP_TYPE_NON_CON,
                           COAP_TOKEN_MAX_LEN, token,
                           COAP_METHOD_POST, coap_next_id());
    if (err < 0)
    {
        LOG_ERR("Failed to create CoAP request, %d", err);
        return err;
    }

    err = coap_packet_append_option(&request, COAP_OPTION_URI_PATH,
                                    (uint8_t *)"devs", 4) ||
          coap_packet_append_option(&request, COAP_OPTION_URI_PATH,
                                    (uint8_t *)CONFIG_DEVICE_IDENTIFIER, 4) ||
          coap_packet_append_option(&request, COAP_OPTION_URI_PATH,
                                    (uint8_t *)"loc", 3);
    if (err < 0)
    {
        LOG_ERR("Failed to encode CoAP path options, %d", err);
        return err;
    }

    err = coap_packet_append_option(&request, COAP_OPTION_CONTENT_FORMAT,
                                    (uint8_t *)COAP_CONTENT_FORMAT_APP_CBOR,
                                    strlen(COAP_CONTENT_FORMAT_APP_CBOR));
    if (err < 0)
    {
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

    LOG_INF("CoAP request sent to /devs/{mac-adress}/loc resource: token 0x%04x, bytes sent: %d", token, err);

    return 0;
}

static bool app_event_handler(const struct app_event_header *aeh)
{
    if (is_gnss_fix_event(aeh))
    {
        struct gnss_fix_event *event = cast_gnss_fix_event(aeh);

        int err = send_gnss_data(&event->pvt_data);
        if (err)
        {
            LOG_ERR("Failed to send gnss data to server, %d", err);
        }

        return true;
    }

    return false;
}

APP_EVENT_LISTENER(gnss, app_event_handler);
APP_EVENT_SUBSCRIBE(gnss, gnss_fix_event);
