package com.nyanthingy.mobileapp.modules.commons.view

import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import kotlin.math.roundToInt

@Composable
fun DraggableDropDown(
    composableList: List<@Composable (() -> Unit)>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    spaceElementsBy: Dp = 5.dp,
    onItemClick: (Int) -> Unit = {}
) {
    //remember dragged position
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    //to set the bounds for the draggable area
    var size by remember { mutableStateOf(IntSize.Zero) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged {
                size = it
            }
    ) {
        DropDownSelect(
            composableList = composableList,
            selectedIndex = selectedIndex,
            modifier = Modifier
                .onSizeChanged {
                    boxSize = it
                }
                //set offset and limit to bounds
                .offset {
                    IntOffset(offsetX.roundToInt().coerceIn(0, size.width - boxSize.width),
                        offsetY.roundToInt().coerceIn(0,size.height - boxSize.height))
                }
                //listen for drag gestures
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        offsetX += dragAmount.x
                        offsetY += dragAmount.y
                    }
                },
            onItemClick = onItemClick
        )
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileListViewPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            DraggableDropDown(composableList = listOf({ Box {} }), selectedIndex = 0)
        }
    }
}
