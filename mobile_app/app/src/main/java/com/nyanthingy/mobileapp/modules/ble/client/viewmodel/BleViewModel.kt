package com.nyanthingy.mobileapp.modules.ble.client.viewmodel

import androidx.lifecycle.ViewModel
import com.nyanthingy.mobileapp.modules.ble.client.manager.BleServiceManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject




@HiltViewModel
class BleViewModel @Inject constructor(
    private val bleServiceManager: BleServiceManager
) : ViewModel() {

    /* The following are for binding to the service in a lifecycle manner. */
    init{
        bleServiceManager.bindService()
    }
    override fun onCleared() {
        super.onCleared()
        bleServiceManager.unbindService()
    }

    /* Main functionalities */
    fun getLedState(){

    }

    fun setLedState(macAddress: String, state: Boolean) =
        bleServiceManager.setLedState(macAddress, state)


    fun getBuzzerState()
    {

    }

    fun setBuzzerState(macAddress: String, state: Boolean) =
        bleServiceManager.setBuzzerState(macAddress, state)

}