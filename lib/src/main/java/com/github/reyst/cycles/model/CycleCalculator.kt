package com.github.reyst.cycles.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import com.github.reyst.cycles.utils.radians
import kotlin.math.cos
import kotlin.math.sin

class CycleGraphicsCalc(
    private val baseWidth: Float,
    private val baseHeight: Float,
) {

    constructor(width: Int, height: Int) : this(width.toFloat(), height.toFloat())

    fun calculate(
        ringWidth: Float,
        handleOuterRadius: Float,
        angleOffset: Float = 0F,
    ): CycleGraphicsData {

        val outerPartOfHandle = maxOf(0F, handleOuterRadius - (ringWidth / 2F))
        val outRadius = (baseHeight / 2F) - outerPartOfHandle

        val startX = baseWidth - baseHeight

        val centerX = startX + baseHeight / 2
        val centerY = baseHeight / 2F

        val baseDistance = outRadius - handleOuterRadius / 2

        val radiansOffset = radians(angleOffset)
        val handleCenterX = centerX + baseDistance * cos(radiansOffset)
        val handleCenterY = centerY + baseDistance * sin(radiansOffset)


        return CycleGraphicsData(
            Size(baseWidth, baseHeight),
            Offset(centerX, centerY),
            Offset(handleCenterX, handleCenterY),
            outRadius,
        )
    }
}

data class CycleGraphicsData(
    val size: Size,
    val center: Offset,
    val handleCenter: Offset,
    val outRadius: Float = 0F,
)
