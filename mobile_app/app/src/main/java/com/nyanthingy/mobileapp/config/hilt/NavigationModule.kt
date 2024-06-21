package com.nyanthingy.mobileapp.config.hilt

import com.nyanthingy.mobileapp.ui.navigation.navigator.NavigationManager
import com.nyanthingy.mobileapp.ui.navigation.navigator.Navigator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityRetainedComponent

@Module
@InstallIn(ActivityRetainedComponent::class)
interface NavigationModule {
    @Binds
    fun bindNavigator(navigator: NavigationManager): Navigator
}