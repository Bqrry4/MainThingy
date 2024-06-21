#ifndef GNSS_FIX_EVENT_H
#define GNSS_FIX_EVENT_H

#include <zephyr/kernel.h>
#include <nrf_modem_gnss.h>
#include <app_event_manager.h>
#include <app_event_manager_profiler_tracer.h>

struct gnss_fix_event {
	struct app_event_header header;
	struct nrf_modem_gnss_pvt_data_frame pvt_data;
};

APP_EVENT_TYPE_DECLARE(gnss_fix_event);

#endif /* GNSS_FIX_EVENT_H */