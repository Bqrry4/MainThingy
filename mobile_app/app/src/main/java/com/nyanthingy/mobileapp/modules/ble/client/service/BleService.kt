package com.nyanthingy.mobileapp.modules.ble.client.service

import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.nyanthingy.mobileapp.R
import com.nyanthingy.mobileapp.modules.ble.client.NyanThingySpecifications
import com.nyanthingy.mobileapp.modules.ble.client.repository.DevicesRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import no.nordicsemi.android.common.core.DataByteArray
import no.nordicsemi.android.kotlin.ble.client.main.callback.ClientBleGatt
import no.nordicsemi.android.kotlin.ble.client.main.service.ClientBleGattCharacteristic
import no.nordicsemi.android.kotlin.ble.core.data.BleGattConnectOptions
import javax.inject.Inject

internal data class BleDevice(
    val client: ClientBleGatt,
    val rxCharacteristic: ClientBleGattCharacteristic,
    val txCharacteristic: ClientBleGattCharacteristic
)

data class BleMessage(
    val macAddress: String,
    val data: DataByteArray
)

@AndroidEntryPoint
class BleService : Service() {

    @Inject
    lateinit var devicesRepository: DevicesRepository
    private var knownDevicesList = emptyList<String>()
    private val connectedDevices = mutableMapOf<String, BleDevice>()

    private val messageChannel = Channel<BleMessage>()
    val incomingMessages = messageChannel.receiveAsFlow()

    companion object {
        private const val NOTIFICATION_ID = 1
        private const val CHANNEL_ID = "ble_service_channel"
        const val CLOSE_INTENT = "com.nyanthingy.ACTION_APP_CLOSE"
    }

    // Binder given to clients
    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        val service: BleService
            get() = this@BleService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)

    override fun onCreate() {
        super.onCreate()

        //Create notification
        val notification = createNotification()
        //Start the service in the foreground
        startForeground(NOTIFICATION_ID, notification)

        //collect devices state
        serviceScope.launch {
            devicesRepository.getAll().collect {
                knownDevicesList = it
                //try to connect
                knownDevicesList.forEach { address ->
                    connect(address)
                }
            }
        }

        //Check connections and try to connect to known devices
        serviceScope.launch {
            while (true) {

                val content = if (connectedDevices.isEmpty()) {
                    "Running"
                } else {
                    connectedDevices.keys.reduce { content, entry ->
                        "$content $entry \n"
                    }
                }

                updateNotification("Maintaining connection", content)

                //wait 1 minute
                delay(60000)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        when (intent?.action) {
            CLOSE_INTENT -> {
                //close the service when there are no devices to maintain connection
                if (knownDevicesList.isEmpty()) {
                    stopSelf()
                }
            }
        }

        return START_STICKY
    }

    /**
     * Creates the notification for the service
     */
    private fun createNotification(): Notification {

        val notificationChannelId = CHANNEL_ID
        val notificationBuilder = NotificationCompat.Builder(this, notificationChannelId)
            .setContentTitle("Maintaining connection")
            .setContentText("Running...")
            .setSmallIcon(R.drawable.cat)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                notificationChannelId,
                "BleService Channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }

        return notificationBuilder.build()
    }

    @SuppressLint("MissingPermission")
    private fun updateNotification(title: String, content: String) {
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.cat)
            .build()

        val notificationManager = NotificationManagerCompat.from(this)
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    @SuppressLint("MissingPermission")
    private fun connect(macAddress: String) = serviceScope.launch {

        if (macAddress in connectedDevices &&
            connectedDevices[macAddress]!!.client.isConnected
        ) {
            //Already connected
            return@launch
        }

        val client = ClientBleGatt
            .connect(
                this@BleService, macAddress, serviceScope,
                options = BleGattConnectOptions(
                    autoConnect = true
                )
            )

        //did not connect
        if (!client.isConnected) {
            return@launch
        }

        //Discover services on the Bluetooth LE Device
        val services = client.discoverServices()
        val service = services.findService(NyanThingySpecifications.UUID_NUS_SERVICE)!!
        //Remember characteristics to communicate with the device
        val rxCharacteristic = service.findCharacteristic(NyanThingySpecifications.UUID_RX_CHAR)!!
        val txCharacteristic = service.findCharacteristic(NyanThingySpecifications.UUID_TX_CHAR)!!

        //Observe tx characteristic
        txCharacteristic.getNotifications().onEach {
            //Send received message
            messageChannel.trySend(
                BleMessage(
                    macAddress = macAddress,
                    data = it
                )
            )

        }.launchIn(serviceScope)

        Log.println(Log.INFO, "ble service", "Connected to $macAddress")

        connectedDevices[macAddress] = BleDevice(
            client = client,
            rxCharacteristic = rxCharacteristic,
            txCharacteristic = txCharacteristic
        )
    }

    @SuppressLint("MissingPermission")
    fun write(message: BleMessage) {
        //maybe throw exception
        if (message.macAddress !in connectedDevices) {
            Log.println(Log.ERROR, "ble service", "Device not connected")
            return
        }

        serviceScope.launch {
            connectedDevices[message.macAddress]!!.rxCharacteristic.write(message.data)
        }
    }

}