#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>

#include "modem/modem_config.h"
#include "uart_channel/uart_channel.h"

LOG_MODULE_REGISTER(MainThingy, LOG_LEVEL_INF);

int main(void)
{
        int err;

        uart_init();
        // LOG_INF("Starting app");

        // if (modem_configure())
        // {
        //         LOG_ERR("Failed on modem configuration");
        //         return -1;
        // }
        
        // wait_for_lte_connection(100);

        // server_exchange_init();
        // send_gps_data();

        return 0;
}
