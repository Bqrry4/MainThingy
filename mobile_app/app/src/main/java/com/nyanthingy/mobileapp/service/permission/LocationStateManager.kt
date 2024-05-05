package com.nyanthingy.mobileapp.service.permission

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.LocationManager
import androidx.core.content.ContextCompat
import androidx.core.location.LocationManagerCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed class LocationFeatureState {
    data object Available : LocationFeatureState()
    data object NotAvailable : LocationFeatureState()
}

@Singleton
class LocationStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    val locationStateFlow = callbackFlow {
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
            },
            ContextCompat.RECEIVER_EXPORTED
        )

        awaitClose {
            context.unregisterReceiver(locationStateChangeReceiver)
        }
    }

    private fun locationServiceState() = when {
        //Location not available
        !context.packageManager.hasSystemFeature(PackageManager.FEATURE_LOCATION) -> LocationFeatureState.NotAvailable
        //Location disabled
        !LocationManagerCompat.isLocationEnabled(
            context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        ) -> LocationFeatureState.NotAvailable

        else -> LocationFeatureState.Available
    }
}