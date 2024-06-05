package com.nyanthingy.mobileapp.modules.commons.view

import android.util.Log
import androidx.compose.foundation.border
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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import com.nyanthingy.mobileapp.modules.database.model.ProfileEntry
import com.nyanthingy.mobileapp.modules.profile.view.ProfileListView
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
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }
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
                .offset {
                    IntOffset(offsetX.roundToInt().coerceIn(0, size.width - boxSize.width),
                        offsetY.roundToInt().coerceIn(0,size.height - boxSize.height))
                }
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
