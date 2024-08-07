#include <zephyr/logging/log.h>
#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/bluetooth/conn.h>
#include <zephyr/bluetooth/gap.h>
#include <zephyr/bluetooth/addr.h>

#include <bluetooth/services/nus.h>
#include <zephyr/settings/settings.h>

#include "uart_channel.h"
#include "ble.h"

#include "peripheral_conn_event.h"
#include "uart_resolver.h"
#include "imu_classifier.h"

LOG_MODULE_REGISTER(ble, LOG_LEVEL_INF);

void ble_write_thread(void *, void *, void *);
K_THREAD_STACK_DEFINE(ble_write_thread_stack_area, CONFIG_BT_NUS_THREAD_STACK_SIZE);
struct k_thread ble_write_thread_id;

static struct bt_le_adv_param *adv_param =
    BT_LE_ADV_PARAM((BT_LE_ADV_OPT_CONNECTABLE | BT_LE_ADV_OPT_USE_IDENTITY),
                    800,   /* Min 500ms (800*0.625ms) */
                    801,   /* Max 500.625ms (801*0.625ms) */
                    NULL); /* undirected advertising */

static const struct bt_data adv_data[] = {
    BT_DATA_BYTES(BT_DATA_FLAGS, (BT_LE_AD_GENERAL | BT_LE_AD_NO_BREDR)),
    BT_DATA(BT_DATA_NAME_COMPLETE, CONFIG_BT_DEVICE_NAME, (sizeof(CONFIG_BT_DEVICE_NAME) - 1))};

/// @brief Respond with services uuid
static const struct bt_data scan_data[] = {
    BT_DATA_BYTES(BT_DATA_UUID128_ALL, BT_UUID_NUS_VAL)};

struct bt_conn *my_conn = NULL;

static void update_phy(struct bt_conn *conn)
{
    int err;
    const struct bt_conn_le_phy_param preferred_phy = {
        .options = BT_CONN_LE_PHY_OPT_NONE,
        .pref_rx_phy = BT_GAP_LE_PHY_2M,
        .pref_tx_phy = BT_GAP_LE_PHY_2M,
    };
    err = bt_conn_le_phy_update(conn, &preferred_phy);
    if (err)
    {
        LOG_ERR("bt_conn_le_phy_update() returned %d", err);
    }
}

static void on_connected(struct bt_conn *conn, uint8_t err)
{

    if (err)
    {
        LOG_ERR("Connection error %d", err);
        return;
    }

    struct bt_conn_info info;
    err = bt_conn_get_info(conn, &info);
    if (err)
    {
        LOG_ERR("Failed to get connection info %d", err);
        return;
    }

    if (info.role == BT_CONN_ROLE_PERIPHERAL)
    {
        LOG_INF("Connected");
        my_conn = bt_conn_ref(conn);

        LOG_INF("Connection parameters: interval %.2f ms, peripheral latency %d intervals, supervision timeout %d ms",
                info.le.interval * 1.25, info.le.latency, info.le.timeout * 10);

        update_phy(my_conn);

        // Submit the connection event
        struct peripheral_conn_event *event = new_peripheral_conn_event();
        event->state = true;
        APP_EVENT_SUBMIT(event);
    }
}

static void on_disconnected(struct bt_conn *conn, uint8_t reason)
{
    int err;
    struct bt_conn_info info;
    err = bt_conn_get_info(conn, &info);
    if (err)
    {
        LOG_ERR("Failed to get connection info %d", err);
        return;
    }
    if (info.role == BT_CONN_ROLE_PERIPHERAL)
    {

        LOG_INF("Disconnected. Reason %d", reason);
        bt_conn_unref(my_conn);

        // Submit the connection event
        struct peripheral_conn_event *event = new_peripheral_conn_event();
        event->state = false;
        APP_EVENT_SUBMIT(event);
    }
}

#ifdef CONFIG_BT_NUS_SECURITY_ENABLED
static void security_changed(struct bt_conn *conn, bt_security_t level, enum bt_security_err err)
{
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, sizeof(addr));

    if (!err)
    {
        LOG_INF("Security changed: %s level %u", addr, level);
    }
    else
    {
        LOG_WRN("Security failed: %s level %u err %d", addr, level, err);
    }
}

static void auth_passkey_display(struct bt_conn *conn, unsigned int passkey)
{
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, sizeof(addr));

    LOG_INF("Passkey for %s: %06u", addr, passkey);
}

static void auth_cancel(struct bt_conn *conn)
{
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, sizeof(addr));

    LOG_INF("Pairing cancelled: %s", addr);
}

static void pairing_complete(struct bt_conn *conn, bool bonded)
{
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, sizeof(addr));

    LOG_INF("Pairing completed: %s, bonded: %d", addr, bonded);
}

static void pairing_failed(struct bt_conn *conn, enum bt_security_err reason)
{
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, sizeof(addr));

    LOG_INF("Pairing failed conn: %s, reason %d", addr, reason);
}

static struct bt_conn_auth_cb conn_auth_callbacks = {
    .passkey_display = auth_passkey_display,
    .cancel = auth_cancel,
};

static struct bt_conn_auth_info_cb conn_auth_info_callbacks = {
    .pairing_complete = pairing_complete,
    .pairing_failed = pairing_failed,
};
#endif

