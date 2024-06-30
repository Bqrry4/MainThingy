package com.nyanthingy.mobileapp.modules.ble.client.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.MacAddress
import android.os.IBinder
import com.nyanthingy.mobileapp.modules.ble.client.repository.BleClientMessage
import com.nyanthingy.mobileapp.modules.ble.client.repository.MessageType
import com.nyanthingy.mobileapp.modules.ble.client.repository.ResourceType
import com.nyanthingy.mobileapp.modules.ble.client.repository.encode
import com.nyanthingy.mobileapp.modules.ble.client.service.BleMessage
import com.nyanthingy.mobileapp.modules.ble.client.service.BleService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import javax.inject.Inject


/**
 * Act as an repository
 */
interface BleServiceManager {

    /**
     * Intention to bind to service
     */
    fun bindService()

    /**
     *  Intention to unbind from the services
     */
    fun unbindService()

    /**
     * State of the service binding
     */
    val isServiceBoundStateFlow: StateFlow<Boolean>

    fun setLedState(macAddress: String, state: Boolean)
    fun setBuzzerState(macAddress: String, state: Boolean)


    /**
     * Get the rssiSFlow of the device associated with the macAddress
     */
    fun rssiFlow(macAddress: String, samplingInterval: Long): Flow<Int>
    /**
     * Get the connectionState of the device associated with the macAddress
     */
    fun connectionState(macAddress: String): StateFlow<GattConnectionState>

}

class BleServiceManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleServiceManager {

    private var bleService: BleService? = null

    private val isBound = MutableStateFlow(false)
    override val isServiceBoundStateFlow: StateFlow<Boolean> = isBound.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleService.LocalBinder
            bleService = binder.service
            isBound.value = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            isBound.value = false
        }
    }

    override fun bindService() {
        val intent = Intent(context, BleService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }

    override fun unbindService() {
        if (isBound.value) {
            context.unbindService(connection)
            isBound.value = false
        }
    }

    override fun rssiFlow(macAddress: String, samplingInterval: Long): Flow<Int> {
        return bleService!!.rssiFlow(macAddress, samplingInterval)
    }

    override fun connectionState(macAddress: String): StateFlow<GattConnectionState> {
        return bleService!!.connectionState(macAddress)
    }


    override fun setLedState(macAddress: String, state: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setBuzzerState(macAddress: String, state: Boolean) {
        bleService!!.write(
            BleMessage(
                macAddress,
                BleClientMessage(
                    MessageType.Request,
                    ResourceType.buzzer,
                    true
                ).encode()
            )
        )
    }

}