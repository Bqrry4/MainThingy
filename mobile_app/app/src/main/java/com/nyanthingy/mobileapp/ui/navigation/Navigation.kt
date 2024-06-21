package com.nyanthingy.mobileapp.ui.navigation

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.nyanthingy.mobileapp.ui.navigation.bottomnav.BottomNavigationBar
import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationGraph
import com.nyanthingy.mobileapp.ui.theme.NyanthingyAppTheme

@Composable
fun Navigation() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = { BottomNavigationBar(navController, currentDestination) }
    ) { paddingValues ->

        NavigationGraph(
            destinations = Routes.destinations,
            navController = navController,
            modifier = Modifier.padding(paddingValues = paddingValues)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun NavigationPreview() {
    NyanthingyAppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
            Navigation()
        }
    }
}