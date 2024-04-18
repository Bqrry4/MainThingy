
#include <stdio.h>
#include <zephyr/logging/log.h>
#include <zephyr/sys/timeutil.h>
#include <nrf_modem_gnss.h>
#include <nrf_modem_at.h>
#include <net/nrf_cloud_coap.h>
#include <net/nrf_cloud_agnss.h>
#include <net/nrf_cloud_pgps.h>

#include "../../modem_config.h"

#include "assistance.h"


/* This module is using the nRfCloud Coap api assistance */

LOG_MODULE_REGISTER(assistance_module);

#if defined(CONFIG_USE_ASSISTANCE_AGNSS)
static uint8_t agnss_buffer[4096];
#endif /* CONFIG_USE_ASSISTANCE_AGNSS */

#if defined(CONFIG_USE_ASSISTANCE_PGPS)

/// @brief Saving the agnss request, to inject it only with data from PGPS when AGNSS request to the cloud will fail
static struct nrf_modem_gnss_agnss_data_frame agnss_need;
static struct gps_pgps_request *pgps_request;
static struct nrf_cloud_pgps_prediction *prediction;

// The function for getting the pgps data
static void get_pgps_data_work_fn(struct k_work *work);
static struct k_work get_pgps_data_work;

// The function for injecting a pgps prediction
static void inject_pgps_data_work_fn(struct k_work *work);
static struct k_work inject_pgps_data_work;

// The pgps handler function
static void pgps_event_handler(struct nrf_cloud_pgps_event *event);
#endif /* CONFIG_USE_ASSISTANCE_PGPS */

// Reusing the working queue
static struct k_work_q *assistance_work_q;

int assistance_init(struct k_work_q *work_q)
{
    int err;

    __ASSERT(assistance_work_q != NULL,
             "null pointer assertion for parameter work_q");

    assistance_work_q = work_q;

    err = nrf_cloud_coap_init();
    if (err)
    {
        LOG_ERR("Failed to initialize nRF Cloud CoAP library.");
        return err;
    }

#if defined(CONFIG_USE_ASSISTANCE_PGPS)
    k_work_init(&get_pgps_data_work, get_pgps_data_work_fn);
    k_work_init(&inject_pgps_data_work, inject_pgps_data_work_fn);

    struct nrf_cloud_pgps_init_param pgps_param = {
        .event_handler = pgps_event_handler,
        /* storage is defined by CONFIG_NRF_CLOUD_PGPS_STORAGE */
        .storage_base = 0u,
        .storage_size = 0u};

    err = nrf_cloud_pgps_init(&pgps_param);
    if (err)
    {
        LOG_ERR("Failed to initialize P-GPS");
        return err;
    }
#endif /* CONFIG_USE_ASSISTANCE_PGPS */

    return 0;
}

#if defined(CONFIG_USE_ASSISTANCE_PGPS)
static void get_pgps_data_work_fn(struct k_work *work)
{
    ARG_UNUSED(work);

    int err;

    // Check for connection, and wait with a timeout of 1 min
    if (!wait_for_lte_connection(60))
    {
        LOG_ERR("Cannot proceed to request pgps assistance from nRfCloud as LTE is lacking connection");
        goto onFailure;
    }

    LOG_INF("Sending request for P-GPS predictions to nRF Cloud...");

    struct nrf_cloud_rest_pgps_request request = {
        .pgps_req = pgps_request};

    struct nrf_cloud_pgps_result file_location;

    err = nrf_cloud_coap_pgps_url_get(&request, &file_location);
    if (err)
    {
        LOG_ERR("Failed to retrieve the url for the PGPS, %d", err);
        goto onFailure;
    }

    LOG_INF("Processing P-GPS response");

    err = nrf_cloud_pgps_update(&file_location);
    if (err)
    {
        LOG_ERR("Failed to process binary P-GPS data using given url, %d", err);
        goto onFailure;
    }

    LOG_INF("P-GPS response processed");
    return;

onFailure:
    //re-enable future retries
    nrf_cloud_pgps_request_reset();
}

