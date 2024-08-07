package com.nyanthingy.mobileapp.modules.server.repository

import com.nyanthingy.mobileapp.modules.server.network.GSPointResponseDTO
import com.nyanthingy.mobileapp.modules.server.network.StateRequestDTO
import kotlinx.coroutines.flow.Flow
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Path

interface RemoteDeviceRepository {
    fun getLocationFlow(
        macAddress: String,
        secret: String?,
        samplingRate: Long
    ): Flow<GSPointResponseDTO>

    fun getLocationsByTimestamp(
        macAddress: String,
        secret: String?,
        timestamp: String
    ): Flow<Iterable<GSPointResponseDTO>>

    fun setLedState(
        macAddress : String,
        secret: String?,
        state: StateRequestDTO
    )

    fun setBuzzerState(
        macAddress : String,
        secret: String?,
        state: StateRequestDTO
    )

    fun setGNSSModeState(
        macAddress : String,
        secret: String?,
        state: StateRequestDTO
    )
}