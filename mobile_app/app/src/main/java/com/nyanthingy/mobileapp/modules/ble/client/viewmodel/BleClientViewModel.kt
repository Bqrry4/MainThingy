package com.nyanthingy.mobileapp.modules.ble.client.viewmodel

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nyanthingy.mobileapp.modules.ble.client.NyanThingySpecifications
import com.nyanthingy.mobileapp.modules.ble.client.repository.BleClientManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattServices
import javax.inject.Inject


@SuppressLint("MissingPermission")
@HiltViewModel
class BleClientViewModel @Inject constructor(
    @ApplicationContext
    private val appContext: Context,
    private val _bleManager: BleClientManager
) : ViewModel() {

    private val _devices = MutableStateFlow(listOf<ClientBleGatt>())
    val devices = _devices.asStateFlow()

    private fun connect(macAddress: String) = viewModelScope.launch {
        val client = ClientBleGatt
            .connect(appContext, macAddress, viewModelScope)
            .also {
                _bleManager.connectedDevices[macAddress] = it
            }

        //Discover services on the Bluetooth LE Device.
        val services = client.discoverServices()
        val service = services.findService(NyanThingySpecifications.UUID_NUS_SERVICE)!!
        //Remember characteristics to communicate with the device.
        rxCharacteristic = service.findCharacteristic(NyanThingySpecifications.UUID_RX_CHAR)!!
        txCharacteristic = service.findCharacteristic(NyanThingySpecifications.UUID_TX_CHAR)!!

        //Observe tx characteristic
        txCharacteristic.getNotifications().onEach {
            //_state.value = _state.value.copy(isButtonPressed = BlinkyButtonParser.isButtonPressed(it))
            Log.println(Log.DEBUG, null, it.toString())
        }.launchIn(viewModelScope)


    }

    private lateinit var rxCharacteristic: ClientBleGattCharacteristic
    private lateinit var txCharacteristic: ClientBleGattCharacteristic
    private suspend fun configureGatt(services: ClientBleGattServices) {


//        //Check the initial state of the Led.
//        val isLedOn = BlinkyLedParser.isLedOn(ledCharacteristic.read())
//        _state.value = _state.value.copy(isLedOn = isLedOn)
    }

}