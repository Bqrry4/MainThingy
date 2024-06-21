#include "gnss_fix_event.h"

static void log_gnss_event(const struct app_event_header *aeh)
{
	struct gnss_fix_event *event = cast_gnss_fix_event(aeh);

	APP_EVENT_MANAGER_LOG(aeh, "gnss_fix_event lat: %lf long: %lf alt: %lf",
			  event->pvt_data.latitude, event->pvt_data.longitude,
			  event->pvt_data.accuracy);
}

APP_EVENT_TYPE_DEFINE(gnss_fix_event,
			      log_gnss_event,
			      NULL,
			      APP_EVENT_FLAGS_CREATE(APP_EVENT_TYPE_FLAGS_INIT_LOG_ENABLE));
