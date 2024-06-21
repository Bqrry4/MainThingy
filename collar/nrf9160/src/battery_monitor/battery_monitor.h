#include <zephyr/types.h>

/**
 * @brief Init the battery charge monitor module.
 *
 */
//void battery_monitor_init(void);

/**
 * @brief Read the current battery charge.
 *
 * @return 0 if the operation was successful, otherwise a (negative) error code.
 */
int battery_monitor_read(uint8_t *buf);