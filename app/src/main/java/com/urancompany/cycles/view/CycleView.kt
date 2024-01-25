package com.urancompany.cycles.view

import android.animation.Animator
import android.animation.ObjectAnimator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.annotation.WorkerThread
import androidx.core.graphics.transform
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin

class CycleView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var currentAngle: Float = 0F
        set(value) {
            field = value
            updateDay()
        }

    var selectedDay: Int = 1
        private set(value) {
            if (field != value && value in 1..cycle.duration) {
                field = value
                post { daySelectListener?.onCycleDaySelected(cycle, value) }
            }
        }

    private var maxAngle: Float = 360F

    private var outRadius : Float = 0F
    private var baseHeight: Int = 0
    private var baseWidth: Int = 0

    private var centerX: Float = 0F
    private var centerY: Float = 0F

    private var ringWidth: Float = 100F

    private var handleOuterRadius: Float = 150F
    private var handleInnerRadius: Float = 16F

    var cycle: Cycle = Cycle(DEFAULT_CYCLE_LENGTH)
        private set(value) {
            if (field.duration != value.duration) {
                field = value
                post { daySelectListener?.onCycleDaySelected(value, selectedDay) }
            }
        }

    var daysInCycle: Int = DEFAULT_CYCLE_LENGTH
        set(value) {
            field = value.takeIf { it in Cycle.AVAILABLE_DURATIONS } ?: DEFAULT_CYCLE_LENGTH
            animateDurationChanging()
            selectedDay = 1
        }


    private var drawDaysInCycle: Int = DEFAULT_CYCLE_LENGTH
        set(value) {
            field = (value.takeIf { it in Cycle.AVAILABLE_DURATIONS } ?: DEFAULT_CYCLE_LENGTH)
                .also { cycle = Cycle(it) }

            recalculate()
            invalidate()
        }

    private var anglePerDay: Float = 360F / 32

    private val circleColors = mutableListOf<Int>()

    private val mainPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        strokeWidth = 1F
    }

    private val _circleParts = mutableListOf<Path>()
    private val circleParts
        get() = _circleParts.map { Path(it) }

    private var _handleOuterRound: RectF = RectF()
    private var handleOuterRound: RectF = RectF()

    private var _handleInnerRound: RectF = RectF()
    private var handleInnerRound: RectF = RectF()

    private var handleOuterColor = Color.BLACK
    private var handleInnerColor = Color.WHITE

    private val rotation = Matrix()

    private var isHandleTouched = false
    private var directionSign: Int = 0
    private var baseY = 0F

    private var angleOffset: Float = DEFAULT_ANGLE_OFFSET

    private var rotationThread: Thread? = null

    private var daySelectListener: OnCycleDaySelectedListener? = null

    private val settingsChangeListener = {
        if (daysInCycle !in Cycle.AVAILABLE_DURATIONS)
            daysInCycle = DEFAULT_CYCLE_LENGTH
    }

    init {
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

                angleOffset = getFloat(R.styleable.CyclesView_angle_offset, DEFAULT_ANGLE_OFFSET)

                handleOuterColor = getColor(R.styleable.CyclesView_handle_outer_color, Color.BLACK)
                handleInnerColor = getColor(R.styleable.CyclesView_handle_inner_color, Color.WHITE)

            } finally {
                recycle()
            }
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h != oldh) {

            baseHeight = h - paddingBottom - paddingTop
            baseWidth = w - paddingEnd

            recalculate()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        CycleSettings.addSettingsChangeListener(settingsChangeListener)
    }

    override fun onDetachedFromWindow() {
        CycleSettings.removeSettingsChangeListener(settingsChangeListener)
        super.onDetachedFromWindow()
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onTouchEvent(event: MotionEvent?): Boolean {

        if (!isEnabled) {
            parent.requestDisallowInterceptTouchEvent(false)
            return super.onTouchEvent(event)
        }

        if (event != null) {
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> {
                    isHandleTouched = _handleOuterRound.contains(event.x, event.y)
                    parent.requestDisallowInterceptTouchEvent(isHandleTouched)
                    directionSign = 0
                    baseY = event.y

                    if (isHandleTouched) {
                        rotationThread = Thread { updateCurrentAngle() }.apply { start() }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    parent.requestDisallowInterceptTouchEvent(false)
                    updateHandlePosition()
                    isHandleTouched = false
                    directionSign = 0
                    rotationThread?.interrupt()
                }
                MotionEvent.ACTION_MOVE -> {
                    parent.requestDisallowInterceptTouchEvent(isHandleTouched)
                    directionSign = -baseY.compareTo(event.y)
                    updateHandlePosition(event.y - baseY)
                }
            }
        }

        return if (isHandleTouched) true else super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        mainPaint.clearShadowLayer()
        rotation.reset()
        rotation.preRotate(angleOffset + currentAngle, centerX, centerY)

        circleParts.forEachIndexed { index, path ->
            val colorIndex = index % circleColors.size
            mainPaint.color = circleColors[colorIndex]

            path.transform(rotation)
            canvas.drawPath(path, mainPaint)
        }

        mainPaint.color = handleOuterColor
        mainPaint.setShadowLayer(4F, 4F,4F, Color.BLACK)
        canvas.drawOval(handleOuterRound, mainPaint)

        mainPaint.color = handleInnerColor
        mainPaint.clearShadowLayer()
        canvas.drawOval(handleInnerRound, mainPaint)
    }


    private fun calculateAngleDay(daysAmount: Int): Float {
        val lengthInDays =
            when {
                daysAmount < 31 -> 32
                else -> 2 + daysAmount
            }

        return 360F / lengthInDays
    }

    private fun recalculate() {
        anglePerDay = calculateAngleDay(drawDaysInCycle)

        val h = baseHeight
        val w = baseWidth

        val outerPartOfHandle = maxOf(0F, 8 + handleOuterRadius - (ringWidth / 2F))
        outRadius = (h / 2F) - outerPartOfHandle

        val startX = w - h.toFloat()
        centerX = startX + h / 2
        centerY = paddingTop + h / 2F

        val pathMatrix = Matrix().apply { postTranslate(centerX, centerY) }

        val angles: List<Pair<Float, Float>> = calculatePathAngles(cycle)
        fillParts(angles)

        maxAngle = cycle.duration * anglePerDay

        _handleOuterRound = getRectForHandleOval(angleOffset, outRadius, handleOuterRadius)
            .transform(pathMatrix)

        _handleInnerRound = getRectForHandleOval(angleOffset, outRadius, handleInnerRadius)
            .transform(pathMatrix)

        updateHandlePosition()
    }

    private fun fillParts(angles: List<Pair<Float, Float>>) {

        val pathMatrix = Matrix()
        pathMatrix.postTranslate(centerX, centerY)

        _circleParts.clear()
        angles
            .mapIndexed { index, pair ->
                val sb = if (index == 0) BorderType.ANGLE_IN else BorderType.ROUND_OUT
                val eb = if (index == 3) BorderType.ANGLE_OUT else BorderType.NONE

                createRingPart(
                    outRadius,
                    startAngle = pair.first,
                    sweepAngle = pair.second,
                    startBorder = sb,
                    endBorder = eb,
                )
            }
            .map {
                it.transform(pathMatrix)
                Path(it)
            }
            .also { _circleParts.addAll(it) }
    }

    private fun calculatePathAngles(cycle: Cycle, dayAngle: Float = anglePerDay): List<Pair<Float, Float>> {
        return listOf(
            cycle.phase1.daysBefore * dayAngle to cycle.phase1.length * dayAngle,
            cycle.phase2.daysBefore * dayAngle to cycle.phase2.length * dayAngle,
            cycle.phase3.daysBefore * dayAngle to cycle.phase3.length * dayAngle,
            cycle.phase4.daysBefore * dayAngle to cycle.phase4.length * dayAngle,
        )
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

    private fun updateHandlePosition(offset: Float = 0F) {
        handleOuterRound = RectF(_handleOuterRound)
        handleInnerRound = RectF(_handleInnerRound)

        if (isHandleTouched) {
            val distance = minOf(abs(offset), ringWidth / 4F)
            val handleMatrix = Matrix().apply {
                setTranslate(0F, directionSign * distance)
            }
            handleOuterRound.transform(handleMatrix)
            handleInnerRound.transform(handleMatrix)
        }
    }

    @WorkerThread
    private fun updateCurrentAngle() {
        try {
            while (isHandleTouched) {
                currentAngle -= directionSign * (anglePerDay / 3)
                if (currentAngle < -maxAngle) currentAngle = -maxAngle
                if (currentAngle > 0) currentAngle = 0F

                Thread.sleep(50L)

                postInvalidate()
            }
        } catch (e: InterruptedException) {
            postInvalidate()
        }
    }

    private fun updateDay() {
        selectedDay = 1 + abs(currentAngle / anglePerDay).toInt()
    }

    fun setOnCycleDaySelectedListener(listener: OnCycleDaySelectedListener?) {
        daySelectListener = listener
    }

    fun selectDay(day: Int) {
        if (day in 1..cycle.duration) {

            val oldAngle = currentAngle
            val newAngle = -(day - 1) * anglePerDay

            selectedDay = day

            val anim = ObjectAnimator.ofFloat(this, "currentAngle", oldAngle, newAngle)
            anim.duration = ANIMATION_DURATION
            anim.addUpdateListener { invalidate() }

            anim.start()
        }
    }

    private fun animateDurationChanging() {

        val filledAngle = cycle.duration * anglePerDay
        val newCycle = Cycle(daysInCycle)
        //        val angleOffset = currentAngle

        val startAngleDay = filledAngle / newCycle.duration
        val dayAngleDelta = calculateAngleDay(daysInCycle) - startAngleDay

        val anim = ValueAnimator.ofFloat(0F, 1F)
        anim.duration = ANIMATION_DURATION
        anim.addUpdateListener { animator ->

            val k = animator.animatedFraction
            //            currentAngle = angleOffset - angleOffset * k
            val iDayAngle = startAngleDay + dayAngleDelta * k
            fillParts(calculatePathAngles(newCycle, iDayAngle))
            invalidate()
        }

        anim.addListener(object : Animator.AnimatorListener {
            override fun onAnimationStart(animation: Animator) = Unit
            override fun onAnimationRepeat(animation: Animator) = Unit
            override fun onAnimationEnd(animation: Animator) { drawDaysInCycle = daysInCycle }
            override fun onAnimationCancel(animation: Animator) { drawDaysInCycle = daysInCycle }
        })

        anim.start()
    }

    companion object {
        private const val DEFAULT_ANGLE_OFFSET = -45F
        private const val ANIMATION_DURATION = 500L

        private val DEFAULT_CYCLE_LENGTH: Int
            get() = Cycle.AVAILABLE_DURATIONS.first()
    }
}
