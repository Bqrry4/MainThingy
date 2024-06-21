package com.nyanthingy.mobileapp.modules.map.viewmodel

import android.os.Parcelable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.google.maps.android.compose.MapType
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

interface MapsProperties {
    companion object {
        const val MapMaximZoomLevel = 18.0
        const val MapAnimationDuration = 200
    }
}

/**
 * Map type for map selection ui element
 */
sealed class MapSelection {
    data class Google(val type: MapType) : MapSelection()
    data object OsmDroid : MapSelection()
}


/**
 * Providing focus camera position state
 */
enum class OnLocationState {
    /**
     * No need to manipulate the camera
     */
    FREE_ROAM,

    /**
     * Set camera on user position
     */
    ON_USER,

    /**
     * Set camera on center point between the markers of interests
     */
    ON_MEAN_POINT
}

/**
 *  The camera wrapper for internal cameras of the 2 maps
 */
data class FocusCameraPosition(
    val position: GeoPosition = GeoPosition(
        latitude = 0.0,
        longitude = 0.0
    ),
    val zoomLevel: Double = 0.0
)


@HiltViewModel
internal class MapPreferencesViewModel @Inject constructor(
) : ViewModel() {

    var mapType by mutableStateOf<MapSelection>(
        MapSelection.Google(MapType.NORMAL)
    )

    var trackLocationState by mutableStateOf(
        OnLocationState.ON_USER
    )

    var showVirtualFences by mutableStateOf(
        true
    )

    /**
     * Remembers current camera position
     */
    var focusCameraPosition = FocusCameraPosition()
}