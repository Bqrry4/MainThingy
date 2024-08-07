package com.nyanthingy.mobileapp.modules.server.repository

import android.util.Log
import com.nyanthingy.mobileapp.modules.server.network.ApiService
import com.nyanthingy.mobileapp.modules.server.network.GSPointResponseDTO
import com.nyanthingy.mobileapp.modules.server.network.StateRequestDTO
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class NetworkDeviceRepository @Inject constructor(
    private val apiService: ApiService
) : RemoteDeviceRepository {

    //this object scope
    private val repoJob = SupervisorJob()
    private val repoScope = CoroutineScope(Dispatchers.Main + repoJob)
    override fun getLocationFlow(
        macAddress: String,
        secret: String?,
        samplingRate: Long
    ): Flow<GSPointResponseDTO> {
        return callbackFlow {
            val pollingJob = launch {
//                var initial: GSPointResponseDTO? = null
                while (true) {
                    runCatching {
                        apiService.getLocation(macAddress, "Bearer $secret")
                    }.onSuccess {
                        trySend(it)
                    }
                    delay(samplingRate)
                }
            }

            awaitClose {
                pollingJob.cancel()
            }
        }
    }

    override fun getLocationsByTimestamp(macAddress: String, secret: String?, timestamp: String): Flow<Iterable<GSPointResponseDTO>> {

        return callbackFlow {
            runCatching {
                apiService.getLocationsByTimestamp(macAddress, "Bearer $secret", timestamp)
            }.onSuccess {
                trySend(it)
            }
                .onFailure { println(it.toString()) }
        }
    }

    override fun setLedState(macAddress: String, secret: String?, state: StateRequestDTO) {
        repoScope.launch {
            apiService.setLedState(macAddress, "Bearer $secret", state)
        }
    }


    override fun setBuzzerState(macAddress: String, secret: String?, state: StateRequestDTO) {
        repoScope.launch {
            apiService.setBuzzerState(macAddress, "Bearer $secret", state)
        }
    }

    override fun setGNSSModeState(macAddress: String, secret: String?, state: StateRequestDTO) {
        repoScope.launch {
            apiService.setGNSSModeState(macAddress, "Bearer $secret", state)
        }
    }
}