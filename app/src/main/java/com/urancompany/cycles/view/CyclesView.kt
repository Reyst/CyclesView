package com.urancompany.cycles.view

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CyclesView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0,
) : View(context, attrs, defStyleAttr) {

    private var currentAngle: Float = 0F
    private var centerX: Float = 0F
    private var centerY: Float = 0F

    private var startX: Float = 0F
    private var startY: Float = 0F

    private val colors = listOf(Color.RED, Color.YELLOW, Color.GREEN, Color.BLUE)


    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        .apply {
            style = Paint.Style.STROKE
            strokeWidth = 10F
        }

    private val circleParts = mutableListOf<Path>()
    private val pathMatrix = Matrix()

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        if(h != oldh) {

            circleParts.clear()

            val outRadius = (h - 10) / 2F
            val oval = RectF(-outRadius, -outRadius, outRadius, outRadius)

            val path1 = Path()
            path1.addArc(oval, 0F, 80F)
            circleParts.add(path1)

            val path2 = Path()
            path2.addArc(oval, 80F, 60F)
            circleParts.add(path2)

            val path3 = Path()
            path3.addArc(oval, 140F, 80F)
            circleParts.add(path3)

            val path4 = Path()
            path4.addArc(oval, 220F, 90F)
            circleParts.add(path4)

            startX = w - h.toFloat()
            centerX = startX + h / 2
            centerY = h / 2F

            pathMatrix.reset()
            pathMatrix.preTranslate(centerX, centerY)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        circleParts.forEachIndexed { index, path ->
            val colorIndex = index % colors.size
            paint.color = colors[colorIndex]
            path.transform(pathMatrix)
            canvas?.drawPath(path, paint)
        }

    }



}