package com.nyanthingy.mobileapp.ui.navigation.navigator

import androidx.navigation.NavOptions
import dagger.hilt.android.scopes.ActivityRetainedScoped


interface Navigator {
    fun <R> navigateBack(result: R? = null)
    fun navigateTo(to: String, args: Any? = null, navOptions: NavOptions? = null)
}