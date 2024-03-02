#include <zephyr/kernel.h>
#include <modem/lte_lc.h>
#include <modem/nrf_modem_lib.h>

#include <zephyr/logging/log.h>

#include "modem_config.h"

#include <dk_buttons_and_leds.h>
#include <nrf_modem_at.h>
#include <zephyr/sys/timeutil.h>
#include <modem/modem_key_mgmt.h>


LOG_MODULE_REGISTER(MainThingy, LOG_LEVEL_INF);

// extern struct k_sem gnss_fix_sem;
// extern struct k_sem lte_connected_sem;

/// @return 0 on success, anything else otherwise
static int modem_configure(void)
{
        int err;

        err = nrf_modem_lib_init();
        if (err)
        {
                LOG_ERR("Modem library initialization failed, error: %d", err);
                return err;
        }

#if defined(CONFIG_COAP_OVER_DTLS)
        //Writing the pre-shared key to modem
        err = modem_key_mgmt_write(CONFIG_SOCK_SEC_TAG, MODEM_KEY_MGMT_CRED_TYPE_IDENTITY, CONFIG_SOCK_PSK_IDENTITY,
                                                                strlen(CONFIG_SOCK_PSK_IDENTITY));
        if (err) {
                LOG_ERR("Failed to write identity: %d\n", err);
                return err;
        }

        err = modem_key_mgmt_write(CONFIG_SOCK_SEC_TAG, MODEM_KEY_MGMT_CRED_TYPE_PSK, CONFIG_SOCK_PSK_SECRET,
                                                                strlen(CONFIG_SOCK_PSK_SECRET));
        if (err) {
                LOG_ERR("Failed to write identity: %d\n", err);
                return err;
        }
#endif /*CONFIG_COAP_OVER_DTLS*/

        err = lte_lc_init();
        if (err)
        {
                LOG_ERR("Modem LTE connection initialization failed, error: %d", err);
                return err;
        }

        err = lte_lc_offline();
        if (err)
        {
                LOG_ERR("Failed to start in OFFLINE functional mode");
                return err;
        }

        //This will activate LTE mode
        err = lte_init();
        if (err)
        {
                LOG_ERR("Failed to init LTE module, error: %d", err);
                return err;
        }
        
        wait_for_lte_connection(100);

        //This will start GNSS mode after the lte_init
        err = gnss_init();
        if (err)
        {
                LOG_ERR("Failed to init GNSS module, error: %d", err);
                return err;
        }

        return 0;
}

int main(void)
{
        int err;

        // FIXME:
        err = dk_leds_init();
        if (err)
        {
                LOG_ERR("Failed to initlize the LEDs Library");
        }

        LOG_INF("Starting app");

        if (modem_configure())
        {
                LOG_ERR("Failed on modem configuration");
                return -1;
        }
        
wait_for_lte_connection(100);

        mainThingy_init();
        send_gps_data();

       // k_sleep(K_SECONDS(300));



        

        //activate_gnss();

        // while (true)
        // {
        //         int err;
        //         int timezone;
        //         struct tm date_time;
        //         /* Read current UTC time from the modem. */
        //         err = nrf_modem_at_scanf("AT+CCLK?",
        // "+CCLK: \"%u/%u/%u,%u:%u:%u+%u\"",
        // 	&date_time.tm_year,
        // 	&date_time.tm_mon,
        // 	&date_time.tm_mday,
        // 	&date_time.tm_hour,
        // 	&date_time.tm_min,
        // 	&date_time.tm_sec,
        //         &timezone
        // );
        //             LOG_INF("+CCLK: \"%u/%u/%u,%u:%u:%u, %d , %u",
        // 	date_time.tm_year,
        // 	date_time.tm_mon,
        // 	date_time.tm_mday,
        // 	date_time.tm_hour,
        // 	date_time.tm_min,
        // 	date_time.tm_sec, err, timezone);
        //         k_sleep(K_MSEC(2000));
        //         // k_sem_take(&gnss_fix_sem, K_FOREVER);
        // }
        // k_work_schedule(&server_transmission_work, K_NO_WAIT);

        return 0;
}
