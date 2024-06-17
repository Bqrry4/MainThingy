package com.nyanthingy.mobileapp.modules.commons.extensions

import androidx.compose.runtime.collection.mutableVectorOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.AwaitPointerEventScope
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerEventTimeoutCancellationException
import androidx.compose.ui.input.pointer.PointerInputScope
import androidx.compose.ui.input.pointer.changedToUpIgnoreConsumed
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.platform.ViewConfiguration
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.util.fastAll
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.coroutines.createCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


/**
 * A ripoff from the [pointerInput] implementation, as it has all it's components internal.
 * It's main purpose is to share the events with other composable siblings and maintain the PointerInputScope functionalities
 */
fun Modifier.onPointerEventAsync(
    block: suspend PointerInputScope.() -> Unit,
    viewConfiguration: ViewConfiguration,
    density: Density
) =
    this then PointerInputElementAsync(block, viewConfiguration, density)

class OnPointerEventNodeAsync(
    pointerInputHandler: suspend PointerInputScope.() -> Unit,
    viewConfiguration: ViewConfiguration,
    density: Density
) : PointerInputModifierNode, Modifier.Node(), PointerInputScope {

    override var density: Float = density.density
    override var fontScale: Float = density.fontScale
    override val size: IntSize
        get() = boundsSize

    override var viewConfiguration: ViewConfiguration = viewConfiguration

    private var boundsSize: IntSize = IntSize.Zero

    private var pointerInputJob: Job? = null

    private var currentEvent: PointerEvent = PointerEvent(emptyList())
    private var lastPointerEvent: PointerEvent? = null

    var pointerInputHandler = pointerInputHandler
        set(value) {
            resetPointerInputHandler()
            field = value
        }

    private fun resetPointerInputHandler() {
        val localJob = pointerInputJob
        if (localJob != null) {
            localJob.cancel()
            pointerInputJob = null
        }
    }

    override fun onDetach() {
        resetPointerInputHandler()
        super.onDetach()
    }

    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize
    ) {
        boundsSize = bounds
        if (pass == PointerEventPass.Initial) {
            currentEvent = pointerEvent
        }

        // Coroutine lazily launches when first event comes in.
        if (pointerInputJob == null) {
            // 'start = CoroutineStart.UNDISPATCHED' required so handler doesn't miss first event.
            pointerInputJob = coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                pointerInputHandler()
            }
        }

        dispatchPointerEvent(pointerEvent, pass)

        lastPointerEvent = pointerEvent.takeIf { event ->
            !event.changes.fastAll { it.changedToUpIgnoreConsumed() }
        }
    }
    private inline fun forEachCurrentPointerHandler(
        pass: PointerEventPass,
        block: (OnPointerEventNodeAsync.PointerEventHandlerCoroutine<*>) -> Unit
    ) {
        // Copy handlers to avoid mutating the collection during dispatch
        synchronized(pointerHandlers) {
            dispatchingPointerHandlers.addAll(pointerHandlers)
        }
        try {
            when (pass) {
                PointerEventPass.Initial, PointerEventPass.Final ->
                    dispatchingPointerHandlers.forEach(block)

                PointerEventPass.Main ->
                    dispatchingPointerHandlers.forEachReversed(block)
            }
        } finally {
            dispatchingPointerHandlers.clear()
        }
    }

    private fun dispatchPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass
    ) {
        forEachCurrentPointerHandler(pass) {
            it.offerPointerEvent(pointerEvent, pass)
        }
    }


    private val pointerHandlers =
        mutableVectorOf<OnPointerEventNodeAsync.PointerEventHandlerCoroutine<*>>()

    /**
     * Scratch list for dispatching to handlers for a particular phase.
     * Used to hold a copy of the contents of [pointerHandlers] during dispatch so that
     * resumed continuations may add/remove handlers without affecting the current dispatch pass.
     * Must only access on the UI thread.
     */
    private val dispatchingPointerHandlers =
        mutableVectorOf<OnPointerEventNodeAsync.PointerEventHandlerCoroutine<*>>()

    override suspend fun <R> awaitPointerEventScope(
        block: suspend AwaitPointerEventScope.() -> R
    ): R = suspendCancellableCoroutine { continuation ->
        val handlerCoroutine = PointerEventHandlerCoroutine(continuation)
        synchronized(pointerHandlers) {
            pointerHandlers += handlerCoroutine

            // NOTE: We resume the new continuation while holding this lock.
            // We do this since it runs in a RestrictsSuspension scope and therefore
            // will only suspend when awaiting a new event. We don't release this
            // synchronized lock until we know it has an awaiter and any future dispatch
            // would succeed.

            // We also create the coroutine with both a receiver and a completion continuation
            // of the handlerCoroutine itself; we don't use our currently available suspended
            // continuation as the resume point because handlerCoroutine needs to remove the
            // ContinuationInterceptor from the supplied CoroutineContext to have un-dispatched
            // behavior in our restricted suspension scope. This is required so that we can
            // process event-awaits synchronously and affect the next stage in the pipeline
            // without running too late due to dispatch.
            block.createCoroutine(handlerCoroutine, handlerCoroutine).resume(Unit)
        }

        // Restricted suspension handler coroutines can't propagate structured job cancellation
        // automatically as the context must be EmptyCoroutineContext; do it manually instead.
        continuation.invokeOnCancellation { handlerCoroutine.cancel(it) }
    }
    private inner class PointerEventHandlerCoroutine<R>(
        private val completion: Continuation<R>,
    ) : AwaitPointerEventScope,
        Density by this@OnPointerEventNodeAsync,
        Continuation<R> {

        private var pointerAwaiter: CancellableContinuation<PointerEvent>? = null
        private var awaitPass: PointerEventPass = PointerEventPass.Main

        override val currentEvent: PointerEvent
            get() = this@OnPointerEventNodeAsync.currentEvent
        override val size: IntSize
            get() = this@OnPointerEventNodeAsync.boundsSize
        override val viewConfiguration: ViewConfiguration
            get() = this@OnPointerEventNodeAsync.viewConfiguration
        override val extendedTouchPadding: Size
            get() = this@OnPointerEventNodeAsync.extendedTouchPadding

        fun offerPointerEvent(event: PointerEvent, pass: PointerEventPass) {
            if (pass == awaitPass) {
                pointerAwaiter?.run {
                    pointerAwaiter = null
                    resume(event)
                }
            }
        }

        // Called to run any finally blocks in the awaitPointerEventScope block
        fun cancel(cause: Throwable?) {
            pointerAwaiter?.cancel(cause)
            pointerAwaiter = null
        }

        // context must be EmptyCoroutineContext for restricted suspension coroutines
        override val context: CoroutineContext = EmptyCoroutineContext

        // Implementation of Continuation; clean up and resume our wrapped continuation.
        override fun resumeWith(result: Result<R>) {
            synchronized(pointerHandlers) {
                pointerHandlers -= this
            }
            completion.resumeWith(result)
        }

        override suspend fun awaitPointerEvent(
            pass: PointerEventPass
        ): PointerEvent = suspendCancellableCoroutine { continuation ->
            awaitPass = pass
            pointerAwaiter = continuation
        }

        override suspend fun <T> withTimeoutOrNull(
            timeMillis: Long,
            block: suspend AwaitPointerEventScope.() -> T
        ): T? {
            return try {
                withTimeout(timeMillis, block)
            } catch (_: PointerEventTimeoutCancellationException) {
                null
            }
        }

        override suspend fun <T> withTimeout(
            timeMillis: Long,
            block: suspend AwaitPointerEventScope.() -> T
        ): T {
            if (timeMillis <= 0L) {
                pointerAwaiter?.resumeWithException(
                    PointerEventTimeoutCancellationException(timeMillis)
                )
            }

            val job = coroutineScope.launch {
                // Delay twice because the timeout continuation needs to be lower-priority than
                // input events, not treated fairly in FIFO order. The second
                // micro-delay reposts it to the back of the queue, after any input events
                // that were posted but not processed during the first delay.
                delay(timeMillis - 1)
                delay(1)

                pointerAwaiter?.resumeWithException(
                    PointerEventTimeoutCancellationException(timeMillis)
                )
            }
            try {
                return block()
            } finally {
                job.cancel()
            }
        }
    }

    override fun onCancelPointerInput() {
        resetPointerInputHandler()
    }

    override fun sharePointerInputWithSiblings(): Boolean {
        return true
    }
}

data class PointerInputElementAsync(
    val pointerInputHandler: suspend PointerInputScope.() -> Unit,
    val viewConfiguration: ViewConfiguration,
    val density: Density
) : ModifierNodeElement<OnPointerEventNodeAsync>() {
    override fun create() = OnPointerEventNodeAsync(pointerInputHandler, viewConfiguration, density)
    override fun update(node: OnPointerEventNodeAsync) {
        node.pointerInputHandler = pointerInputHandler
        node.viewConfiguration = viewConfiguration
        node.density = density.density
        node.fontScale = density.fontScale
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "onPointerEvent"
        properties["viewConfiguration"] = viewConfiguration
        properties["density"] = density
        properties["pointerInputHandler"] = pointerInputHandler
    }
}
