package com.nyanthingy.mobileapp.modules.ble.scanner.repository

import android.annotation.SuppressLint
import dagger.hilt.android.scopes.ViewModelScoped
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScannerSettings
import no.nordicsemi.android.kotlin.ble.scanner.BleScanner
import javax.inject.Inject

@ViewModelScoped
class ScannerRepositoryImpl @Inject internal constructor(
    private val _bleScanner: BleScanner
):ScannerRepository {
    @SuppressLint("MissingPermission")
    override fun getScanResultFlow() = _bleScanner.scan(
        //don't include stored bonded devices
        settings = BleScannerSettings(includeStoredBondedDevices = false)
    )
}