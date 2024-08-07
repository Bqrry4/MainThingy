#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>

#include "peripheral_conn_event.h"
#include "uart_channel.h"

#include "uart_resolver.h"

LOG_MODULE_REGISTER(uart_resolver, LOG_LEVEL_INF);


static bool app_event_handler(const struct app_event_header *aeh)
{
    
    if (is_peripheral_conn_event(aeh))
    {
        struct peripheral_conn_event *event = cast_peripheral_conn_event(aeh);

        //TODO: wait for the buffer
        struct uart_data_t *tx = k_malloc(sizeof(*tx));
        if (!tx)
        {
            LOG_WRN("Not able to allocate UART send data buffer");
            return;
        }

        tx->data[0] = RequestNON;
        tx->data[1] = ble_conn;
        tx->data[2] = 0;
        tx->data[3] = 1;
        tx->data[4] = event->state;
        tx->len = 5;
        k_fifo_put(&fifo_uart_tx_data, tx);
        return true;
    }

    return false;
}

APP_EVENT_LISTENER(uart_resolver, app_event_handler);
APP_EVENT_SUBSCRIBE(uart_resolver, peripheral_conn_event);
