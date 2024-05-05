package com.nyanthingy.mobileapp.ui.navbar

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.nyanthingy.mobileapp.R

sealed class BottomNavigationScreen(val route: String) {
    data object Wellness : BottomNavigationScreen("wellness_screen")
    data object Map : BottomNavigationScreen("map_screen")
    data object Profile : BottomNavigationScreen("profile_screen")
}

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
                icon = ImageVector.vectorResource(R.drawable.cat),
                route = BottomNavigationScreen.Wellness.route
            ),
            BottomNavigationItem(
                label = "Map",
                icon = Icons.Default.LocationOn,
                route = BottomNavigationScreen.Map.route
            ),
            BottomNavigationItem(
                label = "Profile",
                icon = Icons.Default.AccountCircle,
                route = BottomNavigationScreen.Profile.route
            ),
        )
    }
}