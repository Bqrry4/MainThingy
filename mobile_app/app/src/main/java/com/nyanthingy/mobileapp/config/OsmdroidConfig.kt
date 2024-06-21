package com.nyanthingy.mobileapp.config

import android.content.Context
import androidx.preference.PreferenceManager
import org.osmdroid.config.Configuration

class OsmdroidConfig {
    companion object {
        fun config(appContext: Context) {
            Configuration.getInstance().load(
                appContext,
                PreferenceManager.getDefaultSharedPreferences(appContext)
            )
            Configuration.getInstance().userAgentValue = appContext.packageName
        }
    }
}