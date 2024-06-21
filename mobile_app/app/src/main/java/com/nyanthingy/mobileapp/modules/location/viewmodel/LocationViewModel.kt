package com.nyanthingy.mobileapp.modules.location.viewmodel

import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.location.repository.LocationTracker
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureNotAvailableReason
import com.nyanthingy.mobileapp.modules.permissions.features.FeatureState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LocationViewModel @Inject constructor(
    private val locationTracker: LocationTracker
) : ViewModel() {

    val currentLocation = locationTracker.locationFlow()
        .stateIn(
        viewModelScope,
        SharingStarted.Lazily,
        null
    )
}