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

    //    private var currentAngle: Float = 0F

    private var centerX: Float = 0F
    private var centerY: Float = 0F

    private var startX: Float = 0F
    private var startY: Float = 0F


    private var ringWidth: Float = 100F

    private val colors = listOf(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            style = Paint.Style.FILL
            strokeWidth = 2F
        }

    private val circleParts = mutableListOf<Path>()
    private val pathMatrix = Matrix()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if (h != oldh) {

            circleParts.clear()

            val outRadius = h / 2F

            circleParts.addAll(
                listOf(
                    createRingPart(outRadius, 10F, 70F),
                    createRingPart(outRadius, 80F, 60F),
                    createRingPart(outRadius, 140F, 80F),
                    createRingPart(outRadius, 220F, 120F),
                )

            )

            startX = w - h.toFloat()
            centerX = startX + h / 2
            centerY = h / 2F

            pathMatrix.reset()
            pathMatrix.preRotate(-45F)
            pathMatrix.postTranslate(centerX, centerY)
        }
    }

    private fun createRingPart(
        outerRadius: Float,
        startAngle: Float,
        sweepAngle: Float,
        startBorder: BorderType = BorderType.ROUND_OUT,
        endBorder: BorderType = BorderType.ROUND_IN,
    ): Path {

        val outOval = RectF(-outerRadius, -outerRadius, outerRadius, outerRadius)
        val innerRadius = outerRadius - ringWidth
        val innerOval = RectF(-innerRadius, -innerRadius, innerRadius, innerRadius)

        val path = Path()

        path.arcTo(outOval, startAngle, sweepAngle)

        when(endBorder) {
            BorderType.ROUND_IN -> path.drawRoundInEnd(outerRadius, startAngle + sweepAngle)
            BorderType.ROUND_OUT -> path.drawRoundOutEnd(outerRadius, startAngle + sweepAngle)
            BorderType.ANGLE_IN -> Unit
            BorderType.ANGLE_OUT -> Unit
            else -> Unit
        }

        path.arcTo(innerOval, startAngle + sweepAngle, -sweepAngle)


        when(startBorder) {
            BorderType.ROUND_IN -> path.drawRoundInStart(outerRadius, startAngle)
            BorderType.ROUND_OUT -> path.drawRoundOutStart(outerRadius, startAngle)
            BorderType.ANGLE_IN -> Unit
            BorderType.ANGLE_OUT -> Unit
            else -> Unit
        }


        path.close()

        return path
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        canvas?.drawARGB(80, 102, 204, 255)
        circleParts
            .forEachIndexed { index, path ->
            val colorIndex = index % colors.size
            paint.color = colors[colorIndex]
            path.transform(pathMatrix)
            canvas?.drawPath(path, paint)
        }

    }

    private fun Path.drawRoundInEnd(outerRadius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, outerRadius)
        arcTo(oval, angle, -180F)
    }

    private fun Path.drawRoundOutEnd(outerRadius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, outerRadius)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundInStart(outerRadius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, outerRadius)
        arcTo(oval, angle, 180F)
    }

    private fun Path.drawRoundOutStart(outerRadius: Float, angle: Float) {
        val oval = getRoundEndOval(angle, outerRadius)
        arcTo(oval, angle, -180F)
    }

    private fun getRoundEndOval(angle: Float, outerRadius: Float): RectF {
        val angleR = Math.toRadians(angle.toDouble())

        val roundRadius = (ringWidth) / 2F
        val src = RectF(-roundRadius, -roundRadius, roundRadius, roundRadius)
        val dstMatrix = Matrix()
        val dx = (outerRadius - roundRadius) * cos(angleR)
        val dy = (outerRadius - roundRadius) * sin(angleR)
        dstMatrix.setTranslate(dx.toFloat(), dy.toFloat())
        return src.transform(dstMatrix)
    }

}
