package com.nyanthingy.mobileapp.modules.permissions.features.managers

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureNotAvailableReason
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureState
import com.nyanthingy.mobileapp.modules.permissions.features.PermissionManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        val permissions = listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }
    fun stateFlow() = callbackFlow {
        //send the initial state
        trySend(
            locationServiceState()
        )

        //register the receiver for when the location service state change
        val locationStateChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                trySend(
                    locationServiceState()
                )
            }
        }
        ContextCompat.registerReceiver(
            context,
            locationStateChangeReceiver,
            IntentFilter().apply {
                addAction(LocationManager.MODE_CHANGED_ACTION)
                addAction(PermissionManager.PERMISSIONS_REFRESH)
            },
            ContextCompat.RECEIVER_EXPORTED
        )

        awaitClose {
            context.unregisterReceiver(locationStateChangeReceiver)
        }
    }

    private fun locationServiceState() = when {
        //Location not available
        !context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) -> FeatureState.NotAvailable(
            FeatureNotAvailableReason.NOT_AVAILABLE
        )
        //Location disabled
        !LocationManagerCompat.isLocationEnabled(
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        ) -> FeatureState.NotAvailable(
            FeatureNotAvailableReason.DISABLED
        )

        !PermissionManager.arePermissionsGranted(context, permissions) -> FeatureState.NotAvailable(
            FeatureNotAvailableReason.PERMISSION_REQUIRED
        )

        else -> FeatureState.Available
    }
}