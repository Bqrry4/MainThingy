package com.nyanthingy.mobileapp.modules.commons.view

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.Layout

@Composable
fun OverlappingBoxes(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = content,
    ) { measurables, constraints ->
        val largeBox = measurables[0]
        val smallBox = measurables[1]
        val looseConstraints = constraints.copy(
            minWidth = 0,
            minHeight = 0,
        )
        val largePlaceable = largeBox.measure(looseConstraints)
        val smallPlaceable = smallBox.measure(looseConstraints)
        layout(
            width = constraints.maxWidth,
            height = largePlaceable.height + smallPlaceable.height / 2,
        ) {
            largePlaceable.placeRelative(
                x = 0,
                y = 0,
            )
            smallPlaceable.placeRelative(
                x = (constraints.maxWidth - smallPlaceable.width) / 2,
                y = largePlaceable.height - smallPlaceable.height / 2
            )
        }
    }
}