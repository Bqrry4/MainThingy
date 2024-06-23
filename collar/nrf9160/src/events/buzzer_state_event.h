#ifndef BUZZER_STATE_EVENT_H
#define BUZZER_STATE_EVENT_H

#include <zephyr/kernel.h>
#include <app_event_manager.h>
#include <app_event_manager_profiler_tracer.h>

struct buzzer_state_event {
	struct app_event_header header;
	
	bool buzzer_state;
};

APP_EVENT_TYPE_DECLARE(buzzer_state_event);

#endif /* BUZZER_STATE_EVENT_H */