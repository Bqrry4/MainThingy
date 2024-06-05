package com.nyanthingy.mobileapp.modules.permissions

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.permissions.features.managers.BleStateManager
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureNotAvailableReason
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureState
import com.nyanthingy.mobileapp.modules.permissions.features.managers.LocationStateManager
import com.nyanthingy.mobileapp.modules.permissions.features.managers.NetworkStateManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FeatureViewModel @Inject constructor(
    private val _bluetoothStateManager: BleStateManager,
    private val _locationStateManager: LocationStateManager,
    private val _networkStateManager: NetworkStateManager
) : ViewModel() {

    val bluetoothState = _bluetoothStateManager
        .stateFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            FeatureState.NotAvailable(FeatureNotAvailableReason.NOT_AVAILABLE)
        )

    val locationState = _locationStateManager
        .stateFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            FeatureState.NotAvailable(FeatureNotAvailableReason.NOT_AVAILABLE)
        )

    val networkState = _networkStateManager
        .stateFlow()
        .stateIn(
            viewModelScope,
            SharingStarted.Lazily,
            FeatureState.NotAvailable(FeatureNotAvailableReason.NOT_AVAILABLE)
        )

}