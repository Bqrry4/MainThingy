package com.nyanthingy.mobileapp.modules.map.view.maps.osmdroid

import android.content.Context
import android.graphics.Color
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun OsmdroidMapView(
    modifier: Modifier = Modifier,
) {

    val context = LocalContext.current
    val darkMode = isSystemInDarkTheme()

    val mapView = remember { configMapView(context, darkMode) }

    AndroidView(
        modifier = modifier,
        factory = {
            mapView
        })
    //MapLifecycle(mapView)
}

fun configMapView(context: Context, darkMode: Boolean): MapView {

    val mapView = MapView(context)

    //Take tiles from network
    mapView.setUseDataConnection(true)

    //Set tile source
    val tileSource = TileSourceFactory.MAPNIK
    mapView.setTileSource(tileSource)

    //Limit scroll
    mapView.isVerticalMapRepetitionEnabled = false
    mapView.isHorizontalMapRepetitionEnabled = true
    mapView.setScrollableAreaLimitLatitude(
        MapView.getTileSystem().maxLatitude,
        MapView.getTileSystem().minLatitude,
        0
    )

    //Limit zoom
    mapView.maxZoomLevel = tileSource.maximumZoomLevel.toDouble()
    mapView.minZoomLevel = 3.0
    mapView.controller.setZoom(3.0)

    //Enable multitouch and disable zoom buttons
    mapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
    mapView.setMultiTouchControls(true)

    val mMyLocationOverlay = MyLocationNewOverlay(mapView)
    mMyLocationOverlay.enableMyLocation()
    mapView.overlays.add(mMyLocationOverlay)

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
        val cRed = 0.5f
        val cGreen = 0.6f
        val cBlue = 1f
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