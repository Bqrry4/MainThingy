#include <zephyr/bluetooth/bluetooth.h>
#include <zephyr/bluetooth/conn.h>
#include <zephyr/bluetooth/uuid.h>
#include <zephyr/bluetooth/gatt.h>
#include <bluetooth/gatt_dm.h>
#include <bluetooth/scan.h>
#include <zephyr/logging/log.h>

#include "imu_classifier.h"
LOG_MODULE_REGISTER(imu_classifier, LOG_LEVEL_INF);

#define BT_UUID_BNO086_NAME "BNO086 Integration"
#define BT_UUID_BNO086 BT_UUID_DECLARE_128(BT_UUID_128_ENCODE(0x31A46D94, 0xDFB5, 0x49D8, 0xB1C7, 0x7B15F65D021B))
#define BT_UUID_BNO086_CHAR BT_UUID_DECLARE_128(BT_UUID_128_ENCODE(0xEE7323E8, 0x795D, 0x41DE, 0xB29C, 0x3FC9D970035F))

static struct bt_conn *central_conn;

static uint8_t notify_cb(struct bt_conn *conn,
                         struct bt_gatt_subscribe_params *params,
                         const void *data, uint16_t length)
{
    if (!data)
    {
        LOG_INF("Notification/indication disabled\n");
        params->value_handle = 0U;
        return BT_GATT_ITER_STOP;
    }

    LOG_INF("Notification received");

    // FIXME: DO THINGS

    return BT_GATT_ITER_CONTINUE;
}

static void discovery_completed_cb(struct bt_gatt_dm *dm,
                                   void *context)
{
    int err;

    LOG_INF("The discovery procedure succeeded");

    bt_gatt_dm_data_print(dm);

    const struct bt_gatt_dm_attr *gatt_char = bt_gatt_dm_char_by_uuid(dm, BT_UUID_BNO086_CHAR);
    if (!gatt_char)
    {
        LOG_ERR("No characteristic found");
        goto finally;
    }

    static struct bt_gatt_subscribe_params subscribe_params;
    const struct bt_gatt_dm_attr *gatt_desc;

    gatt_desc = bt_gatt_dm_desc_by_uuid(dm, gatt_char, BT_UUID_BNO086_CHAR);
    if (!gatt_desc)
    {
        LOG_ERR("No descriptor value found");
        goto finally;
    }
    subscribe_params.value_handle = gatt_desc->handle;

    gatt_desc = bt_gatt_dm_desc_by_uuid(dm, gatt_char, BT_UUID_GATT_CCC);
    if (!gatt_desc)
    {
        LOG_ERR("No descriptor CCC found");
        goto finally;
    }
    subscribe_params.ccc_handle = gatt_desc->handle;

    subscribe_params.notify = notify_cb;
    subscribe_params.value = BT_GATT_CCC_NOTIFY;
    atomic_set_bit(subscribe_params.flags,
                   BT_GATT_SUBSCRIBE_FLAG_VOLATILE);

    err = bt_gatt_subscribe(central_conn, &subscribe_params);
    if (err)
    {
        LOG_ERR("Subscribe to char failed, %d", err);
    }

finally:
    err = bt_gatt_dm_data_release(dm);
    if (err)
    {
        LOG_ERR("Could not release the discovery data, %d", err);
    }
}

static void discovery_not_found_cb(struct bt_conn *conn,
                                   void *context)
{
    LOG_INF("Service could not be found during the discovery");
}

static void discovery_error_found_cb(struct bt_conn *conn,
                                     int err, void *context)
{
    LOG_INF("The discovery procedure failed with %d", err);
}

static const struct bt_gatt_dm_cb discovery_cb = {
    .completed = discovery_completed_cb,
    .service_not_found = discovery_not_found_cb,
    .error_found = discovery_error_found_cb};

static void gatt_discover(struct bt_conn *conn)
{
    int err;

    err = bt_gatt_dm_start(conn, BT_UUID_BNO086, &discovery_cb, NULL);
    if (err)
    {
        LOG_ERR("Could not start the discovery procedure, %d", err);
    }
}

