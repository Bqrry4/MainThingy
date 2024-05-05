package com.nyanthingy.mobileapp.service.permission

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class BleFeatureState {
    data object Available : BleFeatureState()
    data class NotAvailable(
        val reason: BlePermissionNotAvailableReason,
    ) : BleFeatureState()
}

@Singleton
class BleStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val bluetoothStateFlow = callbackFlow {
        //send the initial state
        trySend(
            bluetoothAdapterState()
        )

        //register the receiver for when the bluetooth adapter state change
        val bluetoothStateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(
                    bluetoothAdapterState()
                )
            }
        }
        ContextCompat.registerReceiver(
            context,
            bluetoothStateChangeReceiver,
            IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
            },
            //Bluetooth is a privileged app that doesn't run under the system's UID
            ContextCompat.RECEIVER_EXPORTED
        )

        awaitClose {
            context.unregisterReceiver(bluetoothStateChangeReceiver)
        }
    }

    private fun bluetoothAdapterState() = when {
        //Ble not available
        !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) -> BleFeatureState.NotAvailable(
            BlePermissionNotAvailableReason.NOT_AVAILABLE
        )
        //Bluetooth disabled
        !(context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled -> BleFeatureState.NotAvailable(
            BlePermissionNotAvailableReason.DISABLED
        )

        else -> BleFeatureState.Available
    }
}