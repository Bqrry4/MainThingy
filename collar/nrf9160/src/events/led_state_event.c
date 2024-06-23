#include "led_state_event.h"

APP_EVENT_TYPE_DEFINE(led_state_event,
		  NULL,
		  NULL,
		  APP_EVENT_FLAGS_CREATE(APP_EVENT_TYPE_FLAGS_INIT_LOG_ENABLE));

