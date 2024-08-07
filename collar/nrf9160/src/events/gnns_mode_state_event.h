#ifndef GNSS_MODE_STATE_EVENT_H
#define GNSS_MODE_STATE_EVENT_H

#include <zephyr/kernel.h>
#include <app_event_manager.h>
#include <app_event_manager_profiler_tracer.h>

struct gnns_mode_state_event {
	struct app_event_header header;
	
	bool state;
};

APP_EVENT_TYPE_DECLARE(gnns_mode_state_event);

#endif /* GNSS_MODE_STATE_EVENT_H */