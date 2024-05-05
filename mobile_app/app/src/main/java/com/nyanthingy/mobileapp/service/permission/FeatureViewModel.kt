package com.nyanthingy.mobileapp.service.permission

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val _bluetoothStateManager: BleStateManager,
    private val _locationStateManager: LocationStateManager
) : ViewModel() {

    val bluetoothState = _bluetoothStateManager
        .bluetoothStateFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            BleFeatureState.NotAvailable(BlePermissionNotAvailableReason.NOT_AVAILABLE)
        )


    val locationState = _locationStateManager
        .locationStateFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            LocationFeatureState.NotAvailable
        )

}