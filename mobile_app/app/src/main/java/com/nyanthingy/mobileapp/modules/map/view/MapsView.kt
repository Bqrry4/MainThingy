package com.nyanthingy.mobileapp.modules.map.view

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.modules.location.viewmodel.LocationViewModel
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import com.nyanthingy.mobileapp.modules.map.view.maps.gmaps.GoogleMapsView
import com.nyanthingy.mobileapp.modules.map.view.maps.osmdroid.OsmdroidMapView
import com.nyanthingy.mobileapp.modules.map.viewmodel.FocusCameraPosition
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.view.overlay.CircleOverlay
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapPreferencesViewModel
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapSelection
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapsProperties
import com.nyanthingy.mobileapp.modules.map.viewmodel.OnLocationState
import com.nyanthingy.mobileapp.modules.map.virtualfences.viewmodel.VirtualFencesViewModel
import com.nyanthingy.mobileapp.modules.permissions.RequireLocation
import kotlinx.coroutines.launch

@Composable
fun MapsView() {
    RequireLocation(whenNotAvailable = {
        Text(text = it.toString())
    }) {
        val preferences = hiltViewModel<MapPreferencesViewModel>()
        val location = hiltViewModel<LocationViewModel>()
        val virtualFences = hiltViewModel<VirtualFencesViewModel>()

        //change state when user moves the map
        val onCameraMoving = {
            preferences.trackLocationState = OnLocationState.FREE_ROAM
        }

        //persist camera on map dispose
        val persistFocusCamera: (FocusCameraPosition) -> Unit = {
            preferences.focusCameraPosition = it
        }

        //Behaviour for trackLocation action
        when (preferences.trackLocationState) {
            OnLocationState.ON_USER -> {
                val currentLocation by location.currentLocation.collectAsStateWithLifecycle()
                //return null or the value
                currentLocation?.let {
                    preferences.focusCameraPosition = FocusCameraPosition(
                        GeoPosition(
                            latitude = it.latitude,
                            longitude = it.longitude,
                        ),
                        zoomLevel = MapsProperties.MapMaximZoomLevel
                    )
                }
            }

            OnLocationState.ON_MEAN_POINT -> {
                //TODO:
            }

            else -> {}
        }

        var showMapPreferencesSheet by remember { mutableStateOf(false) }
        if (showMapPreferencesSheet) {
            MapPreferencesBottomSheet(
                onDismiss = {
                    showMapPreferencesSheet = false
                }
            )
        }

        val virtualFencesState by virtualFences.state.collectAsStateWithLifecycle()
        //build the overlay list
        val overlays: List<@Composable ((MapsProjection) -> Unit)>? =
            when (preferences.showVirtualFences) {

                true -> virtualFencesState.virtualFencesList.map { virtualFence ->
                    { projection ->
                        val coroutineScope = rememberCoroutineScope()

                        var editMode by remember {
                            mutableStateOf(false)
                        }

                        CircleOverlay(
                            center = virtualFence.center,
                            radius = virtualFence.radius,
                            projection = projection,
                            editMode = editMode,
                            onEditModeChange = {
                                editMode = it
                            },
                            onModified = { position, radius ->
                            coroutineScope.launch {
                                virtualFences.update(
                                    virtualFence.copy(
                                        center = position,
                                        radius = radius
                                    )
                                )
                            }
                        }
                        )
                    }
                }


//                    listOf(
//                    {
//                        var editMode by remember {
//                            mutableStateOf(false)
//                        }
//                        var position by remember {
//                            mutableStateOf(GeoPosition(47.154005998392535, 27.593949871427842))
//                        }
//
//                        var radius by remember {
//                            mutableDoubleStateOf(150.0)
//                        }
//                        CircleOverlay(
//                            center = position,
//                            radius = radius,
//                            projection = it,
//                            editMode = editMode,
//                            onEditModeChange = { editModeValue ->
//                                editMode = editModeValue
//                            },
//                            onModified = { geoPosition, geoRadius ->
//                                position = geoPosition
//                                radius = geoRadius
//                            }
//                        )
//                    },
//                    {
//                        CircleOverlay(
//                            center = GeoPosition(47.157639582381286, 27.604728770613246),
//                            radius = 100.0,
//                            projection = it
//                        )
//                    },
//                )

                false -> null
            }

        //Render maps
        val darkMode = isSystemInDarkTheme()
        when (preferences.mapType) {
            is MapSelection.Google -> {
                GoogleMapsView(
                    mapType = (preferences.mapType as MapSelection.Google).type,
                    darkMode = darkMode,
                    focusCameraPosition = preferences.focusCameraPosition,
                    contentPadding = PaddingValues(20.dp),
                    onCameraMoving = onCameraMoving,
                    onCameraChange = persistFocusCamera,
                    overlays = overlays,
                )
            }

            is MapSelection.OsmDroid -> {
                OsmdroidMapView(
                    focusCameraPosition = preferences.focusCameraPosition,
                    darkMode = darkMode,
                    onCameraMoving = onCameraMoving,
                    onCameraChange = persistFocusCamera,
                    overlays = overlays
                )
            }
        }

        //Render buttons
        MapButtons(
            modifier = Modifier.padding(horizontal = 10.dp), //Shift a little
            locationButtonState = preferences.trackLocationState,
            onHistoryButtonClick = {

            },
            onLocationButtonClick = {
                preferences.trackLocationState = OnLocationState.ON_USER
            },
            onMapPreferencesClick = {
                showMapPreferencesSheet = true
            },
        )
    }
}



