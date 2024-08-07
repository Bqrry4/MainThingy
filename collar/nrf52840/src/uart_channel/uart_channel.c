#include <zephyr/kernel.h>
#include <zephyr/device.h>
#include <zephyr/devicetree.h>
#include <zephyr/logging/log.h>
#include <zephyr/drivers/uart.h>

#include "uart_channel.h"

#define SLEEP_TIME_MS 1000
#define UART_WAIT_FOR_BUF_DELAY K_MSEC(CONFIG_UART_WAIT_FOR_BUFFER_DELAY)

LOG_MODULE_REGISTER(uart_channel);

K_FIFO_DEFINE(fifo_uart_tx_data);
K_FIFO_DEFINE(fifo_uart_rx_data);

static void uart_event_handler(const struct device *dev, struct uart_event *evt, void *data);

/** @brief The low power uart device*/
struct device *lpuart = DEVICE_DT_GET(DT_NODELABEL(lpuart));

/** @brief Work that tries to alloc a buffer and enable de receive on uart*/
static struct k_work_delayable wait_for_buf_work;
static void wait_for_buf_work_handler(struct k_work *work);

int uart_channel_init()
{
	int err;

	if (!device_is_ready(lpuart))
	{
		LOG_ERR("Uart device not ready");
		return -ENODEV;
	}

	err = uart_callback_set(lpuart, uart_event_handler, NULL);
	if (err)
	{
		LOG_ERR("Failed to set uart async callback, error: %d", err);
		return err;
	}

	k_work_init_delayable(&wait_for_buf_work, wait_for_buf_work_handler);

	// the receive buffer
	struct uart_data_t *rx;
	rx = k_malloc(sizeof(*rx));
	if (!rx)
	{
		LOG_ERR("Failed to allocate UART receive buffer: %d", err);
		return -ENOMEM;
	}
	rx->len = 0;

	err = uart_rx_enable(lpuart, rx->data, sizeof(rx->data), CONFIG_UART_RECEIVE_TIMEOUT);
	if (err)
	{
		LOG_ERR("Failed to enable uart receive, error: %d", err);
		return err;
	}

	// uint8_t buff[3] = {3, 2, 1};
	// int cnt = 0;
	// while(1)
	// {
	// 	k_sleep(K_SECONDS(3));
	// 	err = uart_tx(lpuart, buff + cnt, sizeof(buff[cnt]), SYS_FOREVER_US);
	// 	__ASSERT(err == 0, "Failed to initiate transmission");

	// 	if(++cnt > 3) cnt = 0;
	// 	LOG_INF("Sending %d", cnt);

	// 	k_sleep(K_MSEC(500));

	// }

	return 0;
}

static void uart_event_handler(const struct device *dev, struct uart_event *evt, void *user_data)
{
	// using only one uart device
	ARG_UNUSED(dev);

	struct uart_data_t *buf;

	switch (evt->type)
	{

	case UART_RX_RDY:
		LOG_DBG("UART_RX_RDY");

		// update the buffer len
		buf = CONTAINER_OF(evt->data.rx.buf, struct uart_data_t, data[0]);
		buf->len += evt->data.rx.len;

		break;

	case UART_RX_BUF_REQUEST:
		LOG_DBG("UART_RX_BUF_REQUEST");

		buf = k_malloc(sizeof(*buf));
		if (buf)
		{
			buf->len = 0;
			uart_rx_buf_rsp(lpuart, buf->data, sizeof(buf->data));
		}
		else
		{
			// cannot provide, receiving will stop
			LOG_WRN("Not able to allocate UART receive buffer");
		}
		break;

	case UART_RX_BUF_RELEASED:
		LOG_DBG("UART_RX_BUF_RELEASED");

		buf = CONTAINER_OF(evt->data.rx_buf.buf, struct uart_data_t, data[0]);
		if (buf->len)
		{
			// Push the received data to rx FIFO
			k_fifo_put(&fifo_uart_rx_data, buf);
		}
		else
		{
			// buffer empty
			k_free(buf);
		}
		break;

	case UART_RX_DISABLED:
		// try to alloc a buffer and reenable
		struct uart_data_t *buf = k_malloc(sizeof(*buf));
		if (!buf)
		{
			LOG_WRN("Not able to allocate UART receive buffer");
			k_work_reschedule(&wait_for_buf_work, UART_WAIT_FOR_BUF_DELAY);
			return;
		}

		buf->len = 0;
		uart_rx_enable(lpuart, buf->data, sizeof(buf->data), CONFIG_UART_RECEIVE_TIMEOUT);
		break;

	case UART_TX_DONE:
		LOG_DBG("UART_TX_DONE");

		// // check for aborted buffer
		// if ((evt->data.tx.len == 0) || (!evt->data.tx.buf))
		// 	return;
		break;

	case UART_TX_ABORTED:
		// Should trigger only when flow control is enabled
		LOG_DBG("UART_TX_ABORTED");
		break;

	case UART_RX_STOPPED:
		// do nothing for now
		break;

	default:
		break;
	}
}

static void wait_for_buf_work_handler(struct k_work *work)
{
	ARG_UNUSED(work);

	struct uart_data_t *buf = k_malloc(sizeof(*buf));
	if (!buf)
	{
		LOG_WRN("Not able to allocate UART receive buffer");
		k_work_reschedule(&wait_for_buf_work, UART_WAIT_FOR_BUF_DELAY);
		return;
	}

	buf->len = 0;
	uart_rx_enable(lpuart, buf->data, sizeof(buf->data), CONFIG_UART_RECEIVE_TIMEOUT);
}

// void uart_channel_tx(struct uart_data_t *tx)
// {
// 	// FIXME:
// 	for (int i = 0; i < tx->len; ++i)
// 	{
// 		LOG_INF("SENDING %hhx", tx->data[i]);
// 	}

// 	// if fifo is not empty put in fifo
// 	if (!k_fifo_is_empty(&fifo_uart_tx_data))
// 	{
// 		k_fifo_put(&fifo_uart_tx_data, tx);
// 		return;
// 	}

// 	// otherwise try to send
// 	int err = uart_tx(lpuart, tx->data, tx->len, SYS_FOREVER_MS);
// 	if (err == -EBUSY)
// 	{
// 		LOG_WRN("Ongoing transfer, postpone in in the fifo");
// 		k_fifo_put(&fifo_uart_tx_data, tx);
// 	}
// 	else
// 	{
// 		LOG_ERR("Failed to send data to uart with %d", err);
// 	}
// }

void retransmit_tx_thread(void)
{
	while (true)
	{
		// Blocking till the FIFO will be filled
		struct uart_data_t *buf = k_fifo_get(&fifo_uart_tx_data, K_FOREVER);

		int err = 0;
		do
		{
			// try to send
			err = uart_tx(lpuart, buf->data, buf->len, SYS_FOREVER_MS);
			if (err)
			{
				if (err == -EBUSY)
				{
					LOG_WRN("Ongoing transfer, trying again");
					k_msleep(CONFIG_FIFO_TX_RETRANSMISSION_DELAY);
				}
				else
				{
					LOG_ERR("Failed to send data to uart with %d", err);
				}
			}

		} while (err);

		// free the buffer from FIFO
		k_free(buf);
	}
}

K_THREAD_DEFINE(retransmit_tx_thread_id, CONFIG_UART_THREAD_STACK_SIZE, retransmit_tx_thread, NULL, NULL,
				NULL, CONFIG_UART_THREAD_PRIORITY, 0, 0);
