package com.nyanthingy.mobileapp.modules.map.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.maps.android.compose.MapType
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject


sealed class MapSelection {
    data class Google(val type: MapType) : MapSelection()
    data object OsmDroid : MapSelection()
}

@HiltViewModel
class MapPreferencesViewModel @Inject constructor(
) : ViewModel() {

    var mapType by mutableStateOf<MapSelection>(
        MapSelection.OsmDroid
    )

}