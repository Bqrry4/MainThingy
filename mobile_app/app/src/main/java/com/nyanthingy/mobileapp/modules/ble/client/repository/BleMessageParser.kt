package com.nyanthingy.mobileapp.modules.ble.client.repository

import no.nordicsemi.android.common.core.DataByteArray


enum class MessageType {
    Response,
    RequestNON,
    RequestCON
}

enum class ResourceType(val value: Int) {
    BATTERY(0),
    LED(1),
    BUZZER(2),
    ACTIVITY(4),
    UNKNOWN(-1);

    companion object {
        fun valueOf(value: Int) = entries.find { it.value == value } ?: UNKNOWN
    }
}


data class BleClientMessage(
    val messageType: MessageType,
    val resourceType: ResourceType,
    val messageBlocks: List<BleClientMessageBlock>
)

data class BleClientMessageBlock(
    val length: Int,
    val value: ByteArray
)

fun BleClientMessage.encode(): DataByteArray {

    var header = byteArrayOf(
        messageType.ordinal.toByte(),
        resourceType.value.toByte()
    )

    messageBlocks.forEach {
        header = header.plus(
            byteArrayOf(
                (it.length shr 8).toByte(),
                (it.length).toByte(),
            ).plus(it.value)
        )
    }

    return DataByteArray(header)
}

fun DataByteArray.decode(): BleClientMessage {

    val blocks = mutableListOf<BleClientMessageBlock>()

    var i = 2
    while(i < this.size)
    {

        val length = (value[i].toInt() shl 8) + value[i + 1]
        blocks.add(
            BleClientMessageBlock(
                length,
                value.sliceArray(
                    i + 2..<i + 2 + length
                )
            )
        )
        i += 2 + length
    }
    return BleClientMessage(
        messageType = MessageType.entries[value[0].toInt()],
        resourceType = ResourceType.valueOf(value[1].toInt()),
        blocks
    )
}