package com.nyanthingy.mobileapp.modules.map.view

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Fence
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.maps.android.compose.MapType
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.commons.extensions.dashedBorder
import com.nyanthingy.mobileapp.modules.map.utils.GeoPosition
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapPreferencesViewModel
import com.nyanthingy.mobileapp.modules.map.viewmodel.MapSelection
import com.nyanthingy.mobileapp.modules.map.virtualfences.model.VirtualFenceModel
import com.nyanthingy.mobileapp.modules.map.virtualfences.viewmodel.VirtualFencesViewModel
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPreferencesBottomSheet(
    onDismiss: () -> Unit = {}
) {
    ModalBottomSheet(
        onDismissRequest = { onDismiss() },
        dragHandle = { BottomSheetDefaults.DragHandle() },
    ) {
        MapPreferencesBottomSheetContent(
            onDismiss = onDismiss
        )
    }
}

@Composable
private fun MapPreferencesBottomSheetContent(
    onDismiss: () -> Unit = {}
) {

    val preferences = hiltViewModel<MapPreferencesViewModel>()
    val virtualFences = hiltViewModel<VirtualFencesViewModel>()
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Text(
            text = "Map Preferences",
            fontSize = 20.sp
        )
        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(10.dp)
        ) {
            val darkMode = isSystemInDarkTheme()

            ChooseMapButton(
                text = "Standard",
                image = {
                    Image(
                        painter = painterResource(
                            id = if (darkMode) R.drawable.gmaps_dark
                            else R.drawable.gmaps_light
                        ),
                        contentDescription = "select google maps standard image"
                    )
                },
                onClick = {
                    preferences.mapType = MapSelection.Google(MapType.NORMAL)
                }
            )
            ChooseMapButton(
                text = "Satellite",
                image = {
                    Image(
                        painter = painterResource(
                            id = R.drawable.gmaps_satellite
                        ),
                        contentDescription = "select google maps satellite image"
                    )
                },
                onClick = {
                    preferences.mapType = MapSelection.Google(MapType.SATELLITE)
                }
            )
            ChooseMapButton(
                text = "OpenStreet",
                image = {
                    Image(
                        painter = painterResource(
                            id = if (darkMode) R.drawable.osmdroid_dark
                            else R.drawable.osmdroid_light
                        ),
                        contentDescription = "select openstreetmap image"
                    )
                },
                onClick = {
                    preferences.mapType = MapSelection.OsmDroid
                }
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(20.dp, 10.dp)
        ) {

            SwitchableChoice(
                primaryText = "Virtual Fences",
                description = "Show Safe and Dangerous zones on the map",
                isChecked = preferences.showVirtualFences,
                onCheckedChange = {
                    preferences.showVirtualFences = it
                },
                includeDivider = true,
                icon = Icons.Outlined.Fence
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer)
                .padding(20.dp, 10.dp)
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
                    .dashedBorder(1.dp, MaterialTheme.colorScheme.primary, 8.dp)
                    .clickable(
                        onClick = {
                            coroutineScope.launch {
                                //create an overlay in edit mode
                                virtualFences.insert(
                                    VirtualFenceModel(
                                        name = "vv",
                                        center = preferences.focusCameraPosition.position.copy(),
                                        radius = 150.0
                                    )
                                )
                            }
                            onDismiss()
                        },
                        role = Role.Button,
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material.ripple.rememberRipple(
                            bounded = true
                        )
                    )
            ) {
                Text(text = "Add Virtual Fence")
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Icon(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .dashedBorder(1.dp, MaterialTheme.colorScheme.primary, 32.dp),
                        imageVector = Icons.Rounded.Add,
                        contentDescription = null
                    )
                }
            }
        }
    }
}

@Composable
private fun SwitchableChoice(
    primaryText: String,
    description: String,
    onCheckedChange: (Boolean) -> Unit,
    isChecked: Boolean,
    icon: ImageVector,
    includeDivider: Boolean = false
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    )
    {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface),
            contentAlignment = Alignment.Center
        )
        {
            Icon(
                imageVector = icon,
                contentDescription = null
            )
        }

        Column {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    verticalArrangement = Arrangement.Center,
                )
                {
                    Text(
                        text = primaryText,
                        fontSize = 14.sp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                    Text(
                        text = description,
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 10.sp,
                        style = TextStyle(
                            platformStyle = PlatformTextStyle(
                                includeFontPadding = false
                            )
                        )
                    )
                }
                Box(
                    Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.CenterEnd
                )
                {
                    Switch(
                        checked = isChecked,
                        onCheckedChange = onCheckedChange
                    )
                }
            }
            if (includeDivider) {
                HorizontalDivider(
                    thickness = 2.dp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }
    }
}

@Composable
private fun ChooseMapButton(
    text: String,
    image: @Composable () -> Unit,
    onClick: () -> Unit,
    size: Dp = 64.dp
) {
    Column(
        modifier = Modifier.width(100.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .minimumInteractiveComponentSize()
                .size(size)
                .clip(CircleShape)
                .clickable(
                    onClick = onClick,
                    role = Role.Button,
                    interactionSource = remember { MutableInteractionSource() },
                    indication = androidx.compose.material.ripple.rememberRipple(
                        bounded = false,
                        radius = size / 2
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            image.invoke()
        }
        Text(
            text = text,
            fontSize = 14.sp
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MapPreferencesBottomSheetPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            MapPreferencesBottomSheetContent()
        }
    }
}
