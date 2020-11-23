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

    private var currentAngle: Float = 0F

    private var baseHeight: Int = 0
    private var baseWidth: Int = 0

    private var centerX: Float = 0F
    private var centerY: Float = 0F

    private var ringWidth: Float = 100F

    private var handleOuterRadius: Float = 150F
    private var handleInnerRadius: Float = 16F

    //    var selectedDay: Int = 0
    //    private set

    var daysInCycle: Int = DEFAULT_CYCLE_LENGTH
        set(value) {
            field = value.takeIf { it in LENGTH_RANGE } ?: DEFAULT_CYCLE_LENGTH
            updateAngles()
            invalidate()
        }

    private var anglePerDay: Float = 360F / 32

    private var phaseAngles = emptyList<Float>()

    private val circleColors = mutableListOf<Int>()

    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 1F
    }

    private val circleParts = mutableListOf<Path>()
    private val pathMatrix = Matrix()

    private var handleOuterRound: RectF = RectF()
    private var handleInnerRound: RectF = RectF()

    private var handleOuterColor = Color.BLACK
    private var handleInnerColor = Color.WHITE

    init {
        //daysInCycle = 28
        val attributes = context.theme.obtainStyledAttributes(attrs, R.styleable.CyclesView, 0, 0)
        with(attributes) {
            try {
                ringWidth = getDimension(R.styleable.CyclesView_ring_width, 100F)
                circleColors.clear()
                circleColors.add(getColor(R.styleable.CyclesView_color1, Color.RED))
                circleColors.add(getColor(R.styleable.CyclesView_color2, Color.YELLOW))
                circleColors.add(getColor(R.styleable.CyclesView_color3, Color.BLUE))
                circleColors.add(getColor(R.styleable.CyclesView_color4, Color.GREEN))

                handleOuterRadius = getDimension(R.styleable.CyclesView_handle_outer_radius, ringWidth * 0.7F)
                handleInnerRadius = getDimension(R.styleable.CyclesView_handle_inner_radius, 16F)

                handleOuterColor = getColor(R.styleable.CyclesView_handle_outer_color, Color.BLACK)
                handleInnerColor = getColor(R.styleable.CyclesView_handle_inner_color, Color.WHITE)

            } finally {
                recycle()
            }
        }
        updateAngles()
    }

    private fun updateAngles() {
        val lengthInDays = 1 + (daysInCycle.takeIf { it > 31 } ?: 31)
        anglePerDay = 360F / lengthInDays

        val phases = obtainPhasesLengthsByDuration(daysInCycle)
        phaseAngles = phases.toAngles(anglePerDay)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h != oldh) {
            baseHeight = h
            baseWidth = w

            recalculate()
        }
    }

    private fun recalculate() {
        circleParts.clear()

        val h = baseHeight
        val w = baseWidth

        val outRadius = h / 2F

        circleParts.addAll(
            listOf(
                createRingPart(outRadius, 0F, phaseAngles[0], startBorder = BorderType.ANGLE_IN),
                createRingPart(outRadius, phaseAngles[0], phaseAngles[1]),
                createRingPart(outRadius, phaseAngles[0] + phaseAngles[1], phaseAngles[2]),
                createRingPart(outRadius, phaseAngles[0] + phaseAngles[1] + phaseAngles[2],  phaseAngles[3], endBorder = BorderType.ANGLE_OUT),
                //createRingPart(outRadius, -90F, 180F, endBorder = BorderType.ANGLE_IN),
            )
        )

        val startX = w - h.toFloat()
        centerX = startX + h / 2
        centerY = h / 2F

        pathMatrix.reset()
        pathMatrix.postTranslate(centerX, centerY)

        handleOuterRound = getRectForHandleOval(ANGLE_OFFSET, outRadius, handleOuterRadius)
            .apply { transform(pathMatrix) }

        handleInnerRound = getRectForHandleOval(ANGLE_OFFSET, outRadius, handleInnerRadius)
            .apply { transform(pathMatrix) }

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

        pathMatrix.preRotate(ANGLE_OFFSET + currentAngle)

        circleParts
            .forEachIndexed { index, path ->
                val colorIndex = index % circleColors.size
                mainPaint.color = circleColors[colorIndex]
                path.transform(pathMatrix)
                canvas?.drawPath(path, mainPaint)
            }

        mainPaint.color = handleOuterColor
        canvas?.drawOval(handleOuterRound, mainPaint)

        mainPaint.color = handleInnerColor
        canvas?.drawOval(handleInnerRound, mainPaint)

    }

    private fun Path.drawRoundInEnd(radius: Float, angle: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, -180F)
    }

    private fun Path.drawRoundOutEnd(radius: Float, angle: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundInStart(radius: Float, angle: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundOutStart(radius: Float, angle: Float) {
        val oval = getRectForOval(angle, radius, ringWidth / 2F)
        arcTo(oval, angle, -180F)
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

    private fun getRectForHandleOval(angle: Float, outerRadius: Float, roundRadius: Float): RectF {
        val angleR = Math.toRadians(angle.toDouble())
        val src = RectF(-roundRadius, -roundRadius, roundRadius, roundRadius)

        val dstMatrix = Matrix()
        val dx = (outerRadius - ringWidth / 2F) * cos(angleR)
        val dy = (outerRadius - ringWidth / 2F) * sin(angleR)
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
        private const val DEFAULT_CYCLE_LENGTH = 28
        private val LENGTH_RANGE = 20..42
    }

    private data class PhasesLengths(
        val phase1: Int,
        val phase2: Int,
        val phase3: Int,
        val phase4: Int,
    )

    private fun PhasesLengths.toAngles(dayAngle : Float) = listOf(
        phase1 * dayAngle,
        phase2 * dayAngle,
        phase3 * dayAngle,
        phase4 * dayAngle,
    )

    private fun obtainPhasesLengthsByDuration(duration: Int): PhasesLengths {

        val ph1: Int = when(duration) {
            20 -> 4
            in 21..25 -> 5
            in 26..31 -> 6
            in 32..36 -> 7
            else -> 8
        }

        val ph2: Int = when (duration) {
            20, 21 -> 7
            22, 23 -> 8
            24, 25, 26 -> 9
            27, 28 -> 10
            in 29..32 -> 11
            33, 34 -> 12
            in 35..38 -> 13
            39 -> 14
            else -> 15
        }

        val ph3: Int = when(duration) {
            20 -> 3
            in 21..24 -> 4
            in 25..30 -> 5
            in 31..35 -> 6
            else -> 7
        }

        val ph4: Int = duration - ph1 - ph2 - ph3

        return PhasesLengths(ph1, ph2, ph3, ph4)
    }
}

