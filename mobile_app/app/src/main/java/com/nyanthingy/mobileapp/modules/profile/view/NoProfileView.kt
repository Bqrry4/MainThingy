package com.nyanthingy.mobileapp.modules.profile.view

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.material3.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.nyanthingy.mobileapp.ui.navigation.LeafRoute
import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationViewModel
import com.nyanthingy.mobileapp.ui.screens.ProfileScreen
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme

@Composable
fun NoProfileView(
    modifier: Modifier = Modifier
) {

    val navigation = hiltViewModel<NavigationViewModel>()

    Box(contentAlignment= Alignment.Center)
    {
        Text(text = "No profile",
            fontSize = 50.sp)
    }

    Box(
        contentAlignment = Alignment.BottomEnd,
        modifier = modifier.padding(20.dp)){
        IconButton(onClick = {
            navigation.navigateTo(LeafRoute.AddProfile.route)
        }) {
            Icon(Icons.Default.Add, contentDescription = null)
        }
    }

}

@Preview(showBackground = true)
@Composable
fun NoProfileViewPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            NoProfileView()
        }
    }
}