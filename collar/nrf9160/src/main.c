#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>
#include <app_event_manager.h>

#include "modem_config.h"
#include "uart_channel.h"
#include "battery_monitor.h"
#include "server_exchange.h"

LOG_MODULE_REGISTER(MainThingy, LOG_LEVEL_INF);

int main(void)
{
    int err;

    err = app_event_manager_init();
    if (err)
    {
        LOG_ERR("Unable to init Application Event Manager (%d)", err);
        return 0;
    }

    if (modem_configure())
    {
        LOG_ERR("Failed on modem configuration");
        return -1;
    }

    err = server_exchange_init();
    if (err)
    {
        LOG_ERR("Failed to init the server_exchange module (%d)", err);
        return 0;
    }

    // uart_init();
    //  LOG_INF("Starting app");

    // if (modem_configure())
    // {
    //         LOG_ERR("Failed on modem configuration");
    //         return -1;
    // }

    // wait_for_lte_connection(100);

    // send_gps_data();

    uint8_t c;
    while (true)
    {
        battery_monitor_read(&c);
        LOG_INF("%d", c);
    };

    return 0;
}
