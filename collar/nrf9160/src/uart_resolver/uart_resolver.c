#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>

#include "uart_channel.h"
#include "buzzer.h"
#include "led.h"

#include "buzzer_state_event.h"
#include "led_state_event.h"

#include "modem_config.h"
#include "battery_monitor.h"

#include "uart_resolver.h"
#include "state_flags.h"
#include "server_exchange.h"

LOG_MODULE_REGISTER(uart_resolver);

void uart_resolver_thread(void *, void *, void *);
K_THREAD_STACK_DEFINE(uart_resolver_thread_stack_area, CONFIG_RESOLVER_THREAD_STACK_SIZE);
struct k_thread uart_resolver_thread_id;

int uart_resolver_init()
{
    // Start the resolver thread
    k_tid_t my_tid = k_thread_create(&uart_resolver_thread_id, uart_resolver_thread_stack_area,
                                     K_THREAD_STACK_SIZEOF(uart_resolver_thread_stack_area),
                                     uart_resolver_thread,
                                     NULL, NULL, NULL,
                                     CONFIG_RESOLVER_THREAD_PRIORITY, 0, K_NO_WAIT);

    return 0;
}

void uart_resolver_thread(void *, void *, void *)
{
    while (true)
    {
        // Blocking till the FIFO will be filled
        struct uart_data_t *buf = k_fifo_get(&fifo_uart_rx_data, K_FOREVER);

        // decode the buffer
        uint8_t type = buf->data[0];
        uint8_t resource = buf->data[1];
        // uint16_t length = buf->data[2] << 8;
        // length += buf->data[3];
        uint8_t value;

        LOG_INF("%d %d", type, resource);


        //suppose only request type is received for now 
        switch (resource)
        {
        case battery:

            // TODO: wait for the buffer
            struct uart_data_t *tx = k_malloc(sizeof(*tx));
            if (!tx)
            {
                LOG_WRN("Not able to allocate UART send data buffer");
                return;
            }

            uint8_t battery_lvl;
            battery_monitor_read(&battery_lvl);

            tx->data[0] = Response;
            tx->data[1] = battery;
            tx->data[2] = 0;
            tx->data[3] = 1;
            tx->data[4] = battery_lvl;
            tx->len = 5;
            k_fifo_put(&fifo_uart_tx_data, tx);

            break;
        case led:

            value = buf->data[4];
            struct led_state_event *led_event = new_led_state_event();
            led_event->led_state = value;
            APP_EVENT_SUBMIT(led_event);

            break;
        case buzzer:

            value = buf->data[4];
            struct buzzer_state_event *buzzer_event = new_buzzer_state_event();
            buzzer_event->buzzer_state = value;
            APP_EVENT_SUBMIT(buzzer_event);

            break;
        case ble_conn:
            // gnss start stop event
            value = buf->data[4];
            LOG_INF("BLE CONNECTION, %d", value);

            //wait if it is not initialized
            while(!isSystemInitialized)
            {
                k_sleep(K_SECONDS(5));
            }

            if (value)
            {
                stop_polling();
                deactivate_gnss();
                deactivate_lte();
            }
            else
            {
                activate_lte();
                activate_gnss();
                wait_for_lte_connection_blocking();
                server_connect();
                start_polling();
            }
            break;
        default:
            break;
        }

        // free the buffer from FIFO
        k_free(buf);
    }
}