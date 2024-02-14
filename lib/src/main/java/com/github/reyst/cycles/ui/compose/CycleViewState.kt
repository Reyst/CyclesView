package com.github.reyst.cycles.ui.compose

import androidx.compose.animation.core.LinearEasing
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

    var angles by mutableStateOf(calculateAngles())
        private set

    private val maxAngle
        get() = cycle.duration * stateData.anglePerDay - 1

    private fun calculateAngles(
        cycle: Cycle = stateData.cycle,
        dayAngle: Float = stateData.anglePerDay,
    ) = cycle.phases.map { it.daysBefore * dayAngle to it.end * dayAngle }


    fun updateAngle(k: Float) {
        val angleStep = stateData.anglePerDay * k
        stateData.angle = (angle - angleStep).coerceIn(-maxAngle, 0F)
    }

    private fun getAngleForDay(day: Int): Float = -(day - 1) * stateData.anglePerDay

    fun setDay(day: Int) {
        if (day in 1..cycle.duration) stateData.angle = getAngleForDay(day)
    }

    suspend fun selectDay(day: Int, duration: Int = 500) {
        if (day in 1..cycle.duration) {
            animate(
                initialValue = stateData.angle,
                targetValue = getAngleForDay(day),
                animationSpec = tween(durationMillis = duration, easing = LinearOutSlowInEasing)
            ) { value, _ -> stateData.angle = value }
        }
    }

    fun setCycleDuration(days: Int) = setCycle(Cycle(days))

    suspend fun resizeCycleTo(days: Int, duration: Int = 500) {

        val filledAngle = cycle.duration * stateData.anglePerDay
        val newCycle = Cycle(days)

        val startAngleDay = filledAngle / newCycle.duration
        val dayAngleDelta = stateData.calculateAngleDay(days) - startAngleDay

        animate(
            initialValue = 0F,
            targetValue = 1F,
            animationSpec = tween(durationMillis = duration, easing = LinearEasing),
        ) { value, _ ->
            val angle = startAngleDay + dayAngleDelta * value
            stateData.anglePerDay = angle
            angles = calculateAngles(newCycle, angle)
        }

        setCycle(newCycle)
    }

    private fun setCycle(cycle: Cycle) {
        stateData.cycle = cycle
        stateData.anglePerDay = stateData.calculateAngleDay(cycle.duration)
        angles = calculateAngles()
        setDay(stateData.day.takeIf { it <= cycle.duration } ?: 1)
    }
}

internal class StateData(duration: Int) {

    var cycle = Cycle(duration)
    var anglePerDay: Float = calculateAngleDay(duration)

    var angle by mutableFloatStateOf(0F)

    val day: Int by derivedStateOf { 1 + abs(angle / anglePerDay).toInt() }

    var handleShift by mutableFloatStateOf(0F)
    var isHandlePressed by mutableStateOf(false)
    var handlePressedAt by mutableFloatStateOf(0F)

    internal fun calculateAngleDay(daysAmount: Int): Float {
        val lengthInDays = if (daysAmount < 31) 32 else 2 + daysAmount
        return 360F / lengthInDays
    }
}

