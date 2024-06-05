package com.nyanthingy.mobileapp.config.hilt

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner

@Module
@InstallIn(ViewModelComponent::class)
interface BleModule {
    companion object {
        @Provides
        @ViewModelScoped
        fun provideBleScanner(
            @ApplicationContext context: Context
        ) = BleScanner(context)
    }
}
