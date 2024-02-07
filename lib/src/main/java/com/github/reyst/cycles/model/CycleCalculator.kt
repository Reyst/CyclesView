package com.github.reyst.cycles.model

import android.graphics.Matrix
import android.graphics.Path
import android.graphics.RectF
import androidx.core.graphics.transform
import com.github.reyst.cycles.ui.BorderType
import kotlin.math.cos
import kotlin.math.sin

class CycleGraphicsCalc(
    private val baseWidth: Float,
    private val baseHeight: Float,
) {

    constructor(width: Int, height: Int) : this(width.toFloat(), height.toFloat())

    fun calculate(
        angles: List<Pair<Float, Float>>,
        ringWidth: Float,
        handleOuterRadius: Float,
        handleInnerRadius: Float,
        angleOffset: Float = 0F,
    ): CycleGraphicsData {

        val outerPartOfHandle = maxOf(0F, handleOuterRadius - (ringWidth / 2F))
        val outRadius = (baseHeight / 2F) - outerPartOfHandle

        val startX = baseWidth - baseHeight

        val centerX = startX + baseHeight / 2
        val centerY = baseHeight / 2F

        val baseDistance = outRadius - handleOuterRadius / 2

        val radiansOffset = Math.toRadians(angleOffset.toDouble())
        val handleCenterX = centerX + baseDistance * cos(radiansOffset)
        val handleCenterY = centerY + baseDistance * sin(radiansOffset)


        return CycleGraphicsData(
            width = baseWidth,
            height = baseHeight,
            centerX = centerX,
            centerY = centerY,
            ringWidth = ringWidth,
            handleInnerRadius = handleInnerRadius,
            handleOuterRadius = handleOuterRadius,
            handleCenterX = handleCenterX.toFloat(),
            handleCenterY = handleCenterY.toFloat(),
            arcs = getArcs(angles, centerX, centerY, outRadius, ringWidth)
        )
    }

    private fun getArcs(
        angles: List<Pair<Float, Float>>,
        centerX: Float,
        centerY: Float,
        outRadius: Float,
        ringWidth: Float,
    ): List<Path> {

        val pathMatrix = Matrix().apply { postTranslate(centerX, centerY) }

        return angles
            .mapIndexed { index, pair ->
                val sb = if (index == 0) BorderType.ANGLE_IN else BorderType.ROUND_OUT
                val eb =
                    if (index == angles.lastIndex) BorderType.ANGLE_OUT else BorderType.NONE

                createRingPart(
                    outRadius,
                    startAngle = pair.first,
                    sweepAngle = pair.second,
                    startBorder = sb,
                    endBorder = eb,
                    ringWidth = ringWidth,
                )
            }
            .map {
                it.transform(pathMatrix)
                Path(it)
            }
    }

    private fun createRingPart(
        outerRadius: Float,
        startAngle: Float,
        sweepAngle: Float,
        startBorder: BorderType = BorderType.ROUND_OUT,
        endBorder: BorderType = BorderType.NONE,
        ringWidth: Float,
    ): Path {

        val outOval = RectF(-outerRadius, -outerRadius, outerRadius, outerRadius)
        val innerRadius = outerRadius - ringWidth
        val innerOval = RectF(-innerRadius, -innerRadius, innerRadius, innerRadius)

        val path = Path()

        path.arcTo(outOval, startAngle, sweepAngle)

        when (endBorder) {
            BorderType.ROUND_IN -> path.drawRoundInEnd(
                outerRadius,
                startAngle + sweepAngle,
                ringWidth
            )

            BorderType.ROUND_OUT -> path.drawRoundOutEnd(
                outerRadius,
                startAngle + sweepAngle,
                ringWidth
            )

            BorderType.ANGLE_IN -> path.drawAngleInEnd(startAngle + sweepAngle, ringWidth)
            BorderType.ANGLE_OUT -> path.drawAngleOutEnd(startAngle + sweepAngle, ringWidth)
            else -> Unit
        }

        path.arcTo(innerOval, startAngle + sweepAngle, -sweepAngle)

        when (startBorder) {
            BorderType.ROUND_IN -> path.drawRoundInStart(outerRadius, startAngle, ringWidth)
            BorderType.ROUND_OUT -> path.drawRoundOutStart(outerRadius, startAngle, ringWidth)
            BorderType.ANGLE_IN -> path.drawAngleInStart(startAngle, ringWidth)
            BorderType.ANGLE_OUT -> path.drawAngleOutStart(startAngle, ringWidth)
            else -> Unit
        }

        path.close()

        return path
    }

    private fun getRectForOval(angle: Float, outerRadius: Float, roundRadius: Float): RectF {
        val angleR = Math.toRadians(angle.toDouble())
        val src = RectF(-roundRadius, -roundRadius, roundRadius, roundRadius)

        val dstMatrix = Matrix()
        val dx = (outerRadius - roundRadius) * cos(angleR)
        val dy = (outerRadius - roundRadius) * sin(angleR)
        dstMatrix.setTranslate(dx.toFloat(), dy.toFloat())
        return src.transform(dstMatrix)
    }

    private fun Path.drawRoundInEnd(radius: Float, angle: Float, ringWidth: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, -180F)
    }

    private fun Path.drawRoundOutEnd(radius: Float, angle: Float, ringWidth: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundInStart(radius: Float, angle: Float, ringWidth: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundOutStart(radius: Float, angle: Float, ringWidth: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, -180F)
    }

    private fun Path.drawAngleOutEnd(angle: Float, ringWidth: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        rLineTo(-offsetX1, offsetY1)

        val offsetX2: Float = offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(offsetX2, offsetY2)

    }

    private fun Path.drawAngleInEnd(angle: Float, ringWidth: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        rLineTo(-offsetX1, -offsetY1)

        val offsetX2: Float = offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(-offsetX2, offsetY2)
    }

    private fun Path.drawAngleOutStart(angle: Float, ringWidth: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        rLineTo(offsetX1, -offsetY1)

        val offsetX2: Float = offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(offsetX2, offsetY2)

    }

    private fun Path.drawAngleInStart(angle: Float, ringWidth: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        rLineTo(offsetX1, offsetY1)

        val offsetX2: Float = -offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(offsetX2, offsetY2)
    }

}

data class CycleGraphicsData(
    val width: Float = 0F,
    val height: Float = 0F,
    val centerX: Float = 0F,
    val centerY: Float = 0F,
    val arcs: List<Path> = emptyList(),
    val handleCenterX: Float = 0F,
    val handleCenterY: Float = 0F,
    val ringWidth: Float = 0F,
    val handleOuterRadius: Float = 0F,
    val handleInnerRadius: Float = 0F,
)
