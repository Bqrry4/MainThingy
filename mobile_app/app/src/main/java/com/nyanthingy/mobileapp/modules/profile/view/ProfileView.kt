package com.nyanthingy.mobileapp.modules.profile.view

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.NetworkCell
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel.ScannerViewModel
import com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel.ScanningState
import com.nyanthingy.mobileapp.modules.commons.view.DropDownSelect
import com.nyanthingy.mobileapp.modules.commons.view.OverlappingBoxes
import com.nyanthingy.mobileapp.modules.profile.viewmodel.ProfileViewModel
import com.nyanthingy.mobileapp.ui.screens.ProfileScreen
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme

@Composable
fun ProfileView() {

    val viewModel = hiltViewModel<ProfileViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    if (state.profileList.isEmpty())
    {
        NoProfileView()
    }
    else
    {
        ProfileListView(state.profileList)
    }
}
