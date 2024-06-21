package com.nyanthingy.mobileapp.modules.permissions.features

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

enum class FeatureNotAvailableReason {
    PERMISSION_REQUIRED,
    NOT_AVAILABLE,
    DISABLED,
}

sealed class FeatureState {
    data object Available : FeatureState()
    data class NotAvailable(
        val reason: FeatureNotAvailableReason,
    ) : FeatureState()
}

class PermissionManager {
    companion object {
        const val PERMISSIONS_REFRESH =
            "com.nyanthingy.mobileapp.service.permission.PERMISSIONS_REFRESH"

        fun arePermissionsGranted(context: Context, permissions: List<String>) =
            permissions.fold(true) { granted: Boolean, permission: String ->
                granted && ActivityCompat.checkSelfPermission(
                    context, permission
                ) == PackageManager.PERMISSION_GRANTED
            }
    }
}
