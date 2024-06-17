package com.nyanthingy.mobileapp.modules.permissions.features.managers

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureNotAvailableReason
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureState
import com.nyanthingy.mobileapp.modules.permissions.features.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BleStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    companion object {
        val permissions = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                add(Manifest.permission.BLUETOOTH_SCAN)
                add(Manifest.permission.BLUETOOTH_CONNECT)
                //add(Manifest.permission.BLUETOOTH_ADVERTISE)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            } else {
                add(Manifest.permission.BLUETOOTH)
                add(Manifest.permission.BLUETOOTH_ADMIN)
                add(Manifest.permission.ACCESS_FINE_LOCATION)
            }
        }
    }

    fun stateFlow() = callbackFlow {
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
            context, bluetoothStateChangeReceiver, IntentFilter().apply {
                addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
                addAction(PermissionManager.PERMISSIONS_REFRESH)
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
        !context.packageManager.hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE) -> FeatureState.NotAvailable(
            FeatureNotAvailableReason.NOT_AVAILABLE
        )
        //Bluetooth disabled
        !(context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager).adapter.isEnabled -> FeatureState.NotAvailable(
            FeatureNotAvailableReason.DISABLED
        )

        !PermissionManager.arePermissionsGranted(
            context, permissions
        ) -> FeatureState.NotAvailable(
            FeatureNotAvailableReason.PERMISSION_REQUIRED
        )

        else -> FeatureState.Available
    }
}