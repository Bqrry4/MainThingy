package com.nyanthingy.mobileapp.modules.map.view.maps.osmdroid

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.util.Log
import android.view.MotionEvent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import com.nyanthingy.mobileapp.modules.map.viewmodel.FocusCameraPosition
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapsProperties
import org.osmdroid.events.MapListener
import org.osmdroid.events.ScrollEvent
import org.osmdroid.events.ZoomEvent
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.gestures.RotationGestureOverlay
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.cos
import kotlin.math.sin


/**
 * Wrapper composition for gmaps
 * @param darkMode change to dark mode style
 * @param modifier Modifier
 * @param focusCameraPosition The wrapper camera to map to internal one
 * @param onCameraMoving lambda to execute when camera is moving
 * @param animateTransition should the transition to other coordinates be animated
 * @param onCameraChange Notify the internal camera position
 * @note There might happen a bug if the onCameraMoving is provided dynamically, as it is registered on mapview creation and ignored further
 * @note No need to remember and manage the controller state as it is done in a level above in focusCameraPosition
 */
@Composable
fun OsmdroidMapView(
    darkMode: Boolean,
    modifier: Modifier = Modifier,
    focusCameraPosition: FocusCameraPosition,
    onCameraMoving: () -> Unit = {},
    animateTransition: Boolean = false,
    overlays: List<@Composable ((MapsProjection) -> Unit)>? = null,
    onCameraChange: (FocusCameraPosition) -> Unit = {}
) {

    val context = LocalContext.current
    //avoid recreating the instance on every
    val mapView = remember {
        configMapView(context, darkMode, onCameraMoving)
    }


    //move camera only on value change
    LaunchedEffect(focusCameraPosition) {
        val position = GeoPoint(
            focusCameraPosition.position.latitude,
            focusCameraPosition.position.longitude
        )

        if (animateTransition) {
            mapView.controller.animateTo(
                position,
                focusCameraPosition.zoomLevel,
                MapsProperties.MapAnimationDuration.toLong()
            )
        } else {
            //Note in this version(6.18), osmdroid seems to have a bug for controller.setCenter, where on first set the latitude is always 0
            //this might be related that is called in the compose context
            mapView.controller.animateTo(
                position,
                focusCameraPosition.zoomLevel,
                0L
            )
        }
    }

    MapLifecycle(mapView)

    AndroidView(
        modifier = modifier,
        factory = {
            mapView
        })

    //@Note mapview has its internal state, so its needed to listen for changes and recompose the overlays with the last known projection
    var projection by remember { mutableStateOf(mapView.projection) }
    mapView.addMapListener(object : MapListener {
        override fun onScroll(event: ScrollEvent?): Boolean {
            projection = mapView.projection
            return false
        }

        override fun onZoom(event: ZoomEvent?): Boolean {
            projection = mapView.projection
            return false
        }
    })

    //Draw all overlays
    overlays?.forEach { overlay ->
        overlay(OsmdroidProjection(projection))
    }

    //notify the camera change on each composition
    onCameraChange(
        FocusCameraPosition(
            GeoPosition(
                latitude = mapView.mapCenter.latitude,
                longitude = mapView.mapCenter.longitude
            ),
            zoomLevel = mapView.zoomLevelDouble
        )
    )
}

