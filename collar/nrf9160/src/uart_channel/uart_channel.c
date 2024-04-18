#include <zephyr/kernel.h>
#include <zephyr/device.h>
#include <zephyr/devicetree.h>
#include <zephyr/logging/log.h>
#include <zephyr/drivers/uart.h>

#include <dk_buttons_and_leds.h>


#define SLEEP_TIME_MS 1000
#define RECEIVE_BUFF_SIZE 10
#define RECEIVE_TIMEOUT 100

LOG_MODULE_REGISTER(uart_channel);

static void uart_event_handler(const struct device *dev, struct uart_event *evt, void *data);

struct device *lpuart = DEVICE_DT_GET(DT_NODELABEL(lpuart));
static uint8_t rx_buf[1024] = {0};

int uart_init()
{
	int err;

	if (!device_is_ready(lpuart))
	{
		LOG_ERR("Uart device not ready");
		return -1;
	}

	err = uart_callback_set(lpuart, uart_event_handler, NULL);
	if (err)
	{
		LOG_ERR("Failed to set uart async callback, error: %d", err);
		return err;
	}

	err = uart_rx_enable(lpuart, rx_buf, sizeof(rx_buf), RECEIVE_TIMEOUT);
	if (err)
	{
		LOG_ERR("Failed to enable uart recieve, error: %d", err);
		return err;
	}


	// FIXME:
	err = dk_leds_init();
	if (err)
	{
			LOG_ERR("Failed to initlize the LEDs Library");
	}
	return 0;
}
#define BUF_SIZE 64
static K_MEM_SLAB_DEFINE(uart_slab, BUF_SIZE, 3, 4);
static void uart_event_handler(const struct device *dev, struct uart_event *evt, void *user_data)
{

		LOG_INF("Here2 %d", evt->type);
	int err;
	switch (evt->type)
	{
	case UART_TX_DONE:
		// do something
		break;

	case UART_TX_ABORTED:
		// do something
		break;

	case UART_RX_RDY:
		switch (evt->data.rx.buf[evt->data.rx.offset])
		{	
		case '1':
			dk_set_led_on(DK_LED1);
			dk_set_led_off(DK_LED2);
			dk_set_led_off(DK_LED3);
			break;
		case '2':
			dk_set_led_off(DK_LED1);
			dk_set_led_on(DK_LED2);
			dk_set_led_off(DK_LED3);
			break;
		case '3':
			dk_set_led_off(DK_LED1);
			dk_set_led_off(DK_LED2);
			dk_set_led_on(DK_LED3);
			break;
		}
		break;

	case UART_RX_BUF_REQUEST:
	{
		// uint8_t *buf;

		// err = k_mem_slab_alloc(&uart_slab, (void **)&buf, K_NO_WAIT);
		// __ASSERT(err == 0, "Failed to allocate slab");

		// err = uart_rx_buf_rsp(lpuart, buf, BUF_SIZE);
		// __ASSERT(err == 0, "Failed to provide new buffer");
		uart_rx_buf_rsp(lpuart, rx_buf, 1024);
		break;
	}

	case UART_RX_BUF_RELEASED:
		//k_mem_slab_free(&uart_slab, (void *)evt->data.rx_buf.buf);
		break;

	case UART_RX_DISABLED:
		uart_rx_enable(lpuart, rx_buf, sizeof(rx_buf), RECEIVE_TIMEOUT);
		break;

	case UART_RX_STOPPED:
		// do something
		break;

	default:
		break;
	}
}