static void inject_pgps_data_work_fn(struct k_work *work)
{
    ARG_UNUSED(work);

    int err;

    LOG_INF("Injecting P-GPS ephemerides");

    err = nrf_cloud_pgps_inject(prediction, &agnss_need);
    if (err)
    {
        LOG_ERR("Failed to inject P-GPS ephemerides");
    }

    err = nrf_cloud_pgps_preemptive_updates();
    if (err)
    {
        LOG_ERR("Failed to request P-GPS updates");
    }
}

static void pgps_event_handler(struct nrf_cloud_pgps_event *event)
{
    switch (event->type)
    {
    case PGPS_EVT_AVAILABLE:
        prediction = event->prediction;

        k_work_submit_to_queue(assistance_work_q, &inject_pgps_data_work);
        break;

    case PGPS_EVT_REQUEST:
        // memcpy(&pgps_request, event->request, sizeof(pgps_request));
        pgps_request = event->request;

        k_work_submit_to_queue(assistance_work_q, &get_pgps_data_work);
        break;

    case PGPS_EVT_LOADING:
        LOG_INF("Loading P-GPS predictions");
        break;

    case PGPS_EVT_READY:
        LOG_INF("P-GPS predictions ready");
        break;

    default:
        break;
    }
}
#endif /* CONFIG_USE_ASSISTANCE_PGPS */

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
    int err = 0;

#if defined(CONFIG_USE_ASSISTANCE_PGPS)

    /* Store the agnss request for pgps use. */
    memcpy(&agnss_need, agnss_request, sizeof(agnss_need));

#if defined(CONFIG_USE_ASSISTANCE_AGNSS)
    if (!agnss_request->data_flags)
    {
        /* No assistance needed from A-GPS, skip directly to P-GPS. */
        nrf_cloud_pgps_notify_prediction();
        return 0;
    }

    // PGPS will handle ephemerides, so skip those.
    agnss_request->system->sv_mask_ephe = 0;
    // Almanacs are not needed with PGPS, so skip those.
    agnss_request->system->sv_mask_alm = 0;

#endif /* CONFIG_USE_ASSISTANCE_AGNSS */
#endif /* CONFIG_USE_ASSISTANCE_PGPS */

#if defined(CONFIG_USE_ASSISTANCE_AGNSS)
    // Check for connection, and wait with a timeout of 5 min
    if (!wait_for_lte_connection(300))
    {
        LOG_ERR("Cannot proceed to request agnss assistance from nRfCloud as LTE is lacking connection");
        goto agnss_exit;
    }

    err = nrf_cloud_coap_connect(NULL);
    if (err)
    {
        LOG_ERR("Failed to connect to nRfCloud through coAp, %d", err);
        goto agnss_exit;
    }

    struct nrf_cloud_rest_agnss_request request = {
        .type = NRF_CLOUD_REST_AGNSS_REQ_CUSTOM,
        .agnss_req = agnss_request,
        .net_info = NULL};

    struct nrf_cloud_rest_agnss_result result = {
        .buf = agnss_buffer,
        .buf_sz = sizeof(agnss_buffer)};

    struct lte_lc_cells_info net_info = {0};

    // Warning: Seems that it can crash the app if .net_info is NULL
    err = serving_cell_info_get(&net_info.current_cell);
    if (err)
    {
        LOG_ERR("Could not get cell info, error: %d", err);
        goto agnss_exit;
    }

    // Network info for the location request.
    request.net_info = &net_info;

    err = nrf_cloud_coap_agnss_data_get(&request, &result);
    if (err)
    {
        LOG_ERR("Failed to get agnss data, error %d", err);
        goto agnss_exit;
    }

    LOG_INF("Processing A-GNSS data");

    err = nrf_cloud_agnss_process(result.buf, result.agnss_sz);
    if (err)
    {
        LOG_ERR("Failed to process A-GNSS data, error: %d", err);
        goto agnss_exit;
    }

    LOG_INF("A-GNSS data injected");

agnss_exit:
#endif /* CONFIG_USE_ASSISTANCE_AGNSS */

#if defined(CONFIG_USE_ASSISTANCE_PGPS)
    nrf_cloud_pgps_notify_prediction();
#endif /* CONFIG_USE_ASSISTANCE_PGPS */

    return err;
}