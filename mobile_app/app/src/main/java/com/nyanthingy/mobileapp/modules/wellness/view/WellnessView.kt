package com.nyanthingy.mobileapp.modules.wellness.view

import android.app.Activity
import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.nyanthingy.mobileapp.LocalActivity
import com.nyanthingy.mobileapp.MainActivity
import com.nyanthingy.mobileapp.modules.ble.client.viewmodel.BleViewModel
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState

@Composable
fun WellnessView() {

//    val profilesVM = hiltViewModel<ProfileViewModel>()
//    val ble = hiltViewModel<BleViewModel>()
    val ble: BleViewModel = hiltViewModel(LocalActivity.current)
    val profilesVM: ProfileViewModel = hiltViewModel(LocalActivity.current)


    val profilesState by profilesVM.state.collectAsStateWithLifecycle()

    if (profilesState.profileModelList.isNotEmpty()) {
        val availabilityState by ble.availabilityState.collectAsStateWithLifecycle()
        if (availabilityState && profilesVM.selectedProfile?.profile?.macAddress != null) {

            val connectionState = ble.connectionState(
                profilesVM.selectedProfile!!.profile.macAddress!!
            )

            //when device is registered
            connectionState?.let { state ->

                val connection by state.collectAsStateWithLifecycle()
                if (connection == GattConnectionState.STATE_CONNECTED) {

                    val activity by ble.getActivity(profilesVM.selectedProfile!!.profile.macAddress!!)
                        .collectAsStateWithLifecycle()
                    activity?.let {
                        Column(
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "Activity: ${it.first}",
                                fontSize = 32.sp
                            )

                            Text(
                                text = "Rest: ${it.second}",
                                fontSize = 32.sp
                            )
                        }
                    }
                }
            }
        } else {
            Text("Nothing to display")
        }
    } else {
        Text("Nothing to display")
    }

}

@Preview(showBackground = true)
@Composable
fun MapScreenPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            WellnessView()
        }
    }
}