
#include <stdio.h>

#include <zephyr/logging/log.h>
#include <zephyr/sys/timeutil.h>
#include <nrf_modem_gnss.h>
#include <nrf_modem_at.h>
#include <net/nrf_cloud_agnss.h>
#include <net/nrf_cloud_coap.h>

#include "assistance.h"
#include "modem_config.h"

LOG_MODULE_REGISTER(assistance_module, LOG_LEVEL_INF);

static uint8_t agnss_buffer[4096];

/// Using the nRfCloud Coap api assistance
int assistance_init()
{
    int err;

    err = nrf_cloud_coap_init();
    if (err)
    {
        LOG_ERR("Failed to initialize nRF Cloud CoAP library.");
        return err;
    }

    return 0;
}

#pragma region TImeCalc
/* (6.1.1980 UTC - 1.1.1970 UTC) */
#define GPS_TO_UNIX_UTC_OFFSET_SECONDS (315964800UL)
/* UTC/GPS time offset as of 1st of January 2017. */
#define GPS_TO_UTC_LEAP_SECONDS (18UL)
#define SEC_PER_MIN (60UL)
#define MIN_PER_HOUR (60UL)
#define SEC_PER_HOUR (MIN_PER_HOUR * SEC_PER_MIN)
#define HOURS_PER_DAY (24UL)
#define SEC_PER_DAY (HOURS_PER_DAY * SEC_PER_HOUR)
#define DAYS_PER_WEEK (7UL)
#define PLMN_STR_MAX_LEN 8 /* MCC + MNC + quotes */

static int64_t utc_to_gps_sec(const int64_t utc_sec)
{
    return (utc_sec - GPS_TO_UNIX_UTC_OFFSET_SECONDS) + GPS_TO_UTC_LEAP_SECONDS;
}

static void gps_sec_to_day_time(int64_t gps_sec,
                                uint16_t *gps_day,
                                uint32_t *gps_time_of_day)
{
    *gps_day = (uint16_t)(gps_sec / SEC_PER_DAY);
    *gps_time_of_day = (uint32_t)(gps_sec % SEC_PER_DAY);
}

static void time_inject(void)
{
    int ret;
    struct tm date_time;
    int64_t utc_sec;
    int64_t gps_sec;
    struct nrf_modem_gnss_agnss_gps_data_system_time_and_sv_tow gps_time = {0};
    struct nrf_modem_gnss_agnss_gps_data_utc utc_time = {0};

    k_sleep(K_MSEC(10000));
    char buf[128];
    int timezone;
    /* Read current UTC time from the modem. */
    ret = nrf_modem_at_scanf("AT+CCLK?",
                             "+CCLK: \"%u/%u/%u,%u:%u:%u\"",
                             &date_time.tm_year,
                             &date_time.tm_mon,
                             &date_time.tm_mday,
                             &date_time.tm_hour,
                             &date_time.tm_min,
                             &date_time.tm_sec);
    LOG_INF("+CCLK: \"%u/%u/%u,%u:%u:%u, %d , %u",
            date_time.tm_year,
            date_time.tm_mon,
            date_time.tm_mday,
            date_time.tm_hour,
            date_time.tm_min,
            date_time.tm_sec, ret, timezone);

    if (ret != 6)
    {
        LOG_WRN("Couldn't read current time from modem, time assistance unavailable");
        return;
    }

    /* Convert to struct tm format. */
    date_time.tm_year = date_time.tm_year + 2000 - 1900; /* years since 1900 */
    date_time.tm_mon--;                                  /* months since January */

    /* Convert time to seconds since Unix time epoch (1.1.1970). */
    utc_sec = timeutil_timegm64(&date_time);
    /* Convert time to seconds since GPS time epoch (6.1.1980). */
    gps_sec = utc_to_gps_sec(utc_sec);

    gps_sec_to_day_time(gps_sec, &gps_time.date_day, &gps_time.time_full_s);

    utc_time;

    ret = nrf_modem_gnss_agnss_write(&gps_time, sizeof(gps_time),
                                     NRF_MODEM_GNSS_AGNSS_GPS_SYSTEM_CLOCK_AND_TOWS);
    if (ret != 0)
    {
        LOG_ERR("Failed to inject time, error %d", ret);
        return;
    }

    LOG_INF("Injected time (GPS day %u, GPS time of day %u)",
            gps_time.date_day, gps_time.time_full_s);
}

