package com.nyanthingy.mobileapp.service.permission

import android.Manifest
import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle

enum class BlePermissionNotAvailableReason {
    PERMISSION_REQUIRED,
    NOT_AVAILABLE,
    DISABLED,
}

@Composable
fun RequireBluetooth(
    whenNotAvailable: @Composable (BlePermissionNotAvailableReason) -> Unit = {},
    content: @Composable () -> Unit
) {
    val viewModel = hiltViewModel<FeatureViewModel>()
    val state by viewModel.bluetoothState.collectAsStateWithLifecycle()

    when(val s = state) {
        is BleFeatureState.NotAvailable -> whenNotAvailable(s.reason)

        //Request permissions if not granted
        else -> {
            RequestPermissions(
                permissions = buildList {
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
                },
                granted = content,
                notGranted = { whenNotAvailable(BlePermissionNotAvailableReason.PERMISSION_REQUIRED) }
            )
        }
    }
}

