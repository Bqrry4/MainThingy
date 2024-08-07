package com.nyanthingy.mobileapp.modules.commons.view

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.animation.with
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Popup
import androidx.compose.ui.window.PopupProperties



/**
 * DropDown of composables
 */
@Composable
fun DropDownSelect(
    composableList: List<@Composable (() -> Unit)>,
    selectedIndex: Int,
    modifier: Modifier = Modifier,
    spaceElementsBy: Dp = 5.dp,
    onItemClick: (Int) -> Unit = {}
) {

    var showDropdown by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier.animateContentSize(
            animationSpec = tween(
                durationMillis = 300, easing = LinearOutSlowInEasing
            )
        )
    )
    {
        if (!showDropdown) {
            Box(
                modifier = Modifier
                    .clickable { showDropdown = true },
                contentAlignment = Alignment.Center
            ) {
                composableList[0].invoke()
            }
        } else {
            Popup(properties = PopupProperties(
                excludeFromSystemGesture = true,
            ), onDismissRequest = { showDropdown = false }) {

                Column(
                    modifier = Modifier
                        .verticalScroll(state = scrollState),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(spaceElementsBy)
                ) {

                    composableList.onEachIndexed { index, composable ->
                        Box(
                            modifier = Modifier
                                .clickable {
                                    onItemClick(index)
                                    showDropdown = false
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            composable()
                        }
                    }
                }
            }
        }
    }
}

///**
// * DropDown of composables
// */
//@Composable
//fun DropDownSelect(
//    composableList: List<@Composable (() -> Unit)>,
//    selectedIndex: Int,
//    modifier: Modifier = Modifier,
//    spaceElementsBy: Dp = 5.dp,
//    onItemClick: (Int) -> Unit = {}
//) {
//
//    var showDropdown by remember { mutableStateOf(false) }
//    val scrollState = rememberScrollState()
//
//    AnimatedContent(
//        targetState = showDropdown,
//        transitionSpec = {
//            fadeIn(animationSpec = tween(150, 150)) togetherWith
//                    fadeOut(animationSpec = tween(150)) using
//                    SizeTransform { initialSize, targetSize ->
//                        if (targetState) {
//                            keyframes {
//                                // Expand horizontally first.
//                                IntSize(targetSize.width, initialSize.height) at 150
//                                durationMillis = 300
//                            }
//                        } else {
//                            keyframes {
//                                // Shrink vertically first.
//                                IntSize(initialSize.width, targetSize.height) at 150
//                                durationMillis = 300
//                            }
//                        }
//                    }
//        }, label = ""
//    )
//    { expand ->
//        Column(
////            modifier = modifier
////                .animateContentSize(
////                    animationSpec = tween(
////                        durationMillis = 300, easing = LinearOutSlowInEasing
////                    )
////                )
//        )
//        {
//            if (!expand) {
//                Box(
//                    modifier = Modifier
//                        .clickable { showDropdown = true },
//                    contentAlignment = Alignment.Center
//                ) {
//                    composableList[0].invoke()
//                }
//            } else {
//                Popup(properties = PopupProperties(
//                    excludeFromSystemGesture = true,
//                ), onDismissRequest = { showDropdown = false }) {
//
//                    Column(
//                        modifier = Modifier
//                            .verticalScroll(state = scrollState),
//                        horizontalAlignment = Alignment.CenterHorizontally,
//                        verticalArrangement = Arrangement.spacedBy(spaceElementsBy)
//                    ) {
//
//                        composableList.onEachIndexed { index, composable ->
//                            Box(
//                                modifier = Modifier
//                                    .clickable {
//                                        onItemClick(index)
//                                        showDropdown = false
//                                    },
//                                contentAlignment = Alignment.Center
//                            ) {
//                                composable()
//                            }
//                        }
//                    }
//                }
//            }
//        }
//
//    }
//}