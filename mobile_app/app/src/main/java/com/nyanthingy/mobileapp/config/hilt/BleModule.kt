package com.nyanthingy.mobileapp.config.hilt

import android.content.Context
import com.nyanthingy.mobileapp.modules.ble.client.manager.BleServiceManager
import com.nyanthingy.mobileapp.modules.ble.client.manager.BleServiceManagerImpl
import com.nyanthingy.mobileapp.modules.ble.scanner.repository.ScannerRepository
import com.nyanthingy.mobileapp.modules.ble.scanner.repository.ScannerRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import dagger.hilt.components.SingletonComponent
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import javax.inject.Singleton

@Module
@InstallIn(ViewModelComponent::class)
interface BleModule {
    companion object {
        @Provides
        @ViewModelScoped
        fun provideBleScanner(
            @ApplicationContext context: Context
        ) = BleScanner(context)

        @Provides
        @ViewModelScoped
        fun provideBleServiceManager(
            @ApplicationContext context: Context
        ): BleServiceManager {
            return BleServiceManagerImpl(context)
        }
    }
    @Binds
    fun bindScannerRepository(
        scannerRepository: ScannerRepositoryImpl
    ): ScannerRepository

}

//@Module
//@InstallIn(SingletonComponent::class)
//object BleManagerModule {
//
//
//}