BT_CONN_CB_DEFINE(connection_callbacks) = {
    .connected = on_connected,
    .disconnected = on_disconnected,
#ifdef CONFIG_BT_NUS_SECURITY_ENABLED
    .security_changed = security_changed,
#endif
};

static void bt_receive_cb(struct bt_conn *conn, const uint8_t *const data, uint16_t len)
{
    char addr[BT_ADDR_LE_STR_LEN] = {0};

    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, ARRAY_SIZE(addr));
    LOG_INF("Received data from: %s", addr);

    // for (uint16_t pos = 0; pos != len;)
    // {
    //     struct uart_data_t *tx = k_malloc(sizeof(*tx));
    //     if (!tx)
    //     {
    //         LOG_WRN("Not able to allocate UART send data buffer");
    //         return;
    //     }

    //     /* Keep the last byte of TX buffer for potential LF char. */
    //     size_t tx_data_size = sizeof(tx->data) - 1;

    //     if ((len - pos) > tx_data_size)
    //     {
    //         tx->len = tx_data_size;
    //     }
    //     else
    //     {
    //         tx->len = (len - pos);
    //     }

    //     memcpy(tx->data, &data[pos], tx->len);

    //     pos += tx->len;

    //     /* Append the LF character when the CR character triggered
    //      * transmission from the peer.
    //      */
    //     if ((pos == len) && (data[len - 1] == '\r'))
    //     {
    //         tx->data[tx->len] = '\n';
    //         tx->len++;
    //     }

    //     k_fifo_put(&fifo_uart_tx_data, tx);
    // }

    struct uart_data_t *tx = k_malloc(sizeof(*tx));
    if (!tx)
    {
        LOG_WRN("Not able to allocate UART send data buffer");
        return;
    }

    // If the resource can be resolved locally
    switch (data[1])
    {
        // suppose its only a request
    case activity:

        tx->data[0] = Response;
        tx->data[1] = activity;
        tx->data[2] = 0;
        tx->data[3] = 16;

        memcpy(tx->data + 4, &stationary_time, sizeof(stationary_time));
        memcpy(tx->data + 4 + sizeof(stationary_time), &motion_time, sizeof(motion_time));

        tx->len = 20;

        // using the receive fifo to send back the data
        k_fifo_put(&fifo_uart_rx_data, tx);
        break;

    default:

        memcpy(tx->data, data, len);

        tx->len = len;

        k_fifo_put(&fifo_uart_tx_data, tx);
        break;
    }
}

static struct bt_nus_cb nus_cb = {
    .received = bt_receive_cb};

int ble_init()
{
    int err;

    bt_addr_le_t addr;
    err = bt_addr_le_from_str("FF:EE:DD:CC:BB:AA", "random", &addr);
    if (err)
    {
        LOG_INF("Invalid BT address %d", err);
    }

    err = bt_id_create(&addr, NULL);
    if (err < 0)
    {
        LOG_INF("Creating new ID failed %d", err);
    }

#if defined(CONFIG_BT_NUS_SECURITY_ENABLED)
    err = bt_conn_auth_cb_register(&conn_auth_callbacks);
    if (err)
    {
        LOG_INF("Failed to register authorization callbacks, %d", err);
        return err;
    }
    err = bt_conn_auth_info_cb_register(&conn_auth_info_callbacks);
    if (err)
    {
        LOG_ERR("Failed to register authorization info callbacks, %d", err);
        return err;
    }

    err = bt_passkey_set(CONFIG_BT_NUS_SECURITY_PASSKEY);
    if (err)
    {
        LOG_ERR("Failed to set static passkey, %d", err);
        return err;
    }
#endif

    err = bt_enable(NULL);
    if (err)
    {
        LOG_ERR("Bluetooth init failed %d", err);
        return err;
    }
    LOG_INF("Bluetooth initialized\n");

    //Restoring the pairing keys
    err = settings_load();
    if(err)
    {
        LOG_ERR("Failed to load settings %d", err);
    }

    err = bt_nus_init(&nus_cb);
    if (err)
    {
        LOG_ERR("Failed to initialize NUS service %d", err);
        return err;
    }

    err = bt_le_adv_start(adv_param, adv_data, ARRAY_SIZE(adv_data),
                          scan_data, ARRAY_SIZE(scan_data));
    if (err)
    {
        LOG_ERR("Advertising failed to start %d", err);
        return err;
    }

    // Start the listen for responses thread
    k_tid_t my_tid = k_thread_create(&ble_write_thread_id, ble_write_thread_stack_area,
                                     K_THREAD_STACK_SIZEOF(ble_write_thread_stack_area),
                                     ble_write_thread,
                                     NULL, NULL, NULL,
                                     CONFIG_BT_NUS_THREAD_PRIORITY, 0, K_NO_WAIT);
    return 0;
}

void ble_write_thread(void *, void *, void *)
{

    while (true)
    {
        /* Wait indefinitely for data from the UART peripheral */
        struct uart_data_t *buf = k_fifo_get(&fifo_uart_rx_data, K_FOREVER);
        /* Send data over Bluetooth LE */
        if (bt_nus_send(NULL, buf->data, buf->len))
        {
            LOG_WRN("Failed to send data over BLE connection");
        }

        k_free(buf);
    }
}