#include <zephyr/kernel.h>
#include <zephyr/device.h>
#include <zephyr/drivers/watchdog.h>
#include <zephyr/logging/log.h>

#include "watchdog.h"

LOG_MODULE_REGISTER(watchdog);

#define WDT_FEED_WORKER_DELAY_MS \
    ((CONFIG_WATCHDOG_TIMEOUT * 1000) / 2)
#define WATCHDOG_TIMEOUT_MSEC \
    (CONFIG_WATCHDOG_TIMEOUT * 1000)

struct device *wdt = DEVICE_DT_GET(DT_NODELABEL(wdt));
int wdt_channel_id;

static void feed_worker(struct k_work *work);
struct k_work_delayable watchdog_feed_workqueue;

int watchdog_init_and_start(void)
{
    int err;

    if (!device_is_ready(wdt))
    {
        LOG_ERR("Watchdog device not ready");
        return -ENODEV;
    }

    struct wdt_timeout_cfg wdt_config = {
        .window = {
            .min = 0,
            .max = WATCHDOG_TIMEOUT_MSEC,
        },
        .callback = NULL,
        .flags = WDT_FLAG_RESET_SOC};

    wdt_channel_id = wdt_install_timeout(wdt, &wdt_config);
    if (wdt_channel_id < 0)
    {
        LOG_ERR("Cannot install watchdog timer! Error code: %d", wdt_channel_id);
        return -EFAULT;
    }

    // Pause watchdog timer when CPU is halted by the debugger
    err = wdt_setup(wdt, WDT_OPT_PAUSE_HALTED_BY_DBG);
    if (err)
    {
        LOG_ERR("Cannot start watchdog, %d", err);
        return err;
    }
    LOG_DBG("Watchdog started");

    k_work_init_delayable(&watchdog_feed_workqueue, feed_worker);
    k_work_schedule(&watchdog_feed_workqueue, K_NO_WAIT);
    LOG_DBG("Watchdog feed enabled. Timeout: %d", WDT_FEED_WORKER_DELAY_MS);

    return 0;
}

static void feed_worker(struct k_work *work)
{
    LOG_DBG("Feeding watchdog");

    int err = wdt_feed(wdt, wdt_channel_id);
    if (err)
    {
        LOG_ERR("Cannot feed watchdog: %d", err);
        return;
    }

    //Reschedule this work
    k_work_reschedule(work, K_MSEC(WDT_FEED_WORKER_DELAY_MS));
}