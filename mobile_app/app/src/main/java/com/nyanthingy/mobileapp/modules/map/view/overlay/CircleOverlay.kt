package com.nyanthingy.mobileapp.modules.map.view.overlay

import android.graphics.Point
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import com.nyanthingy.mobileapp.modules.commons.extensions.detectPressGesturesUnconsumed
import com.nyanthingy.mobileapp.modules.commons.extensions.onPointerEventAsync
import com.nyanthingy.mobileapp.modules.map.utils.shiftLatitudeByMeters
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.utils.minus
import kotlin.math.abs
import kotlin.math.hypot

/**
 * @param center Center of the circle
 * @param radius in meters
 * @param projection The projection matrix
 * @param editMode init editMode
 * @param onEditModeChange notify about editModeChange intention
 * @param onModified notify about modified state
 */
@Composable
fun CircleOverlay(
    center: GeoPosition,
    radius: Double,
    projection: MapsProjection,
    editMode: Boolean = false,
    onEditModeChange: (Boolean) -> Unit = {},
    onModified: (GeoPosition, Double) -> Unit = { _, _ -> },
) {

    //flag for notifying the update after exiting the editMode
    var wasModified = remember { false }

    // The drawable circle properties
    // @Note use internal pixel state to not apply the projection matrix at each gesture apply change
    var circleCenter by remember(projection) {
        mutableStateOf(
            run {
                //Convert to screen coordinates
                val pixelCenter = projection.toPixels(center)
                Offset(pixelCenter.x.toFloat(), pixelCenter.y.toFloat())
            }
        )
    }
    var circleRadius by remember(projection) {
        mutableFloatStateOf(
            run {
                //find the horizontal line radius point
                val radiusPoint = GeoPosition(
                    latitude = shiftLatitudeByMeters(center.latitude, radius),
                    longitude = center.longitude
                )
                val pixelRadiusPoint = projection.toPixels(radiusPoint)

                val dy = (circleCenter.y - pixelRadiusPoint.y)
                val dx = (circleCenter.x - pixelRadiusPoint.x)
                hypot(dx, dy)
            }
        )
    }

    println(circleCenter.toString())
    println(circleRadius.toString())
    println(center)

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .then(when (editMode) {
                true -> Modifier
                    //Detect tap outside to dismiss
                    .pointerInput(Unit) {
                        detectTapGestures { position ->
                            //Interested only in outside click
                            if (pointInCircle(circleCenter, circleRadius, position))
                                return@detectTapGestures

                            //exit from edit mode
                            onEditModeChange(false)

                            //If it was not modified return
                            if (!wasModified)
                                return@detectTapGestures

                            //Otherwise notify changes
                            val modifiedCenter = projection.fromPixels(
                                Point(
                                    (circleCenter.x).toInt(),
                                    (circleCenter.y).toInt()
                                )
                            )
                            val modifiedHorizontalPoint = projection.fromPixels(
                                Point(
                                    (circleCenter.x + circleRadius).toInt(),
                                    (circleCenter.y).toInt()
                                )
                            )
                            onModified(
                                modifiedCenter,
                                modifiedCenter - modifiedHorizontalPoint
                            )
                        }
                    }
                    //Detect zoom and drag
                    .pointerInput(Unit) {
                        //when in edit mode consume the event
                        detectTransformGestures(false) { centroid, pan, zoom, _ ->
                            circleRadius *= zoom
                            if (pointInCircle(circleCenter, circleRadius, centroid)) {
                                circleCenter += pan
                            }
                            wasModified = true
                        }
                    }

                false -> Modifier
                    //share the event in non edit mode
                    .onPointerEventAsync(
                        viewConfiguration = LocalViewConfiguration.current,
                        density = LocalDensity.current,
                        block = {
                            detectPressGesturesUnconsumed(
                                onLongPress = { position, _ ->
                                    if (pointInCircle(
                                            circleCenter,
                                            circleRadius,
                                            position
                                        )
                                    ) {
                                        onEditModeChange(true)
                                    }
                                }
                            )
                        }
                    )
            }),
        onDraw = {
            drawCircle(
                color = when (editMode) {
                    true -> Color(42, 255, 116, 32)
                    false -> Color(42, 255, 116, 64)
                },
                center = circleCenter,
                radius = circleRadius
            )
        }
    )
}

fun pointInCircle(center: Offset, radius: Float, point: Offset): Boolean {

    val dx: Float = abs(center.x - point.x)
    if (dx > radius) return false

    val dy: Float = abs(center.y - point.y)
    if (dy > radius) return false

    return (dx + dy <= radius) && (dx * dx + dy * dy <= radius * radius)
}