package com.nyanthingy.mobileapp.ui.navigation.bottomnav

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.ui.navigation.RootRoute

data class BottomNavigationItem(
    val label: String,
    val icon: ImageVector,
    val route: String
) {
    companion object {
        @Composable
        fun getList() = listOf(
            BottomNavigationItem(
                label = "Wellness",
                icon = Icons.Default.MonitorHeart,
                route = RootRoute.Wellness.route
            ),
            BottomNavigationItem(
                label = "Map",
                icon = Icons.Default.LocationOn,
                route = RootRoute.Map.route
            ),
            BottomNavigationItem(
                label = "Profile",
                icon = ImageVector.vectorResource(R.drawable.cat),
                route = RootRoute.Profile.route
            ),
        )
    }
}