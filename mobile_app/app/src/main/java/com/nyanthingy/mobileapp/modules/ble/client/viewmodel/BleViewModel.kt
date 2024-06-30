package com.nyanthingy.mobileapp.modules.ble.client.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.ble.client.manager.BleServiceManager
import com.nyanthingy.mobileapp.modules.commons.extensions.chunked
import com.nyanthingy.mobileapp.modules.commons.extensions.windowed
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.io.File
import javax.inject.Inject


@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleServiceManager: BleServiceManager
) : ViewModel() {


    /* The following are for binding to the service in a lifecycle manner. */
    init {
        bleServiceManager.bindService()
    }

    override fun onCleared() {
        super.onCleared()
        bleServiceManager.unbindService()
    }

    /**
     * Should be checked before interacting with the main functions
     */
    val availabilityState = bleServiceManager.isServiceBoundStateFlow

    fun connectionState(macAddress: String) = bleServiceManager.connectionState(macAddress)

    private val signalStrengthCache = mutableMapOf<String, StateFlow<String>>()
    fun signalStrength(macAddress: String): StateFlow<String> {

            return signalStrengthCache[macAddress] ?: bleServiceManager.rssiFlow(macAddress, 25)
            .chunked(20)
            // Find the mode of each chunk
            .map { chunk ->
                //Using the dictionary approach but functional
                chunk.groupingBy { it }
                    .eachCount()
                    .maxBy { it.value }
                    .key
            }
            .windowed(10)
            .map { sampled ->
                //Computing the derivative with finite difference
                val trend = sampled
                    .windowed(2)
                    .map {
                        it.last() - it.first()
                    }
                    .reduce { acc, value ->
                        acc + value
                    }
                when {
                    trend < 0 -> "Decreasing"
                    trend > 0 -> "Increasing"
                    else -> "Stable"
                }
            }.stateIn(
                viewModelScope,
                SharingStarted.Lazily,
                ""
            ).also {
                signalStrengthCache[macAddress] = it
            }
    }

    /* Main functionalities */
    fun getLedState() {

    }

    fun setLedState(macAddress: String, state: Boolean) =
        bleServiceManager.setLedState(macAddress, state)


    fun getBuzzerState() {

    }

    fun setBuzzerState(macAddress: String, state: Boolean) =
        bleServiceManager.setBuzzerState(macAddress, state)

}