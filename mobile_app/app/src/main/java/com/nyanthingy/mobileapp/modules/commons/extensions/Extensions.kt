package com.nyanthingy.mobileapp.modules.commons.extensions

import android.annotation.SuppressLint
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Clears the local focus on region tap
 */
fun Modifier.clearFocusOnTap(): Modifier = composed {
    val focusManager = LocalFocusManager.current
    this.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(pass = PointerEventPass.Initial)
            val upEvent = waitForUpOrCancellation(pass = PointerEventPass.Initial)
            if (upEvent != null) {
                focusManager.clearFocus()
            }
        }
    }
}

/**
 * Ignores parent padding by dp
 */
fun Modifier.ignoreParentPadding(horizontal: Dp = 0.dp, vertical: Dp = 0.dp): Modifier {
    return this.layout { measurable, constraints ->
        val overridenWidth = constraints.maxWidth + 2 * horizontal.roundToPx()
        val overridenHeight = constraints.maxHeight + 2 * vertical.roundToPx()

        val placeable = measurable.measure(
            constraints.copy(
                maxWidth = overridenWidth,
                maxHeight = overridenHeight
            )
        )
        layout(placeable.width, placeable.height) {
            placeable.place(0, 0)
        }
    }
}