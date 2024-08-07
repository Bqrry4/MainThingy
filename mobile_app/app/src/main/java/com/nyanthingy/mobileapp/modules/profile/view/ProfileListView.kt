package com.nyanthingy.mobileapp.modules.profile.view

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SurroundSound
import androidx.compose.material.icons.outlined.BluetoothAudio
import androidx.compose.material.icons.outlined.Fence
import androidx.compose.material.icons.outlined.LightMode
import androidx.compose.material.icons.outlined.LocationOn
import androidx.compose.material.icons.outlined.SurroundSound
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.nyanthingy.mobileapp.LocalActivity
import com.nyanthingy.mobileapp.modules.ble.client.viewmodel.BleViewModel
import com.nyanthingy.mobileapp.modules.commons.view.DraggableDropDown
import com.nyanthingy.mobileapp.modules.commons.view.SwitchableChoice
import com.nyanthingy.mobileapp.modules.profile.model.ProfileModel
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.modules.profile.viewmodel.SelectedProfile
import com.nyanthingy.mobileapp.modules.server.viewmodel.NetworkDeviceViewModel
import com.nyanthingy.mobileapp.ui.navigation.LeafRoute
import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationViewModel
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState


enum class ConnectionType {
    Near,
    Remote,
    NoConnection
}

@Composable
fun ProfileListView(
    profiles: List<ProfileModel>
) {
//    val profilesVM = hiltViewModel<ProfileViewModel>()
    val ble = hiltViewModel<BleViewModel>()
    val navigation = hiltViewModel<NavigationViewModel>()
    val network = hiltViewModel<NetworkDeviceViewModel>()
//    val ble: BleViewModel = hiltViewModel(LocalActivity.current)
    val profilesVM: ProfileViewModel = hiltViewModel(LocalActivity.current)

    println(profiles)

    var connectionType by remember {
        mutableStateOf(ConnectionType.Remote)
    }

    if (profilesVM.selectedProfile!!.profile.macAddress == null) {
        connectionType = ConnectionType.NoConnection
    } else {
        //When service is available
        val availabilityState by ble.availabilityState.collectAsStateWithLifecycle()
        if (availabilityState) {
            val connectionState = ble.connectionState(
                profilesVM.selectedProfile!!.profile.macAddress!!
            )
            //when device is registered
            connectionState?.let {
                val connection by it.collectAsStateWithLifecycle()
                connectionType = when (connection) {
                    GattConnectionState.STATE_CONNECTED -> {
                        ConnectionType.Near
                    }

                    else -> {
                        ConnectionType.Remote
                    }
                }
            }
        }
    }


    var dropDownItems: List<@Composable () -> Unit> = profiles.map {
        {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .border(
                        BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                        CircleShape
                    )
            )
            {
                AsyncImage(
                    model = it.profileImageUri,
                    contentScale = ContentScale.Crop,
                    contentDescription = null
                )
            }
        }
    }

    val addProfileDropDownItem: @Composable () -> Unit = {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .border(
                    BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                    CircleShape
                )
        )
        {
            Icon(
                modifier = Modifier.size(48.dp),
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        }
    }

    dropDownItems = dropDownItems.plus(addProfileDropDownItem)

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .fillMaxSize()
    ) {
        CoverAndProfileImage(
            modifier = Modifier,
            coverImage = {
                AsyncImage(
                    model = profilesVM.selectedProfile!!.profile.coverImageUri,
                    contentDescription = "coverImage",
                    modifier = Modifier
                        .fillMaxSize()
                        .border(BorderStroke(1.dp, MaterialTheme.colorScheme.primary))
                        .background(MaterialTheme.colorScheme.background),
                    contentScale = ContentScale.Crop
                )
            },
            profileImage = {
                AsyncImage(
                    model = profilesVM.selectedProfile!!.profile.profileImageUri,
                    contentDescription = "profileImage",
                    modifier = Modifier
                        .fillMaxSize()
                        .border(
                            BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                            CircleShape
                        )
                        .background(MaterialTheme.colorScheme.background),
                    contentScale = ContentScale.Crop
                )
            },
//            onCoverClick = {
//                coverImagePicker.launch(
//                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                )
//            },
//            onProfileClick = {
//                profileImagePicker.launch(
//                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
//                )
//            }
        )
        BasicTextField(
            value = profilesVM.selectedProfile!!.profile.name,
            onValueChange = {},
            textStyle = TextStyle(textAlign = TextAlign.Center)
        )

//        Row(
//            horizontalArrangement = Arrangement.SpaceBetween,
//        ) {
//            ProfileStatusItem(ImageVector.vectorResource(R.drawable.gps), "GPS", true)
//            Spacer(Modifier.width(5.dp))
//            ProfileStatusItem(Icons.Default.NetworkCell, "Online", true)
//            Spacer(Modifier.width(5.dp))
//            ProfileStatusItem(ImageVector.vectorResource(R.drawable.battery), "99%", false)
//        }

        when (connectionType) {
            ConnectionType.Near -> {
                Text(text = "Connected over BLE")
            }

            ConnectionType.Remote -> {
                Text(text = "Might be connected Remote")
            }

            ConnectionType.NoConnection -> {
                Text(text = "Device is not associated")
            }
        }

        if (connectionType != ConnectionType.NoConnection) {

            //Get battery only on close connection for now
            if (connectionType == ConnectionType.Near) {
                val battery by ble.getBatteryState(profilesVM.selectedProfile!!.profile.macAddress!!)
                    .collectAsStateWithLifecycle()
                battery?.let {
                    Text(text = "$it %")
                }
            }


            var radarState by remember {
                mutableStateOf(false)
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
                    .padding(20.dp, 10.dp)
            ) {

                var ledState by remember {
                    mutableStateOf(false)
                }
                SwitchableChoice(
                    primaryText = "Led",
                    description = "Turn On/Off the Led",
                    isChecked = ledState,
                    onCheckedChange = {
                        ledState = it

                        when (connectionType) {
                            ConnectionType.Near -> {
                                ble.setLedState(
                                    profilesVM.selectedProfile!!.profile.macAddress!!,
                                    it
                                )
                            }

                            ConnectionType.Remote -> {
                                network.setLedState(
                                    profilesVM.selectedProfile!!.profile.macAddress!!,
                                    profilesVM.selectedProfile!!.profile.secret,
                                    it
                                )
                            }

                            else -> {}
                        }
                    },
                    includeDivider = true,
                    icon = Icons.Outlined.LightMode
                )

                var buzzerState by remember {
                    mutableStateOf(false)
                }
                SwitchableChoice(
                    primaryText = "Buzzer",
                    description = "Turn On/Off the Buzzer",
                    isChecked = buzzerState,
                    onCheckedChange = {
                        buzzerState = it

                        when (connectionType) {
                            ConnectionType.Near -> {
                                ble.setBuzzerState(
                                    profilesVM.selectedProfile!!.profile.macAddress!!,
                                    it
                                )
                            }

                            ConnectionType.Remote -> {
                                network.setBuzzerState(
                                    profilesVM.selectedProfile!!.profile.macAddress!!,
                                    profilesVM.selectedProfile!!.profile.secret,
                                    it
                                )
                            }

                            else -> {}
                        }
                    },
                    includeDivider = true,
                    icon = Icons.Outlined.SurroundSound
                )

                var gnssMode by remember {
                    mutableStateOf(false)
                }
                SwitchableChoice(
                    primaryText = "GNSS Live mode",
                    description = "Only over network will be sent",
                    isChecked = gnssMode,
                    onCheckedChange = {
                        gnssMode = it

                        network.setGNSSModeState(
                            profilesVM.selectedProfile!!.profile.macAddress!!,
                            profilesVM.selectedProfile!!.profile.secret,
                            it
                        )
                    },
                    includeDivider = true,
                    icon = Icons.Outlined.LocationOn
                )

                SwitchableChoice(
                    primaryText = "Radar",
                    description = "Use ble signal as radar",
                    isChecked = radarState,
                    onCheckedChange = {
                        radarState = it
                    },
                    includeDivider = true,
                    icon = Icons.Outlined.BluetoothAudio
                )
            }
            if (radarState) {
                val signalState = ble.signalStrength(
                    profilesVM.selectedProfile!!.profile.macAddress!!
                )
                if (signalState == null) {
                    Text(text = "Device not registered")
                } else {
                    val signal by signalState.collectAsStateWithLifecycle()
                    Text(text = signal)
                }

            }

        }
    }



    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = Modifier
            .padding(20.dp)
    ) {
        IconButton(onClick = {
            navigation.navigateTo(LeafRoute.Scanner.route)
        }) {
            Icon(
                Icons.Default.AddLink,
                contentDescription = "bindDevice"
            )
        }
    }


    DraggableDropDown(
        modifier = Modifier.zIndex(2f),
        composableList = dropDownItems,
        selectedIndex = profilesVM.selectedProfile!!.index,
        onItemClick = {
            //The last one will be the add profile
            if (it == dropDownItems.count() - 1) {
                navigation.navigateTo(LeafRoute.AddProfile.route)
            } else {
                profilesVM.selectedProfile = SelectedProfile(profiles[it], it)
            }
        }
    )
}


