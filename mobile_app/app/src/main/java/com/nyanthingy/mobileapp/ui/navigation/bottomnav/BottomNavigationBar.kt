package com.nyanthingy.mobileapp.ui.navigation.bottomnav

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.NavGraph.Companion.findStartDestination


@Composable
fun BottomNavigationBar(
    navController: NavController,
    currentDestination: NavDestination?
) {
    NavigationBar(
        modifier = Modifier
            .defaultMinSize(minHeight = 64.dp)
            .clip(RoundedCornerShape(20.dp, 20.dp))
            .background(Color.Transparent)
    ) {
        BottomNavigationItem.getList().forEach { navigationItem ->
            NavigationBarItem(
                modifier = Modifier.defaultMinSize(minHeight = 64.dp),
                selected = navigationItem.route == currentDestination?.route,
                label = {
                    Text(navigationItem.label)
                },
                icon = {
                    Icon(
                        navigationItem.icon,
                        contentDescription = navigationItem.label
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    selectedTextColor = MaterialTheme.colorScheme.primary,
                    indicatorColor = Color.Transparent,
                    unselectedIconColor = Color.White,
                    unselectedTextColor = Color.White
                ),
                onClick = {
                    navController.navigate(navigationItem.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                }
            )
        }
    }
}