#include "battery_monitor.h"

#include <stdbool.h>
#include <string.h>
#include <zephyr/types.h>

#include <adp536x.h>

#define ADP536X_I2C_DEV_NAME	        
#define BATTERY_CHARGE_CAPACITY         0xFF /* Maximum battery capacity. */
#define BATTERY_CHARGE_SLEEP_MODE       true /* Set sleep mode. */
#define BATTERY_CHARGE_UPDATE_RATE      3 /* Sample every 16 minute. */

// void battery_monitor_init(void)
// {
// 	int ret;

// 	ret = adp536x_init(DEVICE_DT_GET(DT_NODELABEL(i2c2)));
// 	if (ret) {
// 		return;
// 	}

// 	ret = adp536x_bat_cap_set(BATTERY_CHARGE_CAPACITY);
// 	if (ret) {
// 		printk("Failed to set battery capacity.\n");
// 		return;
// 	}

// 	ret = adp536x_fuel_gauge_enable_sleep_mode(BATTERY_CHARGE_SLEEP_MODE);
// 	if (ret) {
// 		printk("Failed to enable fuel gauge sleep mode.\n");
// 		return;
// 	}

// 	ret = adp536x_fuel_gauge_update_rate_set(BATTERY_CHARGE_UPDATE_RATE);
// 	if (ret) {
// 		printk("Failed to set fuel gauge update rate.\n");
// 		return;
// 	}

// 	ret = adp536x_fuel_gauge_set(true);
// 	if (ret) {
// 		printk("Failed to enable fuel gauge.\n");
// 		return;
// 	}
// }

int battery_monitor_read(uint8_t *buf)
{
	return adp536x_fg_soc(buf);
}


