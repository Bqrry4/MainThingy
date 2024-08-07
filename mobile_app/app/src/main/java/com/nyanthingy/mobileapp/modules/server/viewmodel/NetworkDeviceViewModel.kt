package com.nyanthingy.mobileapp.modules.server.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.commons.extensions.windowed
import com.nyanthingy.mobileapp.modules.server.network.GSPointResponseDTO
import com.nyanthingy.mobileapp.modules.server.network.StateRequestDTO
import com.nyanthingy.mobileapp.modules.server.repository.RemoteDeviceRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.util.Date
import javax.inject.Inject

@HiltViewModel
class NetworkDeviceViewModel @Inject constructor(
    private val _remoteDeviceRepository: RemoteDeviceRepository
) : ViewModel() {

    private val locationStateCache = mutableMapOf<String, StateFlow<GSPointResponseDTO?>>()
    fun locationStateFlow(macAddress: String, secret: String?): StateFlow<GSPointResponseDTO?> {
        return locationStateCache[macAddress] ?: _remoteDeviceRepository
            .getLocationFlow(macAddress, secret, 5000)
            .stateIn(
                viewModelScope,
                SharingStarted.WhileSubscribed(),
                null
            ).also {
                locationStateCache[macAddress] = it
            }
    }

    fun locationsHistory24hrFlow(macAddress: String, secret: String?, timestamp: String)
    {
//        val a = Date() - 24
    }

    fun setLedState(
        macAddress : String,
        secret: String?,
        state: Boolean
    ) = _remoteDeviceRepository.setLedState(
        macAddress,
        secret,
        StateRequestDTO(
            state = state
        )
    )

    fun setBuzzerState(
        macAddress : String,
        secret: String?,
        state: Boolean
    ) = _remoteDeviceRepository.setBuzzerState(
        macAddress,
        secret,
        StateRequestDTO(
            state = state
        )
    )

    fun setGNSSModeState(
        macAddress : String,
        secret: String?,
        state: Boolean
    ) = _remoteDeviceRepository.setGNSSModeState(
        macAddress,
        secret,
        StateRequestDTO(
            state = state
        )
    )

}