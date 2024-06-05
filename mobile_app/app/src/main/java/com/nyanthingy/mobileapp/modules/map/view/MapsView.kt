package com.nyanthingy.mobileapp.modules.map.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.map.view.maps.osmdroid.OsmdroidMapView
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapPreferencesViewModel
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapSelection
import com.nyanthingy.mobileapp.modules.permissions.RequireLocation

@Composable
fun MapsView() {
    RequireLocation(whenNotAvailable = {
        Text(text = it.toString())
    }) {

        val viewModel = hiltViewModel<MapPreferencesViewModel>()

        var showMapPreferencesSheet by remember { mutableStateOf(false) }

        if (showMapPreferencesSheet) {
            MapPreferencesBottomSheet(
                onDismiss = {
                    showMapPreferencesSheet = false
                }
            )
        }

        val coords = LatLng(47.147503, 27.604764)

        val cameraPositionState = rememberCameraPositionState {
            position = CameraPosition.fromLatLngZoom(coords, 15f)
        }

        when (viewModel.mapType) {
            is MapSelection.Google -> {
                //Load the map night mode
                val mapStyleOptions: MapStyleOptions? = if (isSystemInDarkTheme())
                    MapStyleOptions.loadRawResourceStyle(
                        LocalContext.current, R.raw.google_maps_night_mode
                    ) else null

                GoogleMap(
                    contentPadding = PaddingValues(20.dp),
                    cameraPositionState = cameraPositionState,
                    uiSettings = MapUiSettings(
                        compassEnabled = false,
                        zoomControlsEnabled = false,
                        myLocationButtonEnabled = false
                    ),
                    properties = MapProperties(
                        isMyLocationEnabled = true,
                        mapType = (viewModel.mapType as MapSelection.Google).type,
                        mapStyleOptions = mapStyleOptions
                    )
                )
            }

            is MapSelection.OsmDroid -> {
                OsmdroidMapView()
            }
        }
        MapButtons(
            modifier = Modifier.padding(horizontal = 10.dp), //Shift a little
            onLocationButtonClick = {

            },
            onMapPreferencesClick = {
                showMapPreferencesSheet = true
            },
        )
    }
}



