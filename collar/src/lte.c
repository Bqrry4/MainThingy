#include <modem/lte_lc.h>
#include <zephyr/logging/log.h>

#include "modem_config.h"

LOG_MODULE_REGISTER(lte_module, LOG_LEVEL_INF);

K_SEM_DEFINE(lte_connected_sem, 0, 1);

static bool is_connected = false;

static void lte_handler(const struct lte_lc_evt *const evt);
static void configure_low_power(void);

int lte_init()
{
    int err;

    configure_low_power();

    err = lte_lc_connect_async(lte_handler);
    if (err)
    {
        LOG_ERR("Connecting to LTE network failed, error: %d", err);
        return err;
    }

    return 0;
}

int activate_lte()
{
    LOG_INF("Enabling LTE");

    int err = lte_lc_func_mode_set(LTE_LC_FUNC_MODE_ACTIVATE_LTE);
    if (err)
    {
        LOG_ERR("Failed to activate LTE functional mode");
        return err;
    }

    enum lte_lc_func_mode mode;

    lte_lc_func_mode_get(&mode);
    LOG_INF("%d mode", mode);

    LOG_ERR("Semafor in");

    k_sem_take(&lte_connected_sem, K_FOREVER);

    // FIXME:
    /* Wait for a while, because with IPv4v6 PDN the IPv6 activation takes a bit more time. */
    k_sleep(K_SECONDS(1));

    LOG_INF("After semafor out");

    return 0;
}

int deactivate_lte()
{
    LOG_INF("Disabling LTE");

    int err = lte_lc_func_mode_set(LTE_LC_FUNC_MODE_DEACTIVATE_LTE);
    if (err)
    {
        LOG_ERR("Failed to deactivate LTE functional mode");
        return err;
    }

    is_connected = false;

    return 0;
}

bool wait_for_lte_connection(uint16_t seconds)
{
    for (int i = 0; i < seconds; ++i)
    {
        k_sleep(K_SECONDS(1));
        if (is_connected)
            break;
    }

    return is_connected;
}

static void lte_handler(const struct lte_lc_evt *const evt)
{
    switch (evt->type)
    {
    case LTE_LC_EVT_NW_REG_STATUS:
        if ((evt->nw_reg_status == LTE_LC_NW_REG_REGISTERED_HOME) ||
            (evt->nw_reg_status == LTE_LC_NW_REG_REGISTERED_ROAMING))
        {
            LOG_INF("Network registration status: %s",
                    evt->nw_reg_status == LTE_LC_NW_REG_REGISTERED_HOME ? "Connected - home network" : "Connected - roaming");
            is_connected = true;
            break;
        }
        is_connected = false;
        break;
    case LTE_LC_EVT_PSM_UPDATE:
        LOG_INF("PSM parameter update: TAU: %d, Active time: %d",
                evt->psm_cfg.tau, evt->psm_cfg.active_time);
        break;
    case LTE_LC_EVT_EDRX_UPDATE:
    {
        // char log_buf[60];
        // ssize_t len;

        // len = snprintf(log_buf, sizeof(log_buf),
        //                "eDRX parameter update: eDRX: %f, PTW: %f\n",
        //                evt->edrx_cfg.edrx, evt->edrx_cfg.ptw);
        // if (len > 0)
        // {
        //     LOG_INF("%s\n", log_buf);
        // }
        break;
    }
    case LTE_LC_EVT_RRC_UPDATE:
        LOG_INF("RRC mode: %s\n",
                evt->rrc_mode == LTE_LC_RRC_MODE_CONNECTED ? "Connected" : "Idle");
        break;
    case LTE_LC_EVT_CELL_UPDATE:
        LOG_INF("LTE cell changed: Cell ID: %d, Tracking area: %d",
                evt->cell.id, evt->cell.tac);
        break;
    default:
        break;
    }
}

static void configure_low_power(void)
{
    int err;

#if defined(CONFIG_LTE_PSM_ENABLE)
    /** Power Saving Mode */
    err = lte_lc_psm_req(true);
    if (err)
    {
        LOG_ERR("lte_lc_psm_req, error: %d", err);
    }
#else
    err = lte_lc_psm_req(false);
    if (err)
    {
        LOG_ERR("lte_lc_psm_req, error: %d", err);
    }
#endif

#if defined(CONFIG_LTE_EDRX_ENABLE)
    /** enhanced Discontinuous Reception */
    err = lte_lc_edrx_req(true);
    if (err)
    {
        LOG_ERR("lte_lc_edrx_req, error: %d", err);
    }
#else
    err = lte_lc_edrx_req(false);
    if (err)
    {
        LOG_ERR("lte_lc_edrx_req, error: %d", err);
    }
#endif

#if defined(CONFIG_LTE_RAI_ENABLE)
    /** Release Assistance Indication  */
    err = lte_lc_rai_req(true);
    if (err)
    {
        LOG_ERR("lte_lc_rai_req, error: %d", err);
    }
#endif
}