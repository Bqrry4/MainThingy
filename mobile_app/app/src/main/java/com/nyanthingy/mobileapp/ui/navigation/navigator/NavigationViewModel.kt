package com.nyanthingy.mobileapp.ui.navigation.navigator

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

/**
 * The Viewmodel used for navigation
 */
@HiltViewModel
class NavigationViewModel @Inject constructor(
    navigationManager: NavigationManager,
    //private val savedStateHandle: SavedStateHandle,
) : ViewModel(), Navigator by navigationManager
{

    /** flow of navigation events. */
    internal val events = navigationManager.events

}