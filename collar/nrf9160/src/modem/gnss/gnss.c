#include <zephyr/kernel.h>
#include <nrf_modem_gnss.h>
#include <zephyr/logging/log.h>
#include <modem/lte_lc.h>

#include "../modem_config.h"
#include "assistance/assistance.h"
#include "gnss_fix_event.h"
#include "gnns_mode_state_event.h"


LOG_MODULE_REGISTER(gnss_module, LOG_LEVEL_DBG);

K_SEM_DEFINE(gnss_fix_sem, 0, 1);

#define GNSS_WORKQ_THREAD_STACK_SIZE 10000
#define GNSS_WORKQ_THREAD_PRIORITY 5

K_THREAD_STACK_DEFINE(gnss_workq_stack_area, GNSS_WORKQ_THREAD_STACK_SIZE);
static struct k_work_q gnss_work_q;

static struct nrf_modem_gnss_pvt_data_frame pvt_data;

static struct nrf_modem_gnss_agnss_data_frame agnss_data;
static struct k_work agnss_req_work;

static struct k_work lte_disable_work;

static void gnss_event_handler(int event_id);
static void agnss_req_work_fn(struct k_work *item);

int gnss_init()
{
    int err;

#if defined(CONFIG_USE_ASSISTANCE)

    err = assistance_init(&gnss_work_q);
    if (err)
    {
        LOG_ERR("Failed to init assistance module");
    }

    // Setting a work_queue
    struct k_work_queue_config cfg = {
        .name = "gnss_work_q",
        .no_yield = false};

    k_work_queue_start(
        &gnss_work_q,
        gnss_workq_stack_area,
        K_THREAD_STACK_SIZEOF(gnss_workq_stack_area),
        GNSS_WORKQ_THREAD_PRIORITY,
        &cfg);

    k_work_init(&agnss_req_work, agnss_req_work_fn);

#endif /*CONFIG_USE_ASSISTANCE*/

    if (nrf_modem_gnss_event_handler_set(gnss_event_handler))
    {
        LOG_ERR("Failed to set GNSS event handler");
        return -1;
    }

    // Using the periodic mode
    err = nrf_modem_gnss_fix_interval_set(CONFIG_GNSS_PERIODIC_INTERVAL);
    if (err)
    {
        LOG_ERR("Failed to set GNSS fix interval, error: %d", err);
        return err;
    }
    err = nrf_modem_gnss_fix_retry_set(CONFIG_GNSS_PERIODIC_TIMEOUT);
    if (err)
    {
        LOG_ERR("Failed to set GNSS fix retry, error: %d", err);
        return err;
    }

    // This use case flag should always be set.
    uint8_t use_case = NRF_MODEM_GNSS_USE_CASE_MULTIPLE_HOT_START | NRF_MODEM_GNSS_USE_CASE_LOW_ACCURACY;

    // Disable scheduled downloads when using assistance
    if (IS_ENABLED(CONFIG_USE_ASSISTANCE))
    {
        use_case |= NRF_MODEM_GNSS_USE_CASE_SCHED_DOWNLOAD_DISABLE;
    }

    err = nrf_modem_gnss_use_case_set(use_case);
    if (err)
    {
        LOG_WRN("Failed to set GNSS use case %d", err);
    }

    err = nrf_modem_gnss_start();
    if (err)
    {
        LOG_ERR("Failed to start GNSS, error: %d", err);
        return err;
    }

    err = nrf_modem_gnss_dyn_mode_change(CONFIG_GNSS_DYNAMICS_MODE);
    if (err)
    {
        LOG_ERR("Failed to set GNSS dynamics mode, error: %d", err);
        return err;
    }

    // Enable prio mode
    err = nrf_modem_gnss_prio_mode_enable();
    if (err)
    {
        LOG_ERR("Error setting GNSS priority mode");
    }

    return 0;
}

int activate_gnss()
{
    LOG_INF("Enabling GNSS");

    // int err = lte_lc_func_mode_set(LTE_LC_FUNC_MODE_ACTIVATE_GNSS);
    // if (err)
    // {
    //     LOG_ERR("Failed to activate GNSS functional mode");
    //     return err;
    // }

    int err = nrf_modem_gnss_start();
    if (err)
    {
        LOG_ERR("Failed to activate GNSS functional mode");
        return err;
    }

    return 0;
}

int deactivate_gnss()
{
    LOG_INF("Disabling GNSS");

    // int err = lte_lc_func_mode_set(LTE_LC_FUNC_MODE_DEACTIVATE_GNSS);
    // if (err)
    // {
    //     LOG_ERR("Failed to deactivate GNSS functional mode");
    //     return err;
    // }
    int err = nrf_modem_gnss_stop();
    if (err)
    {
        LOG_ERR("Failed to deactivate GNSS functional mode");
        return err;
    }
    return 0;
}

