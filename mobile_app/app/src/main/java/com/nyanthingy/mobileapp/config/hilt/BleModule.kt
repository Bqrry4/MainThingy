package com.nyanthingy.mobileapp.config.hilt

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner

@Module
@InstallIn(ViewModelComponent::class)
abstract class BleModule {
    companion object {
        @Provides
        @ViewModelScoped
        fun provideBleScanner(
            @ApplicationContext context: Context
        ) = BleScanner(context)
    }
}

//@Suppress("unused")
//@Module
//@InstallIn(SingletonComponent::class)
//internal class ScannerHiltModule {
//
//    @Provides
//    fun provideBluetoothAdapter(
//        @ApplicationContext context: Context
//    ): BluetoothAdapter {
//        val manager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
//        return manager.adapter
//    }
//}