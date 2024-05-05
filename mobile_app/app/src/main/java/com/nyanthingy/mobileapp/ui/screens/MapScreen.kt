package com.nyanthingy.mobileapp.ui.screens

import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import com.google.maps.android.compose.GoogleMap
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView

@Composable
fun MapScreen() {
    NyanthingyAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            //OsmdroidMapView()
            GoogleMap()
        }
    }
}

@Composable
fun OsmdroidMapView(
    modifier: Modifier = Modifier,
) {
    AndroidView(
        factory = { context ->
            MapView(context).also {

                //Take tiles from network
                it.setUseDataConnection(true)

                val tileSource = TileSourceFactory.MAPNIK
                it.setTileSource(tileSource)

                it.isVerticalMapRepetitionEnabled = false
                it.isHorizontalMapRepetitionEnabled = true
                it.setScrollableAreaLimitLatitude(
                    MapView.getTileSystem().maxLatitude,
                    MapView.getTileSystem().minLatitude,
                    0
                )

                it.maxZoomLevel = tileSource.maximumZoomLevel.toDouble()
                it.minZoomLevel = 3.0
                it.controller.setZoom(3.0)
                it.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                it.scrollX

                //it.controller.setCenter(GeoPoint(	47.1672, 27.6083))
                //it.controller.animateTo(GeoPoint(	47.1672, 27.6083))
                //it.controller.zoomTo(17.0)
            }
        },
        modifier = modifier.fillMaxHeight()
    )
}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MapScreen()
        }
    }
}