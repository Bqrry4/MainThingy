package com.nyanthingy.mobileapp.modules.ble.client.repository

import no.nordicsemi.android.common.core.DataByteArray


enum class MessageType {
    Request,
    Response
}

enum class ResourceType {
    battery,
    led,
    buzzer
}


data class BleClientMessage(
    val messageType: MessageType,
    val resourceType: ResourceType,
    val value: Boolean
)

fun BleClientMessage.encode(): DataByteArray {
    return DataByteArray(
        byteArrayOf(
            messageType.ordinal.toByte(),
            resourceType.ordinal.toByte(),
            if(value) 0x01 else 0x00
        )
    )
}

fun DataByteArray.decode(): BleClientMessage {
    return BleClientMessage(
        messageType = MessageType.entries[value[0].toInt()] ,
        resourceType = ResourceType.entries[value[1].toInt()],
        value = value[3].toInt() != 0
    )
}