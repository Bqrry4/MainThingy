package com.nyanthingy.mobileapp.modules.ble.scanner.repository

import kotlinx.coroutines.flow.Flow
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResult

interface ScannerRepository{
    fun getScanResultFlow(): Flow<BleScanResult>
}