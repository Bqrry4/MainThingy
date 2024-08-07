package com.nyanthingy.mobileapp.ui.navigation.navigator

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavDeepLinkRequest
import androidx.navigation.NavDestination
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.navigation

/**
 * NavigationGraph
 * @param destinations list of destination which builds the graph
 * @param navController to perform the navigation
 * @param modifier to be applied to the layout
 */
@Composable
fun NavigationGraph(
    destinations: List<NavigationDestination>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navigation = hiltViewModel<NavigationViewModel>()
    //navigation.use(navController.currentBackStackEntryFlow)

    val activity = LocalContext.current as Activity

    //Without a key as it should be launched only one time to start the collection of navigation events
    LaunchedEffect(true)
    {
        navigation.events.collect {
            it?.let {
                when (it) {
                    is Direction.NavigateTo -> {
                        navController.navigate(it.route, it.args, it.navOptions)
                        Log.println(Log.DEBUG, null, it.route)
                    }

                    is Direction.NavigateBack -> {
                        Log.println(Log.DEBUG, null, "back")

                        // Navigate back passing the result
                        navController.currentBackStackEntry?.destination?.route?.let { route ->
                            navController.previousBackStackEntry?.savedStateHandle?.set(
                                route,
                                it.result
                            )
                        }

                        //finish the Activity if at root
                        if (!navController.navigateUp()) {
                            activity.finish()
                        }
                    }
                }
            }
        }
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = destinations.first().route
    ) {
        renderDestinations(destinations, navigation)
    }
}

/**
 * Renders NavigationDestinations to corresponding NavGraph element
 */
private fun NavGraphBuilder.renderDestinations(
    destinations: List<NavigationDestination>,
    navigation: NavigationViewModel
) {
    destinations.forEach { destination ->
        when (destination) {
            is ComposableDestination -> {
                composable(
                    route = destination.route,

                    ) {
                    //navigation.use(it.savedStateHandle)
                    destination.content()
                }
            }

            is InnerNavigationDestination -> {
                navigation(
                    startDestination = destination.destinations.first().route,
                    route = destination.route
                ) {
                    renderDestinations(destination.destinations, navigation)
                }
            }

            is DialogDestination -> {
                dialog(
                    route = destination.route,
                    dialogProperties = destination.dialogProperties,
                ) {
                    //navigation.use(it.savedStateHandle)
                    destination.content()
                }
            }
        }
    }
}

/**
 * Extension for passing a Bundle.
 * Ignores the Bundle when the route is not defined in the graph
 */
@SuppressLint("RestrictedApi")
internal fun NavController.navigate(
    route: String,
    args: Bundle?,
    navOptions: NavOptions? = null,
) {
    val routeLink = NavDeepLinkRequest
        .Builder
        .fromUri(NavDestination.createRoute(route).toUri())
        .build()

    when (val deepLinkMatch = graph.matchDeepLink(routeLink)) {
        null -> {
            navigate(route, navOptions)
        }

        else -> {
            navigate(deepLinkMatch.destination.id, args, navOptions)
        }
    }
}