@Composable
fun ProfileStatusItem(
    icon: ImageVector,
    text: String,
    isActive: Boolean,
    modifier: Modifier = Modifier,
    activeColor: Color = Color.Green,
    inactiveColor: Color = Color.Red
) {

    Card(
        modifier = modifier
            .background(MaterialTheme.colorScheme.primary)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = modifier.padding(horizontal = 40.dp, vertical = 2.dp)
        ) {
            Box {
                Icon(
                    icon,
                    text,
                    modifier = modifier
                        .size(32.dp)
                )
                Box(
                    modifier = modifier
                        .align(Alignment.TopEnd)
                        .offset(10.dp)
                ) {
                    StatusDot(
                        color = if (isActive) activeColor else inactiveColor,
                        size = 8.dp
                    )
                }
            }
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {

                Text(
                    text = text,
                    fontSize = 10.sp
                )
            }

        }
    }
}

@Composable
fun StatusDot(
    color: Color,
    size: Dp,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .size(size)
            .background(color)
    )
}


@Preview(showBackground = true)
@Composable
fun ProfileListViewPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            ProfileListView(
                listOf(
                    ProfileModel(
                        name = "Cat1",
                        profileImageUri = "null"
                    ),
                    ProfileModel(
                        name = "Cat2",
                        profileImageUri = "null"
                    )
                )
            )
        }
    }
}
