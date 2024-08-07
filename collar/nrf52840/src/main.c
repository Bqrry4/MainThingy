#include <zephyr/logging/log.h>
#include <app_event_manager.h>

#include "uart_channel/uart_channel.h"
#include "ble_gatt/ble.h"
#include "imu_classifier.h"

LOG_MODULE_REGISTER(MainThingy, LOG_LEVEL_INF);

int main()
{
    int err;

    err = app_event_manager_init();
    if (err)
    {
        LOG_ERR("Unable to init Application Event Manager (%d)", err);
        return err;
    }

    err = ble_init();
    if (err)
    {
        LOG_ERR("Failed to init ble module, %d", err);
        return err;
    }

    err = uart_channel_init();
    if (err)
    {
        LOG_ERR("Failed to init uart_channel module, %d", err);
        return err;
    }

    // should be initiated when ble is enabled
    err = imu_classifier_init();
    if (err)
    {
        LOG_ERR("Failed to init imu_classifier module, %d", err);
        return err;
    }

    LOG_INF("Started");

    return 0;
}