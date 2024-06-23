#ifndef LED_STATE_EVENT_H
#define LED_STATE_EVENT_H

#include <zephyr/kernel.h>
#include <app_event_manager.h>
#include <app_event_manager_profiler_tracer.h>

struct led_state_event {
	struct app_event_header header;
	
	bool led_state;
};

APP_EVENT_TYPE_DECLARE(led_state_event);

#endif /* LED_STATE_EVENT_H */