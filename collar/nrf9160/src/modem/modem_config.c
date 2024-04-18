#include <nrf_modem_at.h>
#include <zephyr/sys/timeutil.h>
#include <modem/modem_key_mgmt.h>
#include <zephyr/logging/log.h>
#include <modem/lte_lc.h>
#include <modem/nrf_modem_lib.h>

LOG_MODULE_REGISTER(modem_config);

int modem_configure()
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
