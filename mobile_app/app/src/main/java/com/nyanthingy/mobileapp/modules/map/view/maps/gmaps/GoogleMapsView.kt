package com.nyanthingy.mobileapp.modules.map.view.maps.gmaps

import android.annotation.SuppressLint
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.compose.CameraMoveStartedReason
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapEffect
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.rememberCameraPositionState
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import com.nyanthingy.mobileapp.modules.map.viewmodel.FocusCameraPosition
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapsProperties

/**
 * Wrapper composition for gmaps
 * @param mapType type of Google map
 * @param darkMode change to dark mode style
 * @param modifier Modifier
 * @param focusCameraPosition The wrapper camera to map to internal one
 * @param contentPadding When map is overlapped by other elements, should add padding to not hide the Google logo
 * @param onCameraMoving lambda to execute when camera is moving
 * @param animateTransition should the transition to other coordinates be animated
 * @param onCameraChange Notify the internal camera position
 */
@SuppressLint("MissingPermission")
@OptIn(MapsComposeExperimentalApi::class)
@Composable
fun GoogleMapsView(
    mapType: MapType,
    darkMode: Boolean,
    focusCameraPosition: FocusCameraPosition,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(),
    onCameraMoving: () -> Unit = {},
    animateTransition: Boolean = false,
    overlays: List<@Composable ((MapsProjection) -> Unit)>? = null,
    onCameraChange: (FocusCameraPosition) -> Unit = {}
) {
    val cameraPositionState = rememberCameraPositionState {}

    //update cameraPositionState only on value change
    LaunchedEffect(focusCameraPosition) {
        //update the camera when provided position
        val cameraUpdate = CameraUpdateFactory.newCameraPosition(
            CameraPosition.fromLatLngZoom(
                LatLng(
                    focusCameraPosition.position.latitude,
                    focusCameraPosition.position.longitude
                ),
                focusCameraPosition.zoomLevel.toFloat()
            )
        )
        if (animateTransition) {
            cameraPositionState.animate(
                durationMs = MapsProperties.MapAnimationDuration,
                update = cameraUpdate
            )
        } else {
            cameraPositionState.move(
                update = cameraUpdate
            )
        }
    }

    //check for dark mode
    val mapStyleOptions: MapStyleOptions? = if (darkMode)
    //Load the map night mode
        MapStyleOptions.loadRawResourceStyle(
            LocalContext.current, R.raw.google_maps_night_mode
        ) else null

    //@Note because of the nature of recomposition, force recomposition on change and pass the most recent projection matrix
    // better to update it in moving event than in LaunchedEffect
    var projection by remember { mutableStateOf(cameraPositionState.projection) }

    //when camera is moving
    if (cameraPositionState.isMoving) {
        //notify the movement when initiated by user
        if(cameraPositionState.cameraMoveStartedReason == CameraMoveStartedReason.GESTURE)
        {
            onCameraMoving()
        }
        //update projection
        projection = cameraPositionState.projection
    }

    GoogleMap(
        modifier = modifier,
        contentPadding = contentPadding,
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(
            compassEnabled = false,
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false
        ),
        properties = MapProperties(
            isMyLocationEnabled = true,
            mapType = mapType,
            mapStyleOptions = mapStyleOptions
        )
    ) {
        MapEffect { map ->
            // enable myLocationOverlay
            map.isMyLocationEnabled = true
            // limit max zoom
            map.setMaxZoomPreference(
                MapsProperties.MapMaximZoomLevel.toFloat()
            )
            map.isBuildingsEnabled = true
        }

    }

    //Draw overlays
    projection?.let {
        //Draw all overlays
        overlays?.forEach { overlay ->
            overlay(GmapsProjection(it))
        }
    }

    //notify the camera change on each composition
    onCameraChange(
        FocusCameraPosition(
            GeoPosition(
                latitude = cameraPositionState.position.target.latitude,
                longitude = cameraPositionState.position.target.longitude
            ),
            zoomLevel = cameraPositionState.position.zoom.toDouble()
        )
    )

}