package com.nyanthingy.mobileapp.ui.navigation.navigator

import android.content.Context
import android.os.Bundle
import androidx.core.os.bundleOf
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.NavDirections
import androidx.navigation.NavOptions
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ActivityRetainedScoped
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

/**
 * The navigation direction used as event
 */
sealed interface Direction {
    data class NavigateTo(
        val route: String,
        val args: Bundle? = null,
        val navOptions: NavOptions? = null
    ) : Direction

    data class NavigateBack(
        val result: Any?
    ) : Direction
}

data class DestinationId<in A, out R>(internal val name: String) {
    override fun toString(): String = name
}

@ActivityRetainedScoped
class NavigationManager @Inject constructor(
) : Navigator {

    private val _events = Channel<Direction?>(Channel.UNLIMITED)
    internal val events = _events.receiveAsFlow()

    internal var savedStateHandle: SavedStateHandle? = null

    override fun <R> navigateBack(result: R?) {
        _events.trySend( Direction.NavigateBack(result) )
    }

    override fun navigateTo(to: String, args: Any?, navOptions: NavOptions?) {
        val bundle = if (args is Unit) bundleOf() else bundleOf(to to args)
        _events.trySend ( Direction.NavigateTo(to, bundle, navOptions) )
    }

}