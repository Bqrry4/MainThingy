#ifndef PERIPHERAL_CONN_EVENT_H
#define PERIPHERAL_CONN_EVENT_H

#include <zephyr/kernel.h>
#include <app_event_manager.h>
#include <app_event_manager_profiler_tracer.h>

struct peripheral_conn_event {
	struct app_event_header header;
};

APP_EVENT_TYPE_DECLARE(peripheral_conn_event);

#endif /* PERIPHERAL_CONN_EVENT_H */