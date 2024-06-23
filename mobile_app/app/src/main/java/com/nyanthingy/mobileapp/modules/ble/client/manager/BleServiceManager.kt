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
import javax.inject.Inject


/**
 * Act as an repository
 */
interface BleServiceManager {
    fun bindService()
    fun unbindService()

    fun setLedState(macAddress: String, state: Boolean)
    fun setBuzzerState(macAddress: String, state: Boolean)

}

class BleServiceManagerImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : BleServiceManager {

    private var bleService: BleService? = null
    private var isBound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as BleService.LocalBinder
            bleService = binder.service
            isBound = true
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            bleService = null
            isBound = false
        }
    }

    override fun bindService() {
        val intent = Intent(context, BleService::class.java)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
    }
    override fun unbindService() {
        if (isBound) {
            context.unbindService(connection)
            isBound = false
        }
    }



    override fun setLedState(macAddress: String, state: Boolean) {
        TODO("Not yet implemented")
    }

    override fun setBuzzerState(macAddress: String, state: Boolean) {
//        bleService?.write(BleMessage(macAddress,
//            ))
        bleService?.write(
            BleMessage(
            macAddress,
                BleClientMessage(
                    MessageType.Request,
                    ResourceType.buzzer,
                    true
                ).encode()
        ))
    }

}