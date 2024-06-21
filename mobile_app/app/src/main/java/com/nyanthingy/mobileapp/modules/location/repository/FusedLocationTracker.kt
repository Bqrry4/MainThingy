package com.nyanthingy.mobileapp.modules.location.repository

import android.annotation.SuppressLint
import android.location.Location
import android.os.Looper
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.Priority
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

/**
 * Implementation with the *FusedLocationProviderClient*
 */
class FusedLocationTracker(
    private val _fusedLocationProviderClient: FusedLocationProviderClient,
) : LocationTracker {
    @SuppressLint("MissingPermission")
    override fun locationFlow() = callbackFlow {

        //try to send the first value
        _fusedLocationProviderClient.lastLocation.addOnSuccessListener { location: Location? ->
            location?.let {
                trySend(it)
            }
        }

        //register a callback request
        val locationRequest = LocationRequest
            .Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 1000L)
            .build()

        val locationListener: (it: Location) -> Unit = {
            trySend(it)
        }

        _fusedLocationProviderClient.requestLocationUpdates(
            locationRequest,
            locationListener,
            Looper.getMainLooper()
        )

        //suspend the coroutine and free callback on close
        awaitClose {
            _fusedLocationProviderClient.removeLocationUpdates(locationListener)
        }
    }
}