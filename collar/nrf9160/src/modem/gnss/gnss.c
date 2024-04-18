#include <zephyr/kernel.h>
#include <nrf_modem_gnss.h>
#include <zephyr/logging/log.h>
#include <modem/lte_lc.h>

#include "../modem_config.h"
#include "assistance/assistance.h"


LOG_MODULE_REGISTER(gnss_module);

K_SEM_DEFINE(gnss_fix_sem, 0, 1);

#define GNSS_WORKQ_THREAD_STACK_SIZE 4096
#define GNSS_WORKQ_THREAD_PRIORITY 5

K_THREAD_STACK_DEFINE(gnss_workq_stack_area, GNSS_WORKQ_THREAD_STACK_SIZE);
static struct k_work_q gnss_work_q;

//::FIXME:
#include <dk_buttons_and_leds.h>

static struct nrf_modem_gnss_pvt_data_frame pvt_data;

static struct nrf_modem_gnss_agnss_data_frame agnss_data;
static struct k_work agnss_req_work;

static struct k_work lte_disable_work;

static void gnss_event_handler(int event_id);
static void print_fix_data(struct nrf_modem_gnss_pvt_data_frame *pvt_data);
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

    // //FIXME: if assistance is disabled set to normal
        err = nrf_modem_gnss_use_case_set(NRF_MODEM_GNSS_USE_CASE_MULTIPLE_HOT_START | NRF_MODEM_GNSS_USE_CASE_LOW_ACCURACY | NRF_MODEM_GNSS_USE_CASE_SCHED_DOWNLOAD_DISABLE);
    if (err) {
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
    k_sleep(K_SECONDS(2));

    // nrf_modem_gnss_stop();

    // FIXME: need to fix about it
    //  err = nrf_modem_gnss_prio_mode_enable();
    //  if (err)
    //  {
    //      LOG_ERR("Error setting GNSS priority mode");
    //  }

    return 0;
}

int activate_gnss()
{
    LOG_INF("Enabling GNSS");

    int err = lte_lc_func_mode_set(LTE_LC_FUNC_MODE_ACTIVATE_GNSS);
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

    int err = lte_lc_func_mode_set(LTE_LC_FUNC_MODE_DEACTIVATE_GNSS);
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

    LOG_INF("%d id", event_id);

    switch (event_id)
    {
    case NRF_MODEM_GNSS_EVT_PVT:
        // FIXME: Repair the function
        int num_satellites = 0;
        for (int i = 0; i < 12; i++)
        {
            if (pvt_data.sv[i].signal != 0)
            {
                //LOG_INF("sv: %d, cn0: %d", pvt_data.sv[i].sv, pvt_data.sv[i].cn0);
                num_satellites++;
            }
        }

        //LOG_INF("Number of current satellites: %d", num_satellites);

        if (nrf_modem_gnss_read(&pvt_data, sizeof(pvt_data), NRF_MODEM_GNSS_DATA_PVT))
        {
            LOG_ERR("nrf_modem_gnss_read failed, err %d", err);
            return;
        }

    LOG_INF("%d fags", pvt_data.flags);


        if (pvt_data.flags & NRF_MODEM_GNSS_PVT_FLAG_NOT_ENOUGH_WINDOW_TIME)
        {
            //LOG_INF("GNSS blocked by LTE");

            // err = k_work_submit(&lte_disable_work);
            // if(err < 0)
            // {
            //     LOG_ERR("failed to submit lte_disable_work, err %d", err);
            // }
        }

        if (pvt_data.flags & NRF_MODEM_GNSS_PVT_FLAG_LEAP_SECOND_VALID)
        {
            //LOG_INF("LEAP_SECOND_VALID");
        }

        //LOG_INF("flags %d", pvt_data.flags);

        if (pvt_data.flags & NRF_MODEM_GNSS_PVT_FLAG_FIX_VALID)
        {
            dk_set_led_on(DK_LED1);
            // dk_set_led_on(DK_LED1);
            print_fix_data(&pvt_data);
            /* STEP 12.3 - Print the time to first fix */
            if (true)
            {
                // LOG_INF("Time to first fix: %2.1lld s", (k_uptime_get() - gnss_start_time) / 1000);
            }
            return;
        }
        break;
    case NRF_MODEM_GNSS_EVT_PERIODIC_WAKEUP:
        LOG_INF("GNSS has woken up");
        dk_set_led_off(DK_LED2);
        dk_set_led_off(DK_LED1);
        dk_set_led_on(DK_LED3);

        break;
    case NRF_MODEM_GNSS_EVT_FIX:
        LOG_INF("GNSS recieved a fix");
        dk_set_led_on(DK_LED2);
        dk_set_led_off(DK_LED3);
        break;

    case NRF_MODEM_GNSS_EVT_SLEEP_AFTER_TIMEOUT:
        LOG_INF("GNSS sleep after timeout");
        dk_set_led_on(DK_LED2);
        dk_set_led_on(DK_LED1);
        dk_set_led_on(DK_LED3);
        dk_set_led_off(DK_LED3);

        break;

    // FIXME: bugged event
    case NRF_MODEM_GNSS_EVT_SLEEP_AFTER_FIX:
        LOG_INF("GNSS sleep after FIX");
        // dk_set_led_on(DK_LED3);
        // dk_set_led_off(DK_LED1);
        // k_sem_give(&gnss_fix_sem);

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

    //Enabling gnss prio mode when needing additional data, which will be disabled after getting the fix
    err = nrf_modem_gnss_prio_mode_enable();
    if (err)
    {
        LOG_ERR("Error setting GNSS priority mode");
    }
    deactivate_lte();
}

//FIXME: needed only for debug
static void print_fix_data(struct nrf_modem_gnss_pvt_data_frame *pvt_data)
{
    LOG_INF("Latitude:       %.06f", pvt_data->latitude);
    LOG_INF("Longitude:      %.06f", pvt_data->longitude);
    LOG_INF("Altitude:       %.01f m", pvt_data->altitude);
    LOG_INF("Time (UTC):     %02u:%02u:%02u.%03u",
            pvt_data->datetime.hour,
            pvt_data->datetime.minute,
            pvt_data->datetime.seconds,
            pvt_data->datetime.ms);
}