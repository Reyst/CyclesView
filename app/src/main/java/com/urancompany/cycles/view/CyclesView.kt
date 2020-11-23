package com.urancompany.cycles.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.graphics.transform
import kotlin.math.cos
import kotlin.math.sin

enum class BorderType {
    ROUND_IN, ROUND_OUT, ANGLE_IN, ANGLE_OUT, NONE
}

class CyclesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    init {
        initValues()
    }


    private var currentAngle: Float = 0F

    private var baseHeight: Int = 0
    private var baseWidth: Int = 0

    private var centerX: Float = 0F
    private var centerY: Float = 0F

    private var ringWidth: Float = 100F

    private var daysInCycle: Int = 28
//        set(value) {
//            field = value
//            updateAngles()
//        }

    private var anglePerDay: Float = 360 / 32F

    private val periodAngles = mutableListOf<Float>()

    private val circleColors = listOf(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)

    private val circlePaint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            style = Paint.Style.FILL
            strokeWidth = 2F
        }

    private val circleParts = mutableListOf<Path>()
    private val pathMatrix = Matrix()


    private fun initValues() {



    }


    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h != oldh) {
            baseHeight = h
            baseWidth = w

            recalculateRing()
        }
    }

    private fun recalculateRing() {
        circleParts.clear()

        val h = baseHeight
        val w = baseWidth

        val outRadius = h / 2F

        circleParts.addAll(
            listOf(
                createRingPart(outRadius, 0F, 80F, startBorder = BorderType.ANGLE_IN),
                createRingPart(outRadius, 80F, 60F),
                createRingPart(outRadius, 140F, 80F),
                createRingPart(outRadius, 220F, 139F, endBorder = BorderType.ANGLE_OUT),
                //createRingPart(outRadius, -90F, 180F, endBorder = BorderType.ANGLE_IN),
            )

        )

        val startX = w - h.toFloat()
        centerX = startX + h / 2
        centerY = h / 2F

        pathMatrix.reset()
        //        pathMatrix.preRotate(ANGLE_OFFSET)
        pathMatrix.postTranslate(centerX, centerY)
    }

    private fun createRingPart(
        outerRadius: Float,
        startAngle: Float,
        sweepAngle: Float,
        startBorder: BorderType = BorderType.ROUND_OUT,
        endBorder: BorderType = BorderType.NONE,
    ): Path {

        val outOval = RectF(-outerRadius, -outerRadius, outerRadius, outerRadius)
        val innerRadius = outerRadius - ringWidth
        val innerOval = RectF(-innerRadius, -innerRadius, innerRadius, innerRadius)

        val path = Path()

        path.arcTo(outOval, startAngle, sweepAngle)

        when (endBorder) {
            BorderType.ROUND_IN -> path.drawRoundInEnd(outerRadius, startAngle + sweepAngle)
            BorderType.ROUND_OUT -> path.drawRoundOutEnd(outerRadius, startAngle + sweepAngle)
            BorderType.ANGLE_IN -> path.drawAngleInEnd(startAngle + sweepAngle)
            BorderType.ANGLE_OUT -> path.drawAngleOutEnd(startAngle + sweepAngle)
            else -> Unit
        }

        path.arcTo(innerOval, startAngle + sweepAngle, -sweepAngle)

        when (startBorder) {
            BorderType.ROUND_IN -> path.drawRoundInStart(outerRadius, startAngle)
            BorderType.ROUND_OUT -> path.drawRoundOutStart(outerRadius, startAngle)
            BorderType.ANGLE_IN -> path.drawAngleInStart(startAngle)
            BorderType.ANGLE_OUT -> path.drawAngleOutStart(startAngle)
            else -> Unit
        }

        path.close()

        return path
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawARGB(80, 102, 204, 255) // @oleksenko: remove
        circleParts
            .forEachIndexed { index, path ->
                val colorIndex = index % circleColors.size
                circlePaint.color = circleColors[colorIndex]
                path.transform(pathMatrix)
                canvas?.drawPath(path, circlePaint)
            }

    }

    private fun Path.drawRoundInEnd(radius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, radius)
        arcTo(oval, angle, -180F)
    }

    private fun Path.drawRoundOutEnd(radius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, radius)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundInStart(radius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, radius)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundOutStart(radius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, radius)
        arcTo(oval, angle, -180F)
    }

    private fun getRoundEndOval(angle: Float, radius: Float): RectF {
        val angleR = Math.toRadians(angle.toDouble())

        val roundRadius = (ringWidth) / 2F
        val src = RectF(-roundRadius, -roundRadius, roundRadius, roundRadius)
        val dstMatrix = Matrix()
        val dx = (radius - roundRadius) * cos(angleR)
        val dy = (radius - roundRadius) * sin(angleR)
        dstMatrix.setTranslate(dx.toFloat(), dy.toFloat())
        return src.transform(dstMatrix)
    }

    private fun Path.drawAngleOutEnd(angle: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        rLineTo(-offsetX1, offsetY1)

        val offsetX2: Float = offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(offsetX2, offsetY2)

    }

    private fun Path.drawAngleInEnd(angle: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        rLineTo(-offsetX1, -offsetY1)

        val offsetX2: Float = offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(-offsetX2, offsetY2)
    }

    private fun Path.drawAngleOutStart(angle: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        rLineTo(offsetX1, -offsetY1)

        val offsetX2: Float = offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(offsetX2, offsetY2)

    }

    private fun Path.drawAngleInStart(angle: Float) {

        val angleR = Math.toRadians(angle.toDouble())
        val roundRadius = ringWidth / 2F

        val offsetX1: Float = (roundRadius * cos(angleR) - roundRadius * sin(angleR)).toFloat()
        val offsetY1: Float = (roundRadius * cos(angleR) + roundRadius * sin(angleR)).toFloat()
        rLineTo(offsetX1, offsetY1)

        val offsetX2: Float = -offsetY1
        val offsetY2: Float = offsetX1
        rLineTo(offsetX2, offsetY2)
    }


    companion object {
        private const val ANGLE_OFFSET = -45F
    }

}