@SuppressLint("ClickableViewAccessibility")
private fun configMapView(
    context: Context,
    darkMode: Boolean,
    onCameraMoving: () -> Unit = {}
): MapView {

    val mapView = MapView(context)

    //Take tiles from network
    mapView.setUseDataConnection(true)

    //Set tile source
    val tileSource = TileSourceFactory.MAPNIK

    mapView.setTileSource(tileSource)
    mapView.isTilesScaledToDpi = true

    //Limit scroll
    mapView.isVerticalMapRepetitionEnabled = false
    mapView.isHorizontalMapRepetitionEnabled = true
    mapView.setScrollableAreaLimitLatitude(
        MapView.getTileSystem().maxLatitude,
        MapView.getTileSystem().minLatitude,
        0
    )

    //Limit zoom
    //mapView.maxZoomLevel = tileSource.maximumZoomLevel.toDouble()
    mapView.maxZoomLevel = MapsProperties.MapMaximZoomLevel //use a custom one
    mapView.minZoomLevel = 3.0

    //Enable multitouch and disable zoom buttons
    mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
    mapView.setMultiTouchControls(true)

    //register listener for camera moving
    mapView.setOnTouchListener { _, event ->
        if (event.action == MotionEvent.ACTION_MOVE)
            onCameraMoving()
        false
    }

    //The MyLocation overlay
    val locationOverlay = MyLocationNewOverlay(mapView)

    locationOverlay.enableMyLocation()
    val db = ContextCompat.getDrawable(context, R.drawable.map_location)

    val bit = Bitmap.createBitmap(
        db!!.intrinsicWidth, db.intrinsicHeight, Bitmap.Config.ARGB_8888
    ).also {
        val canvas = Canvas(it)
        db.setBounds(0, 0, canvas.width, canvas.height)
        db.draw(canvas)
    }

    locationOverlay.setPersonIcon(bit)
    //set anchor on center
    locationOverlay.setPersonAnchor(0.5f, 0.5f)

    //Enable map rotation by multitouch
    val rotationGestureOverlay = RotationGestureOverlay(mapView)
    rotationGestureOverlay.setEnabled(true)
    mapView.overlays.add(locationOverlay)
    mapView.overlays.add(rotationGestureOverlay)

    //Enable dark mode
    //Made for the Mapnik tile source!!
    if (darkMode) {
        //set background color
        mapView.overlayManager.tilesOverlay.loadingBackgroundColor = Color.BLACK

        //negate matrix
        val negateMatrix = ColorMatrix(
            floatArrayOf(
                -1.0f, 0.0f, 0.0f, 0.0f, 255f,
                0.0f, -1.0f, 0.0f, 0.0f, 255f,
                0.0f, 0.0f, -1.0f, 0.0f, 255f,
                0.0f, 0.0f, 0.0f, 1.0f, 0.0f
            )
        )

        //Hue rotation matrix
        //taken from https://learn.microsoft.com/en-us/windows/win32/direct2d/hue-rotate
        val hueAngle = 50 //seems

        val hueRadians = hueAngle * 3.14f / 180
        val cosHue = cos(hueRadians)
        val sinHue = sin(hueRadians)

        val hueMatrix = ColorMatrix(
            floatArrayOf(
                0.213f + cosHue * 0.787f - sinHue * 0.213f,
                0.213f - cosHue * 0.213f + sinHue * 0.143f,
                0.213f - cosHue * 0.213f - sinHue * 0.787f,
                0f,
                0f,

                0.715f - cosHue * 0.715f - sinHue * 0.715f,
                0.715f + cosHue * 0.285f + sinHue * 0.140f,
                0.715f - cosHue * 0.715f + sinHue * 0.715f,
                0f,
                0f,

                0.072f - cosHue * 0.072f + sinHue * 0.928f,
                0.072f - cosHue * 0.072f - sinHue * 0.283f,
                0.072f + cosHue * 0.928f + sinHue * 0.072f,
                0f,
                0f,

                0f,
                0f,
                0f,
                1f,
                0f,

                0f,
                0f,
                0f,
                0f,
                1f
            )
        )

        //adjust contrast
        val cRed = 0.3f
        val cGreen = 0.5f
        val cBlue = 0.8f
        val t = 0.01f
        val contrastMatrix = ColorMatrix(
            floatArrayOf(
                cRed, 0f, 0f, 0f, 0f,
                0f, cGreen, 0f, 0f, 0f,
                0f, 0f, cBlue, 0f, 0f,
                0f, 0f, 0f, 1f, 0f,
                t, t, t, 0f, 1f
            )
        )

        //apply the negate->hue->contrast
        negateMatrix.postConcat(hueMatrix)
        negateMatrix.postConcat(contrastMatrix)

        val filter = ColorMatrixColorFilter(negateMatrix)
        mapView.overlayManager.tilesOverlay.setColorFilter(filter)
    }

    return mapView
}