static void gnss_event_handler(int event_id)
{
    int err;
    struct gnss_fix_event *event;

    switch (event_id)
    {
    case NRF_MODEM_GNSS_EVT_PVT:

        err = nrf_modem_gnss_read(&pvt_data, sizeof(pvt_data), NRF_MODEM_GNSS_DATA_PVT);
        if (err)
        {
            LOG_ERR("nrf_modem_gnss_read failed, err %d", err);
            return;
        }

        if (pvt_data.flags & NRF_MODEM_GNSS_PVT_FLAG_NOT_ENOUGH_WINDOW_TIME)
        {
            LOG_DBG("GNSS blocked by LTE");
        }

        if (pvt_data.flags & NRF_MODEM_GNSS_PVT_FLAG_LEAP_SECOND_VALID)
        {
            LOG_DBG("LEAP_SECOND_VALID");
        }

        break;
    case NRF_MODEM_GNSS_EVT_PERIODIC_WAKEUP:
        LOG_INF("GNSS has woken up");
        break;
    case NRF_MODEM_GNSS_EVT_FIX:
        LOG_INF("GNSS recieved a fix");

        err = nrf_modem_gnss_read(&pvt_data, sizeof(pvt_data), NRF_MODEM_GNSS_DATA_PVT);
        if (err)
        {
            LOG_ERR("nrf_modem_gnss_read failed, err %d", err);
            return;
        }

        event = new_gnss_fix_event();

        event->pvt_data = pvt_data;
        APP_EVENT_SUBMIT(event);

        break;

    case NRF_MODEM_GNSS_EVT_SLEEP_AFTER_TIMEOUT:
        LOG_INF("GNSS sleep after timeout");
        break;

    case NRF_MODEM_GNSS_EVT_SLEEP_AFTER_FIX:
        LOG_DBG("GNSS sleep after FIX");
        break;

#if defined(CONFIG_USE_ASSISTANCE)
    case NRF_MODEM_GNSS_EVT_AGNSS_REQ:

        LOG_INF("AGNSS support requested");

        err = nrf_modem_gnss_read(&agnss_data, sizeof(agnss_data), NRF_MODEM_GNSS_DATA_AGNSS_REQ);
        if (err)
        {
            LOG_ERR("Failed to read agnss_req data, err %d", err);
            return;
        }

        // err = k_work_submit(&agnss_req_work);
        err = k_work_submit_to_queue(&gnss_work_q, &agnss_req_work);
        if (err < 0)
        {
            LOG_ERR("Failed to submit agnss_req_work to gnss_work_q, err %d", err);
            return;
        }
        break;
#endif /*CONFIG_USE_ASSISTANCE*/
    default:
        break;
    }
}

static void agnss_req_work_fn(struct k_work *item)
{
    ARG_UNUSED(item);

    int err;

    /* GPS data need is always expected to be present and first in list. */
    __ASSERT(agnss_data.system_count > 0,
             "GNSS system data need not found");
    __ASSERT(agnss_data.system[0].system_id == NRF_MODEM_GNSS_SYSTEM_GPS,
             "GPS data need not found");

    LOG_INF("Assistance data needed: data_flags: 0x%02x", agnss_data.data_flags);
    for (int i = 0; i < agnss_data.system_count; i++)
    {
        LOG_INF("Assistance data needed: %s ephe: 0x%llx, alm: 0x%llx",
                (agnss_data.system[i].system_id == NRF_MODEM_GNSS_SYSTEM_GPS)       ? "GPS"
                : (agnss_data.system[i].system_id == NRF_MODEM_GNSS_SYSTEM_QZSS)    ? "QZSS"
                : (agnss_data.system[i].system_id == NRF_MODEM_GNSS_SYSTEM_INVALID) ? "invalid"
                                                                                    : "unknown",
                agnss_data.system[i].sv_mask_ephe,
                agnss_data.system[i].sv_mask_alm);
    }

    err = assistance_request(&agnss_data);
    if (err)
    {
        LOG_ERR("Failed to request assistance data");
    }

    // Enabling gnss prio mode when needing additional data, which will be disabled after getting the fix
    err = nrf_modem_gnss_prio_mode_enable();
    if (err)
    {
        LOG_ERR("Error setting GNSS priority mode");
    }
    // deactivate_lte();
}

static bool app_event_handler(const struct app_event_header *aeh)
{

    if (is_gnns_mode_state_event(aeh))
    {
        struct gnns_mode_state_event *event = cast_gnns_mode_state_event(aeh);

        int err;
        LOG_INF("Changing gnss state to %d", event->state);
        if (event->state)
        {

            err = nrf_modem_gnss_stop();
            if (err)
            {
                LOG_INF("Failed to stop gnss");
            }
            err = nrf_modem_gnss_fix_interval_set(10);
            if (err)
            {
                LOG_INF("Failed to set interval");
            }
            err = nrf_modem_gnss_start();
            if (err)
            {
                LOG_INF("Failed to restart");
            }
        }
        else
        {
            err = nrf_modem_gnss_stop();
            if (err)
            {
                LOG_INF("Failed to stop gnss");
            }
            err = nrf_modem_gnss_fix_interval_set(CONFIG_GNSS_PERIODIC_INTERVAL);
            if (err)
            {
                LOG_INF("Failed to set interval");
            }
            err = nrf_modem_gnss_start();
            if (err)
            {
                LOG_INF("Failed to restart");
            }
        }

        return true;
    }

    return false;
}

APP_EVENT_LISTENER(gnss, app_event_handler);
APP_EVENT_SUBSCRIBE(gnss, gnns_mode_state_event);
