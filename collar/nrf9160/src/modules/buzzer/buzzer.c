#include <zephyr/kernel.h>
#include <zephyr/drivers/pwm.h>
#include <zephyr/devicetree.h>
#include <zephyr/logging/log.h>

#include "buzzer.h"
#include "notes.h"

#include "buzzer_state_event.h"

LOG_MODULE_DECLARE(buzzer);

static const struct pwm_dt_spec buzzer = PWM_DT_SPEC_GET(DT_NODELABEL(buzzer));

/*Syncopated maxwell song */
#define MAXWELL_CAT 63
struct note_duration song[MAXWELL_CAT] = {
    {.note = D5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = D5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = D5, .duration = eigth},
    {.note = E5, .duration = eigth},
    {.note = F5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = F5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = B5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = B5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = B5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = A5, .duration = eigth},
    {.note = G5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = G5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = C5, .duration = half},
    {.note = A4, .duration = half},
    {.note = E5, .duration = half},
    {.note = C5, .duration = half},
    {.note = D5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = D5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = D5, .duration = eigth},
    {.note = E5, .duration = eigth},
    {.note = F5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = F5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = B5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = B5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = B5, .duration = eigth},
    {.note = A5, .duration = eigth},
    {.note = G5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = G5, .duration = eigth},
    {.note = REST, .duration = eigth},
    {.note = A5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = A5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = A5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = A5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = A5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = A5, .duration = sixteenth},
    {.note = REST, .duration = sixteenth},
    {.note = A5, .duration = eigth},
    {.note = B5, .duration = eigth},
    {.note = A5, .duration = quarter},
    {.note = G5, .duration = quarter},
    {.note = F5, .duration = quarter},
    {.note = E5, .duration = quarter},
};

/* The buzzer state*/
static bool state = false;

void buzzer_thread(void *, void *, void *);
K_THREAD_STACK_DEFINE(buzzer_thread_stack_area, CONFIG_RESPONSE_THREAD_STACK_SIZE);
struct k_thread buzzer_thread_id;

int buzzer_init()
{
    if (!device_is_ready(buzzer.dev))
    {
        LOG_ERR("Buzzer PWM device not ready");
        return -ENODEV;
    }

    // Start the buzzer thread with lowest prio
    k_tid_t my_tid = k_thread_create(&buzzer_thread_id, buzzer_thread_stack_area,
                                     K_THREAD_STACK_SIZEOF(buzzer_thread_stack_area),
                                     buzzer_thread,
                                     NULL, NULL, NULL,
                                     K_LOWEST_APPLICATION_THREAD_PRIO, 0, K_NO_WAIT);

    return 0;
}

void buzzer_thread(void *, void *, void *)
{
    while (true)
    {
        if (!state)
        {
            k_msleep(100);
            continue;
        }

        for (int i = 0; i < MAXWELL_CAT; i++)
        {
            // If state changed in meantime
            if (!state)
            {
                pwm_set_pulse_dt(&buzzer, 0);
                break;
            }
            
            // Lowest frequency note is C0 at 16.35 Hz
            if (song[i].note < 15)
            {
                // Interpret low frequencies as pauses
                pwm_set_pulse_dt(&buzzer, 0);
            }
            else
            {
                // Set to the note frequency with a duty cycle of 50%
                pwm_set_dt(&buzzer, PWM_HZ(song[i].note),
                           PWM_HZ((song[i].note)) / 2);
            }
            k_msleep(song[i].duration);
        }
    }
}

static bool app_event_handler(const struct app_event_header *aeh)
{
    if (is_buzzer_state_event(aeh))
    {
        struct buzzer_state_event *event = cast_buzzer_state_event(aeh);

        state = event->buzzer_state;

        return true;
    }

    return false;
}

APP_EVENT_LISTENER(buzzer, app_event_handler);
APP_EVENT_SUBSCRIBE(buzzer, buzzer_state_event);