package com.nyanthingy.mobileapp.ui.navigation.navigator

import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

sealed class NavigationDestination(
    val route: String
) {
    operator fun plus(other: NavigationDestination): List<NavigationDestination> {
        return listOf(this, other)
    }
    operator fun plus(other: List<NavigationDestination>): List<NavigationDestination> {
        return listOf(this) + other
    }
}

class ComposableDestination(
    route: String,
    val content: @Composable () -> Unit,
): NavigationDestination(route)

class InnerNavigationDestination(
    route: String,
    val destinations: List<NavigationDestination>,
): NavigationDestination(route)

class DialogDestination(
    route: String,
    val dialogProperties: DialogProperties = DialogProperties(),
    val content: @Composable () -> Unit,
): NavigationDestination(route)
