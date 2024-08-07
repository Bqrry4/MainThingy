package com.nyanthingy.mobileapp.modules.map.view.overlay

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.view.maps.MapsProjection
import java.lang.Integer.max
import java.lang.Integer.min
import kotlin.math.sqrt

@Composable
fun ProfileOverlay(
    position: GeoPosition,
    projection: MapsProjection,
    image: ImageBitmap,
    radius: Float,
    padding: Int
) {

    val positionPixels by remember(projection, position) {
        mutableStateOf(
            run {
                //Convert to screen coordinates
                val pixelCenter = projection.toPixels(position)
                Offset(pixelCenter.x.toFloat(), pixelCenter.y.toFloat() - radius * sqrt(2f))
            }
        )
    }

    val color = MaterialTheme.colorScheme.background
    Canvas(
        modifier = Modifier.fillMaxSize(),
        onDraw = {

            rotate(45f, positionPixels)
            {
                drawRect(color, positionPixels, Size(radius, radius))
            }

            drawCircle(color, radius, positionPixels)

            // Clip the drawing area to a circle
            clipPath(Path().apply {
                addOval(Rect(positionPixels, radius - padding))
            }) {

                val scaleRatio = (radius * 2) / min(image.width, image.height)

                val dstWidth = image.width * scaleRatio
                val dstHeight = image.height * scaleRatio

                drawImage(
                    image = image,
                    srcOffset = IntOffset.Zero,
                    srcSize = IntSize(image.width, image.height),
                    dstOffset = IntOffset(
                        (positionPixels.x.toInt() - dstWidth / 2).toInt(),
                        (positionPixels.y.toInt() - dstHeight / 2).toInt()
                    ),
                    dstSize = IntSize(
                        dstWidth.toInt(),
                        dstHeight.toInt()
                    ),
                )
            }
        }
    )
}
