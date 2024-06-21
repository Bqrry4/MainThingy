package com.nyanthingy.mobileapp.modules.permissions.features.managers

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureNotAvailableReason
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NetworkStateManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun stateFlow() = callbackFlow {

        //Didn't found a way to send the initial state as functions for this are mostly deprecated

        val connectivityManagerCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(FeatureState.Available)
            }
            override fun onUnavailable() {
                trySend(FeatureState.NotAvailable(FeatureNotAvailableReason.NOT_AVAILABLE))
            }
        }

        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        connectivityManager.registerDefaultNetworkCallback(connectivityManagerCallback)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(connectivityManagerCallback)
        }
    }

}