#ifndef UART_CHANNEL_H
#define UART_CHANNEL_H

#include <zephyr/types.h>

/// @brief An UART buffer
struct uart_data_t {
	void *fifo_reserved;
	uint8_t data[CONFIG_UART_BUFFER_SIZE];
	uint16_t len;
};
/** @brief The api for the UART module that is represented by the 2 FIFOS.
 *  @param fifo_uart_tx_data represents the fifo for transfer
 *  @param fifo_uart_rx_data represents the fifo for receive
 *  @attention Buffers are freed upon the consumption */
extern struct k_fifo fifo_uart_tx_data,
                     fifo_uart_rx_data;

/// @brief init the uart channel
/// @return 0 on succes
int uart_channel_init();

#endif