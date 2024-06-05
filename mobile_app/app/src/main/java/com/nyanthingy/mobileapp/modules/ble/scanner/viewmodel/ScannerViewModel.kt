package com.nyanthingy.mobileapp.modules.ble.scanner.viewmodel

import android.os.ParcelUuid
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.ble.scanner.repository.ScannerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import no.nordicsemi.android.kotlin.ble.core.scanner.BleScanResults
import no.nordicsemi.android.kotlin.ble.scanner.aggregator.BleScanResultAggregator
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanFailedError
import no.nordicsemi.android.kotlin.ble.scanner.errors.ScanningFailedException
import javax.inject.Inject
sealed class ScanningState {
    data object Loading : ScanningState()
    data class Error(val errorCode: Int) : ScanningState()
    data class DevicesDiscovered(val devices: List<BleScanResults>) : ScanningState() {
        val bonded: List<BleScanResults> = devices.filter { it.device.isBonded }
        val notBonded: List<BleScanResults> = devices.filter { !it.device.isBonded }
    }

    fun isRunning(): Boolean {
        return this is Loading || this is DevicesDiscovered
    }
}

data class DevicesScanFilter(
    val filterUuidRequired: Boolean?,
    val filterNearbyOnly: Boolean,
    val filterWithNames: Boolean
)

@HiltViewModel
class ScannerViewModel @Inject constructor(
    private val _scannerRepository: ScannerRepository
) : ViewModel() {

    private val _state = MutableStateFlow<ScanningState>(ScanningState.Loading)
    val state = _state.asStateFlow()

    init {
        processScanResultFlow()
    }

        val filterConfig = MutableStateFlow(
        DevicesScanFilter(
            filterUuidRequired = true,
            filterNearbyOnly = false,
            filterWithNames = true
        )
    )
    private fun processScanResultFlow() {
        val aggregator = BleScanResultAggregator()
        _scannerRepository.getScanResultFlow()
            .map { aggregator.aggregate(it) } //Add new device and return an aggregated list
            //.filter { it.isNotEmpty() }

            .onStart { _state.value = ScanningState.Loading }
            .onEach { _state.value = ScanningState.DevicesDiscovered(it) }
            //when error on flow
            .catch { e ->
                _state.value = (e as? ScanningFailedException)?.let {
                    ScanningState.Error(it.errorCode.value)
                } ?: ScanningState.Error(ScanFailedError.UNKNOWN.value)
            }
            //.cancellable()
            .launchIn(viewModelScope) //Scanning will stop after the viewmodel is destroyed
    }
    private var uuid: ParcelUuid? = null
    private val FILTER_RSSI = -50 // [dBm]
    private fun List<BleScanResults>.applyFilters(config: DevicesScanFilter) =
        filter {
            uuid == null ||
                    config.filterUuidRequired == false ||
                    it.lastScanResult?.scanRecord?.serviceUuids?.contains(uuid) == true
        }
            .filter { !config.filterNearbyOnly || it.highestRssi >= FILTER_RSSI }
            .filter { !config.filterWithNames || it.device.hasName || it.advertisedName?.isNotEmpty() == true }
}
