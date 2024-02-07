package com.github.reyst.cycles.ui.compose

import android.util.Log
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.PointerEvent
import androidx.compose.ui.input.pointer.PointerEventPass
import androidx.compose.ui.input.pointer.PointerInputChange
import androidx.compose.ui.node.ModifierNodeElement
import androidx.compose.ui.node.PointerInputModifierNode
import androidx.compose.ui.platform.InspectorInfo
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor

fun Modifier.shiftable(
    onTouch: (x: Float, y: Float) -> Unit = { _, _ -> },
    onRelease: () -> Unit = { },
    callback: (x: Float, y: Float) -> Unit,
) = this then ShiftableElement(onTouch, onRelease, callback)

class ShiftableElement(
    private val onTouch: (x: Float, y: Float) -> Unit,
    private val onRelease: () -> Unit,
    private val callback: (x: Float, y: Float) -> Unit,
) : ModifierNodeElement<OnShiftEventNode>() {

    override fun create() = OnShiftEventNode(onTouch, onRelease, callback)
    override fun update(node: OnShiftEventNode) {
        node.onTouch = onTouch
        node.onRelease = onRelease
        node.callback = callback
    }

    override fun InspectorInfo.inspectableProperties() {
        name = "onShiftEvent"
        properties["onTouch"] = onTouch
        properties["onRelease"] = onRelease
        properties["callback"] = callback
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ShiftableElement

        if (onTouch != other.onTouch) return false
        if (onRelease != other.onRelease) return false
        return callback == other.callback
    }

    override fun hashCode(): Int {
        var result = onTouch.hashCode()
        result = 31 * result + onRelease.hashCode()
        result = 31 * result + callback.hashCode()
        return result
    }


}

class OnShiftEventNode(
    var onTouch: (x: Float, y: Float) -> Unit,
    var onRelease: () -> Unit,
    var callback: (x: Float, y: Float) -> Unit,
) : PointerInputModifierNode, Modifier.Node() {

    private var eventChannel: SendChannel<PointerInputChange> =
        Channel<PointerInputChange>(Channel.CONFLATED).apply { close() }

    @OptIn(ObsoleteCoroutinesApi::class)
    override fun onPointerEvent(
        pointerEvent: PointerEvent,
        pass: PointerEventPass,
        bounds: IntSize,
    ) {
        if (pass == PointerEventPass.Initial) {
            val changes = pointerEvent.changes.first()
            when {
                changes.pressed && !changes.previousPressed -> {

                    eventChannel = coroutineScope.actor(
                        capacity = Channel.CONFLATED,
                        onCompletion = { onRelease() }
                    ) {
                        handleEvent(changes, onTouch)
                        for (change in channel) handleEvent(change, callback)
                    }
                }

                changes.pressed && changes.previousPressed -> eventChannel.trySend(changes)
                !changes.pressed && changes.previousPressed -> eventChannel.close()
            }
        }
    }

    private fun handleEvent(changes: PointerInputChange, action: (x: Float, y: Float) -> Unit) {
        val (x, y) = changes.position
        action(x, y)
    }

    override fun onCancelPointerInput() {
        Log.wtf("SHIFTABLE", "onCancelPointerInput()")
        // Do nothing
    }
}