#pragma endregion TImeCalc

static int serving_cell_info_get(struct lte_lc_cell *serving_cell)
{
    int err;

    err = modem_info_init();
    if (err)
    {
        return err;
    }

    char resp_buf[MODEM_INFO_MAX_RESPONSE_SIZE];

    err = modem_info_string_get(MODEM_INFO_CELLID,
                                resp_buf,
                                MODEM_INFO_MAX_RESPONSE_SIZE);
    if (err < 0)
    {
        return err;
    }

    serving_cell->id = strtol(resp_buf, NULL, 16);

    err = modem_info_string_get(MODEM_INFO_AREA_CODE,
                                resp_buf,
                                MODEM_INFO_MAX_RESPONSE_SIZE);
    if (err < 0)
    {
        return err;
    }

    serving_cell->tac = strtol(resp_buf, NULL, 16);

    /* Request for MODEM_INFO_MNC returns both MNC and MCC in the same string. */
    err = modem_info_string_get(MODEM_INFO_OPERATOR,
                                resp_buf,
                                MODEM_INFO_MAX_RESPONSE_SIZE);
    if (err < 0)
    {
        return err;
    }

    serving_cell->mnc = strtol(&resp_buf[3], NULL, 10);
    /* Null-terminate MCC, read and store it. */
    resp_buf[3] = '\0';
    serving_cell->mcc = strtol(resp_buf, NULL, 10);

    return 0;
}

int assistance_request(struct nrf_modem_gnss_agnss_data_frame *agnss_request)
{

    int err;
    
    // Check for connection, and wait with a timeout of 5 min
    if(!wait_for_lte_connection(300))
    {
        LOG_ERR("Cannot proceed to request assistance from nRfCloud as LTE is lacking connection");
        return -1;
    }
    
    err = nrf_cloud_coap_connect(NULL);
    if (err)
    {
        LOG_ERR("Failed to connect to nRfCloud through coAp, %d", err);
        return err;
    }

    struct nrf_cloud_rest_agnss_request request = {
        .type = NRF_CLOUD_REST_AGNSS_REQ_CUSTOM,
        .agnss_req = agnss_request,
        .net_info = NULL};

    struct nrf_cloud_rest_agnss_result result = {
        .buf = agnss_buffer,
        .buf_sz = sizeof(agnss_buffer)};

    struct lte_lc_cells_info net_info = {0};

    // FIXME: Seems that it can crash the app if .net_info is NULL
    err = serving_cell_info_get(&net_info.current_cell);
    if (err)
    {
        LOG_ERR("Could not get cell info, error: %d", err);
        return err;
    }
    else
    {
        /* Network info for the location request. */
        request.net_info = &net_info;
    }

    err = nrf_cloud_coap_agnss_data_get(&request, &result);
    if (err)
    {
        LOG_ERR("Failed to get agnss data, error %d", err);
        return err;
    }

    LOG_INF("Processing A-GNSS data");

    err = nrf_cloud_agnss_process(result.buf, result.agnss_sz);
    if (err)
    {
        LOG_ERR("Failed to process A-GNSS data, error: %d", err);
        return err;
    }

    LOG_INF("A-GNSS data injected");
    
    return 0;
}

void agnss_pgp()
{
    int err;

    struct nrf_cloud_rest_agnss_request request = {
        .type = NRF_CLOUD_REST_AGNSS_REQ_CUSTOM,
        .agnss_req = NULL,
        .net_info = NULL};
    struct nrf_cloud_rest_agnss_result result;

    // FIXME:
    // TESTING
    request.type = NRF_CLOUD_REST_AGNSS_REQ_LOCATION;
    request.agnss_req = NULL;
    request.filtered = true;
    request.mask_angle = NRF_CLOUD_AGNSS_MASK_ANGLE_NONE;
    request.net_info = NULL;

    uint8_t buff[1024];

    result.buf = buff;
    result.buf_sz = sizeof(buff);

    // err = nrf_cloud_coap_agnss_data_get(&request, &result);
    if (err)
    {
        LOG_ERR("Failed to get agnss data., error %d", err);
    }

    struct nrf_modem_gnss_agnss_data_frame agnss_data;

    // nrf_modem_gnss_agnss_write(result.buf, result.agnss_sz, 0);
}