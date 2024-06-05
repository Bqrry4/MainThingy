package com.nyanthingy.mobileapp.ui.navigation

import com.nyanthingy.mobileapp.modules.ble.scanner.view.ScannerView
import com.nyanthingy.mobileapp.modules.profile.view.AddProfileView
import com.nyanthingy.mobileapp.ui.navigation.navigator.ComposableDestination
import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationDestination
import com.nyanthingy.mobileapp.ui.screens.MapScreen
import com.nyanthingy.mobileapp.ui.screens.ProfileScreen
import com.nyanthingy.mobileapp.ui.screens.WellnessScreen


/**
 * Routes for bottom navigation
 */
sealed class RootRoute(val route: String) {
    data object Wellness : RootRoute("wellness_root")
    data object Map : RootRoute("map_root")
    data object Profile : RootRoute("profile_root")
}

/**
 * Routes for screens
 */
sealed class LeafRoute(val route: String) {
    data object Wellness : LeafRoute("wellness")
    data object Map : LeafRoute("map")
    data object Profile : LeafRoute("profile")
    data object AddProfile : LeafRoute("add_profile")
    data object Scanner : LeafRoute("scanner")
}


interface Routes {
    companion object {
        val destinations = listOf<NavigationDestination>(
            ComposableDestination(RootRoute.Wellness.route) {
                WellnessScreen()
            },
            ComposableDestination(RootRoute.Map.route) {
               MapScreen()
            },
            ComposableDestination(RootRoute.Profile.route) {
                ProfileScreen()
            },
            ComposableDestination(LeafRoute.AddProfile.route) {
                AddProfileView()
            },
            ComposableDestination(LeafRoute.Scanner.route) {
                ScannerView()
            }
        )
    }
}

