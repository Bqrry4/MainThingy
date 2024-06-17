package com.nyanthingy.mobileapp.modules.ble.client.repository

import dagger.hilt.android.scopes.ActivityRetainedScoped
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import javax.inject.Inject

@ActivityRetainedScoped
class BleClientManager @Inject constructor(
) {
    val connectedDevices = mutableMapOf<String, ClientBleGatt>()

}