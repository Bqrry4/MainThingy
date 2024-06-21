package com.nyanthingy.mobileapp.modules.permissions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.modules.permissions.features.managers.BleStateManager
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureNotAvailableReason
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureState


@Composable
fun RequireBluetooth(
    whenNotAvailable: @Composable (FeatureNotAvailableReason) -> Unit = {},
    content: @Composable () -> Unit
) {
    val viewModel = hiltViewModel<FeatureViewModel>()
    val state by viewModel.bluetoothState.collectAsStateWithLifecycle()

    when (val s = state) {
        is FeatureState.NotAvailable -> when (s.reason) {
            FeatureNotAvailableReason.PERMISSION_REQUIRED -> {
                RequestPermissions(
                    permissions = BleStateManager.permissions,
                    contentWhenRejected = { whenNotAvailable(FeatureNotAvailableReason.PERMISSION_REQUIRED) }
                )
            }
            else -> whenNotAvailable(s.reason)
        }
        is FeatureState.Available -> content()
    }
}

