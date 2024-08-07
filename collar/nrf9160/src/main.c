#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>
#include <app_event_manager.h>

#include "modem_config.h"
#include "uart_channel.h"
#include "battery_monitor.h"
#include "server_exchange.h"
#include "buzzer.h"
#include "led.h"
#include "uart_resolver.h"
#include "state_flags.h"
#include "watchdog.h"

bool isSystemInitialized = false;

LOG_MODULE_REGISTER(MainThingy, LOG_LEVEL_INF);

int main(void)
{
    int err;

    if (modem_configure())
    {
        LOG_ERR("Failed on modem configuration");
        return err;
    }
    // k_sleep(K_SECONDS(25));

    // wait_for_lte_connection(100);
    wait_for_lte_connection_blocking();
    err = server_exchange_init();
    if (err)
    {
        LOG_ERR("Failed to init the server_exchange module (%d)", err);
        return err;
    }

    

    err = buzzer_init();
    if (err)
    {
        LOG_ERR("Failed to init buzzer module (%d)", err);
        return err;
    }

    err = led_init();
    if (err)
    {
        LOG_ERR("Failed to init led module (%d)", err);
        return err;
    }

    err = uart_channel_init();
    if (err)
    {
        LOG_ERR("Failed to init the uart_channel module (%d)", err);
        return err;
    }

    err = app_event_manager_init();
    if (err)
    {
        LOG_ERR("Unable to init Application Event Manager (%d)", err);
        return err;
    }

    err = uart_resolver_init();
    if (err)
    {
        LOG_ERR("Failed to init the uart_resolver module (%d)", err);
        return err;
    }

    err = watchdog_init_and_start();
    if(err)
    {
        LOG_ERR("Failed to start watchdog (%d)", err);
        return err;    
    }

    LOG_INF("Started");
    isSystemInitialized = true;

    return 0;
}
