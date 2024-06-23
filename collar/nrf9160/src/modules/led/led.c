#include <dk_buttons_and_leds.h>
#include <zephyr/logging/log.h>

#include "led_state_event.h"

LOG_MODULE_DECLARE(led);

int led_init()
{
    int err;

    err = dk_leds_init();
    if (err)
    {
        LOG_ERR("Failed to initlize the LEDs Library");
    }

    return 0;
}

static bool app_event_handler(const struct app_event_header *aeh)
{
    if (is_led_state_event(aeh))
    {
        struct led_state_event *event = cast_led_state_event(aeh);

        dk_set_led(DK_LED1, event->led_state);

        return true;
    }

    return false;
}

APP_EVENT_LISTENER(led, app_event_handler);
APP_EVENT_SUBSCRIBE(led, led_state_event);