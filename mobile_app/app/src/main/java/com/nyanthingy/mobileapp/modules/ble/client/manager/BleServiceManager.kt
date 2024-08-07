package com.nyanthingy.mobileapp.modules.ble.client.manager

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import com.nyanthingy.mobileapp.modules.ble.client.repository.BleClientMessage
import com.nyanthingy.mobileapp.modules.ble.client.repository.BleClientMessageBlock
import com.nyanthingy.mobileapp.modules.ble.client.repository.MessageType
import com.nyanthingy.mobileapp.modules.ble.client.repository.ResourceType
import com.nyanthingy.mobileapp.modules.ble.client.repository.decode
import com.nyanthingy.mobileapp.modules.ble.client.repository.encode
import com.nyanthingy.mobileapp.modules.ble.client.service.BleMessage
import com.nyanthingy.mobileapp.modules.ble.client.service.BleService
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.kotlin.ble.core.data.GattConnectionState
import javax.inject.Inject
import kotlin.experimental.or


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
    fun getBatteryState(macAddress: String): StateFlow<Int?>

    fun getActivity(macAddress: String): StateFlow<Pair<Float, Float>?>
    /**
     * Get the rssiSFlow of the device associated with the macAddress
     */
    fun rssiFlow(macAddress: String, samplingInterval: Long): Flow<Int>?

    /**
     * Get the connectionState of the device associated with the macAddress
     * @return null when no device associated
     */
    fun connectionState(macAddress: String): StateFlow<GattConnectionState>?

}

class BleServiceManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleServiceManager {

    //this object scope
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    private var bleService: BleService? = null

    private val isBound = MutableStateFlow(false)
    override val isServiceBoundStateFlow: StateFlow<Boolean> = isBound.asStateFlow()

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleService.LocalBinder
            bleService = binder.service
            isBound.value = true

            serviceScope.launch {
                receiveMessages()
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            isBound.value = false

            serviceJob.cancel()
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

    override fun rssiFlow(macAddress: String, samplingInterval: Long): Flow<Int>? {
        return bleService!!.rssiFlow(macAddress, samplingInterval)
    }

    override fun connectionState(macAddress: String): StateFlow<GattConnectionState>? {
        return bleService?.connectionState(macAddress)
    }

    //Cashing the stateFlow
    private val responses = mutableMapOf<Pair<String, ResourceType>, MutableStateFlow<Any?>>()
    private suspend fun receiveMessages() {
        bleService!!.incomingMessages.collect {
            val message = it.data.decode()
            //for now message type is always response
            //message.messageType
            val state = responses[Pair(it.macAddress, message.resourceType)] ?: run {
                responses[Pair(it.macAddress, message.resourceType)] = MutableStateFlow(null)
                responses[Pair(it.macAddress, message.resourceType)]!!
            }

            when (message.resourceType) {
                ResourceType.BATTERY -> {
                    state.value = message.messageBlocks[0].value[0].toInt()
                }

                else -> {}
            }

            when (message.resourceType) {
                ResourceType.ACTIVITY -> {

                    val stationaryTime =  littleEndianConversion(message.messageBlocks.first().value.sliceArray(0..<8));
                    val motionTime = littleEndianConversion(message.messageBlocks.first().value.sliceArray(8..<16));

                    val motion = motionTime.toFloat() / 60000
                    val stationary = stationaryTime.toFloat() / 60000

                    state.value = Pair(motion, stationary)
                }

                else -> {}
            }
        }
    }

    override fun setLedState(macAddress: String, state: Boolean) {
        bleService!!.write(
            BleMessage(
                macAddress,
                BleClientMessage(
                    MessageType.RequestNON,
                    ResourceType.LED,
                    listOf(
                        BleClientMessageBlock(
                            length = 1,
                            value = byteArrayOf(
                                (if (state) 0x01 else 0x00).toByte()
                            )
                        )
                    )
                ).encode()
            )
        )
    }

    override fun setBuzzerState(macAddress: String, state: Boolean) {
        bleService!!.write(
            BleMessage(
                macAddress,
                BleClientMessage(
                    MessageType.RequestNON,
                    ResourceType.BUZZER,
                    listOf(
                        BleClientMessageBlock(
                            length = 1,
                            value = byteArrayOf(
                                (if (state) 0x01 else 0x00).toByte()
                            )
                        )
                    )
                ).encode()
            )
        )
    }

    override fun getBatteryState(macAddress: String): StateFlow<Int?> {
        //send request async
        bleService!!.write(
            BleMessage(
                macAddress,
                BleClientMessage(
                    MessageType.RequestCON,
                    ResourceType.BATTERY,
                    emptyList()
                ).encode()
            )
        )

        //send back a cached flow that will be updated
        val state = responses[Pair(macAddress, ResourceType.BATTERY)] ?: run {
            responses[Pair(macAddress, ResourceType.BATTERY)] = MutableStateFlow(null)
            responses[Pair(macAddress, ResourceType.BATTERY)]!!
        }

        return state as StateFlow<Int?>
    }

    override fun getActivity(macAddress: String): StateFlow<Pair<Float, Float>?> {
        //send request async
        bleService!!.write(
            BleMessage(
                macAddress,
                BleClientMessage(
                    MessageType.RequestCON,
                    ResourceType.ACTIVITY,
                    emptyList()
                ).encode()
            )
        )

        //send back a cached flow that will be updated
        val state = responses[Pair(macAddress, ResourceType.ACTIVITY)] ?: run {
            responses[Pair(macAddress, ResourceType.ACTIVITY)] = MutableStateFlow(null)
            responses[Pair(macAddress, ResourceType.ACTIVITY)]!!
        }

        return state as StateFlow<Pair<Float, Float>?>
    }

}


fun littleEndianConversion(bytes: ByteArray): UInt {
    var result = 0u

    for (i in bytes.indices) {
        result = result or ((bytes[i].toUInt() and 0xFFu) shl 8 * i)
    }
    return result
}