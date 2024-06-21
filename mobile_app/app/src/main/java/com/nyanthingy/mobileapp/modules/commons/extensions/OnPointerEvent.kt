package com.nyanthingy.mobileapp.modules.commons.extensions

import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel


/**
 * Custom pointer event modifier for sharing the events with the siblings on the same tree level
 * @param callback callback to handle the receive channel
 */
fun Modifier.onPointerEvent(
    callback: (ReceiveChannel<PointerEvent>) -> Unit,
) =
    this then PointerInputElement(callback)

class OnPointerEventNode(
    var callback: (ReceiveChannel<PointerEvent>) -> Unit,
) : PointerInputModifierNode, Modifier.Node() {
    private var eventQueue = Channel<PointerEvent>()
    init{
        callback(eventQueue)
    }
    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
        if (pass == PointerEventPass.Initial) {
            eventQueue.trySend(pointerEvent)
        }
    }

    override fun onCancelPointerInput() {
        // Do nothing
    }

    override fun sharePointerInputWithSiblings(): Boolean {
        return true
    }
}

data class PointerInputElement(
    val callback: (ReceiveChannel<PointerEvent>) -> Unit
) : ModifierNodeElement<OnPointerEventNode>() {
    override fun create() = OnPointerEventNode(callback)
    override fun update(node: OnPointerEventNode) {
        node.callback = callback
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "onPointerEvent"
        properties["callback"] = callback
    }
}
