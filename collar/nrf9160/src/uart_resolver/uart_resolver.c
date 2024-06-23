#include <zephyr/kernel.h>
#include <zephyr/logging/log.h>

#include "uart_channel.h"
#include "buzzer.h"
#include "led.h"

#include "buzzer_state_event.h"
#include "led_state_event.h"

LOG_MODULE_REGISTER(uart_resolver);

/**  0 = response/notification
 *   1 = request NON
 *   2 = request CON
 */
enum MessageType
{
    Response,
    RequestNON,
    RequestCON
};

enum Resource
{
    battery,
    led,
    buzzer
};

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
        uint8_t value = buf->data[2];

        LOG_INF("%d %d %d %d", type, resource, value, buf->len);

        switch (resource)
        {
        case battery:
            /* code */
            break;
        case led:

            struct buzzer_state_event *led_event = new_led_state_event();
            led_event->buzzer_state = value;
            APP_EVENT_SUBMIT(led_event);

            break;
        case buzzer:

            struct buzzer_state_event *buzzer_event = new_buzzer_state_event();
            buzzer_event->buzzer_state = value;
            APP_EVENT_SUBMIT(buzzer_event);

            break;

        default:
            break;
        }

        // free the buffer from FIFO
        k_free(buf);
    }
}