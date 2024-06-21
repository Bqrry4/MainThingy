package com.nyanthingy.mobileapp.modules.ble.client

import java.util.UUID

object NyanThingySpecifications {
    /** Nordic NUS Service UUID. */
    val UUID_NUS_SERVICE: UUID = UUID.fromString("6E400001-B5A3-F393-E0A9-E50E24DCCA9E")

    /** RX characteristic UUID.
     * @note We write to this
     */
    val UUID_RX_CHAR: UUID = UUID.fromString("6E400002-B5A3-F393-E0A9-E50E24DCCA9E")

    /** TX characteristic UUID.
     * @note We read from this
     */
    val UUID_TX_CHAR: UUID = UUID.fromString("6E400003-B5A3-F393-E0A9-E50E24DCCA9E")
}