static int scan_start(void)
{
    int err = bt_scan_start(BT_SCAN_TYPE_SCAN_PASSIVE);
    if (err)
    {
        LOG_ERR("Scanning failed to start, %d", err);
    }

    LOG_INF("Scanning");
    return err;
}

static void connected(struct bt_conn *conn, uint8_t conn_err)
{
    int err;
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, sizeof(addr));

    if (conn_err)
    {
        LOG_ERR("Failed to connect to %s (%u)", addr, conn_err);

        if (conn == central_conn)
        {
            bt_conn_unref(central_conn);
            central_conn = NULL;

            scan_start();
        }

        return;
    }

    struct bt_conn_info info;
    err = bt_conn_get_info(conn, &info);
    if (err)
    {
        LOG_ERR("Failed to get connection info %d", err);
        return;
    }
    if (info.role == BT_CONN_ROLE_CENTRAL)
    {
        LOG_INF("Connected: %s", addr);

        // Set secturity at basic level
        err = bt_conn_set_security(conn, BT_SECURITY_L1);
        if (err)
        {
            LOG_INF("Failed to set security, %d", err);
        }

        gatt_discover(conn);
    }
}

static void disconnected(struct bt_conn *conn, uint8_t reason)
{
    char addr[BT_ADDR_LE_STR_LEN];
    bt_addr_le_to_str(bt_conn_get_dst(conn), addr, sizeof(addr));
    LOG_INF("Disconnected: %s (reason %u)\n", addr, reason);

    if (conn == central_conn)
    {
        bt_conn_unref(central_conn);
        central_conn = NULL;

        scan_start();
    }
}

// Reregister callbacks for this module
BT_CONN_CB_DEFINE(conn_callbacks) = {
    .connected = connected,
    .disconnected = disconnected};

static void scan_filter_match(struct bt_scan_device_info *device_info,
                              struct bt_scan_filter_match *filter_match,
                              bool connectable)
{
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(device_info->recv_info->addr, addr, sizeof(addr));

    LOG_INF("Filters matched. Address: %s connectable: %d",
            addr, connectable);
}

static void scan_connecting_error(struct bt_scan_device_info *device_info)
{
    LOG_INF("Connecting failed");
}

static void scan_connecting(struct bt_scan_device_info *device_info,
                            struct bt_conn *conn)
{
    central_conn = bt_conn_ref(conn);
    LOG_INF("Connecting..");
}

static void filter_no_match(struct bt_scan_device_info *device_info,
                            bool connectable)
{
    char addr[BT_ADDR_LE_STR_LEN];

    bt_addr_le_to_str(device_info->recv_info->addr, addr, sizeof(addr));
    LOG_INF("Filter no match %d, %s ", connectable, addr);
}

BT_SCAN_CB_INIT(scan_cb, scan_filter_match, NULL,
                scan_connecting_error, scan_connecting);

static int scan_init(void)
{
    int err;

    // Connect automatically after a match
    struct bt_scan_init_param param = {
        .scan_param = NULL,
        .conn_param = BT_LE_CONN_PARAM_DEFAULT,
#if CONFIG_BT_CENTRAL
        .connect_if_match = 1};
#endif /* CONFIG_BT_CENTRAL */

    bt_scan_init(&param);
    bt_scan_cb_register(&scan_cb);

    // err = bt_scan_filter_add(BT_SCAN_FILTER_TYPE_UUID, BT_UUID_BNO086);
    // if (err)
    // {
    //     LOG_ERR("Scanning filters cannot be set, %d", err);
    // }

    /*The used IMU advertises only it's name*/

    err = bt_scan_filter_add(BT_SCAN_FILTER_TYPE_NAME, BT_UUID_BNO086_NAME);
    if (err)
    {
        LOG_ERR("Scanning filters cannot be set, %d", err);
        return err;
    }

    err = bt_scan_filter_enable(BT_SCAN_NAME_FILTER, false);
    if (err)
    {
        LOG_ERR("Filters cannot be turned on, %d", err);
        return err;
    }
}

int imu_classifier_init()
{
    int err;

    err = scan_init();
    if (err)
    {
        LOG_ERR("Failed to init scan, %d", err);
        return err;
    }

    err = scan_start();
    if (err)
    {
        LOG_ERR("Failed to start scan, %d", err);
        return err;
    }

    return 0;
}