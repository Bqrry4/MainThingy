#ifndef UART_RESOLVER_H
#define UART_RESOLVER_H

/**
 * @brief init the uart resolver
 * @return 0 on succesŁ
 */
int uart_resolver_init();

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
    battery = 0,
    led = 1,
    buzzer = 2,
    ble_conn = 3
};

#endif