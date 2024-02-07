package com.github.reyst.cycles.ui.compose

import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.github.reyst.cycles.model.Cycle
import kotlin.math.abs

@Composable
fun rememberCycleViewState(
    duration: Int = 28,
): CycleViewState = remember { CycleViewState(duration) }


class CycleViewState private constructor(private val stateData: StateData) {

    constructor(duration: Int) : this(StateData(duration))

    val cycle
        get() = stateData.cycle

    val day
        get() = stateData.day


    val angle
        get() = stateData.angle

    var handleShift
        get() = stateData.handleShift
        set(value) {
            stateData.handleShift = value
        }

    var isHandlePressed
        get() = stateData.isHandlePressed
        set(value) {
            stateData.isHandlePressed = value
            handleShift = 0F
        }


    var handlePressedAt
        get() = stateData.handlePressedAt
        set(value) {
            stateData.handlePressedAt = value
        }

    val angles: List<Pair<Float, Float>>
        get() = cycle
            .phases
            .map { it.daysBefore * stateData.anglePerDay to it.length * stateData.anglePerDay }

    private val maxAngle = cycle.duration * stateData.anglePerDay - 1

    fun updateAngle(k: Float) {
        val angleStep = stateData.anglePerDay * k
        stateData.angle = (angle - angleStep).coerceIn(-maxAngle, 0F)
    }

    suspend fun setDay(day: Int) = setDayTo(day, 0)
    suspend fun setDayTo(day: Int, duration: Int = 1_000) {

        if (day in 1..cycle.duration) {
            val newAngle = -(day-1) * stateData.anglePerDay
            if (duration == 0) {
                stateData.angle = newAngle
            } else {
                animate(
                    initialValue = stateData.angle,
                    targetValue = newAngle,
                    animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing)
                ) { value, velocity ->
                    stateData.angle = value
                }
            }
        }
    }

}

internal class StateData(
    duration: Int,
) {

    var cycle by mutableStateOf(Cycle(duration))
    var anglePerDay: Float = calculateAngleDay(duration)
        private set

    var angle by mutableFloatStateOf(0F)

    val day: Int by derivedStateOf { 1 + abs(angle / anglePerDay).toInt() }

    var handleShift by mutableFloatStateOf(0F)
    var isHandlePressed by mutableStateOf(false)
    var handlePressedAt by mutableFloatStateOf(0F)


    private fun calculateAngleDay(daysAmount: Int): Float {
        val lengthInDays = when {
            daysAmount < 31 -> 32
            else -> 2 + daysAmount
        }

        return 360F / lengthInDays
    }
}
