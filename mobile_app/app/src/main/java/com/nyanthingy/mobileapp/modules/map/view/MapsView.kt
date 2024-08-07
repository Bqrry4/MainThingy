package com.nyanthingy.mobileapp.modules.map.view

import android.net.Uri
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.modules.commons.extensions.bitmapFromUri
import com.nyanthingy.mobileapp.modules.location.viewmodel.LocationViewModel
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import com.nyanthingy.mobileapp.modules.map.view.maps.gmaps.GoogleMapsView
import com.nyanthingy.mobileapp.modules.map.view.maps.osmdroid.OsmdroidMapView
import com.nyanthingy.mobileapp.modules.map.view.overlay.CircleOverlay
import com.nyanthingy.mobileapp.modules.map.view.overlay.ProfileOverlay
import com.nyanthingy.mobileapp.modules.map.viewmodel.FocusCameraPosition
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapPreferencesViewModel
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapSelection
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapsProperties
import com.nyanthingy.mobileapp.modules.map.viewmodel.OnLocationState
import com.nyanthingy.mobileapp.modules.map.virtualfences.viewmodel.VirtualFencesViewModel
import com.nyanthingy.mobileapp.modules.permissions.RequireLocation
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.modules.server.viewmodel.NetworkDeviceViewModel
import kotlinx.coroutines.launch

@Composable
fun MapsView() {
    RequireLocation(whenNotAvailable = {
        Text(text = it.toString())
    }) {
        val preferences = hiltViewModel<MapPreferencesViewModel>()
        val location = hiltViewModel<LocationViewModel>()
        val virtualFences = hiltViewModel<VirtualFencesViewModel>()
        val profiles = hiltViewModel<ProfileViewModel>()
        val network = hiltViewModel<NetworkDeviceViewModel>()

        val profilesState by profiles.state.collectAsStateWithLifecycle()

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

//                profilesState.profileModelList
//                    //only associated devices can be found on the server
//                    .filter { profile ->
//                        profile.macAddress != null
//                    }.map {
//                        val locationState by network.locationStateFlow(
//                            it.macAddress!!,
//                            it.secret
//                        ).collectAsStateWithLifecycle()
//                    }
//                //find centroid
//
//
//                preferences.focusCameraPosition = FocusCameraPosition(
//                    GeoPosition(
//                        latitude = it.latitude,
//                        longitude = it.longitude,
//                    ),
//                    zoomLevel = MapsProperties.MapMaximZoomLevel
//                )
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
        var overlays: List<@Composable ((MapsProjection) -> Unit)> =
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

                false -> emptyList()
            }


        var historyMode by remember {
            mutableStateOf(false)
        }

        if (historyMode) {

            HistoryModeBottomSheet(
                onDismiss = {
                    historyMode = false
                }
            )
        } else {
            //Add profile overlays
            overlays = overlays.plus(
                profilesState.profileModelList
                    //only associated devices can be found on the server
                    .filter { profile ->
                        profile.macAddress != null
                    }
                    .map { profile ->
                        { projection ->

                            val context = LocalContext.current
                            val bitmap = remember {
                                bitmapFromUri(
                                    context,
                                    uri = Uri.parse(profile.profileImageUri)
                                )!!.asImageBitmap()
                            }

                            val locationState by network.locationStateFlow(
                                profile.macAddress!!,
                                profile.secret
                            ).collectAsStateWithLifecycle()

                            locationState?.let {
                                ProfileOverlay(
                                    position = GeoPosition(
                                        latitude = it.latitude.toDouble(),
                                        longitude = it.longitude.toDouble()
                                    ),
                                    projection = projection,
                                    image = bitmap,
                                    radius = 100f,
                                    padding = 10
                                )
                            }
                        }
                    }
            )
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
                historyMode = !historyMode
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



