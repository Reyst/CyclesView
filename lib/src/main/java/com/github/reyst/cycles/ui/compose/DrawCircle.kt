package com.github.reyst.cycles.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import com.github.reyst.cycles.ui.BorderType
import com.github.reyst.cycles.utils.radians
import kotlin.math.cos
import kotlin.math.sin

@Composable
@Preview(showSystemUi = true)
internal fun DrawCirclePreview() {
    DrawCircle(
        center = Offset(500F, 800F),
        angles = listOf(
            30F to 60F,
            60F to 120F,
            120F to 180F,
            180F to 270F,
        ),
        colors = listOf(Color.Blue, Color.Red, Color.Green, Color.Magenta),
        outRadius = 500F,
        ringWidth = 120F
    )
}

@Composable
internal fun DrawCircle(
    center: Offset,
    angles: List<Pair<Float, Float>>,
    colors: List<Color>,
    outRadius: Float,
    ringWidth: Float,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        angles
            .forEachIndexed { index, (startAngle, endAngle) ->
                val colorIndex = index % colors.size

                val sweepAngle = endAngle - startAngle

                val sb =
                    if (index == 0) BorderType.ANGLE_IN
                    else BorderType.ROUND_IN
                val eb =
                    if (index == angles.lastIndex) BorderType.ANGLE_OUT
                    else BorderType.ROUND_OUT

                createRingPart(
                    outRadius,
                    startAngle,
                    sweepAngle,
                    sb,
                    eb,
                    ringWidth,
                ).apply { translate(center) }
                    .also {
                        drawPath(
                            path = it,
                            color = colors[colorIndex] /*, style = Stroke(5F)*/
                        )
                    }
            }
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

    val outOval = Rect(-outerRadius, -outerRadius, outerRadius, outerRadius)
    val innerRadius = outerRadius - ringWidth
    val innerOval = Rect(-innerRadius, -innerRadius, innerRadius, innerRadius)

    return Path()
        .apply {
            arcTo(outOval, startAngle, sweepAngle, true)

            when (endBorder) {
                BorderType.ROUND_IN -> drawRoundInEnd(
                    outerRadius,
                    startAngle + sweepAngle,
                    ringWidth
                )

                BorderType.ROUND_OUT -> drawRoundOutEnd(
                    outerRadius,
                    startAngle + sweepAngle,
                    ringWidth
                )

                BorderType.ANGLE_IN -> drawAngleInEnd(startAngle + sweepAngle, ringWidth)
                BorderType.ANGLE_OUT -> drawAngleOutEnd(startAngle + sweepAngle, ringWidth)
                else -> Unit
            }

            arcTo(innerOval, startAngle + sweepAngle, -sweepAngle, false)

            when (startBorder) {
                BorderType.ROUND_IN -> drawRoundInStart(outerRadius, startAngle, ringWidth)
                BorderType.ROUND_OUT -> drawRoundOutStart(outerRadius, startAngle, ringWidth)
                BorderType.ANGLE_IN -> drawAngleInStart(startAngle, ringWidth)
                BorderType.ANGLE_OUT -> drawAngleOutStart(startAngle, ringWidth)
                else -> Unit
            }

            close()

        }
}

private fun getRectForOval(degrees: Float, outerRadius: Float, roundRadius: Float): Rect {
    val radians = radians(degrees)
    val dx = (outerRadius - roundRadius) * cos(radians)
    val dy = (outerRadius - roundRadius) * sin(radians)

    return Rect(
        dx - roundRadius,
        dy - roundRadius,
        dx + roundRadius,
        dy + roundRadius,
    )
}

private fun Path.drawRoundInEnd(radius: Float, degrees: Float, ringWidth: Float) {
    arcTo(getRectForOval(degrees, radius, ringWidth / 2F), degrees, -180F, false)
}

private fun Path.drawRoundOutEnd(radius: Float, degrees: Float, ringWidth: Float) {
    arcTo(getRectForOval(degrees, radius, ringWidth / 2F), degrees, 180F, false)
}

private fun Path.drawRoundInStart(radius: Float, degrees: Float, ringWidth: Float) {
    arcTo(getRectForOval(degrees, radius, ringWidth / 2F), degrees + 180, -180F, false)
}

private fun Path.drawRoundOutStart(radius: Float, degrees: Float, ringWidth: Float) {
    arcTo(getRectForOval(degrees, radius, ringWidth / 2F), degrees - 180, 180F, false)
}

private fun Path.drawAngleOutEnd(degrees: Float, ringWidth: Float) {

    val radians = radians(degrees)
    val roundRadius = ringWidth / 2F

    val offset1: Float = (roundRadius * cos(radians) + roundRadius * sin(radians))
    val offset2: Float = (roundRadius * cos(radians) - roundRadius * sin(radians))
    relativeLineTo(-offset1, offset2)
    relativeLineTo(-offset2, -offset1)
}

private fun Path.drawAngleInEnd(degrees: Float, ringWidth: Float) {

    val radians = radians(degrees)
    val roundRadius = ringWidth / 2F

    val offset1: Float = (roundRadius * cos(radians) - roundRadius * sin(radians))
    val offset2: Float = (roundRadius * cos(radians) + roundRadius * sin(radians))
    relativeLineTo(-offset1, -offset2)
    relativeLineTo(-offset2, offset1)
}

private fun Path.drawAngleOutStart(degrees: Float, ringWidth: Float) {

    val radians = radians(degrees)
    val roundRadius = ringWidth / 2F

    val offset1: Float = (roundRadius * cos(radians) + roundRadius * sin(radians))
    val offset2: Float = (roundRadius * cos(radians) - roundRadius * sin(radians))
    relativeLineTo(offset1, -offset2)
    relativeLineTo(offset2, offset1)
}

private fun Path.drawAngleInStart(degrees: Float, ringWidth: Float) {
    val radians = radians(degrees)
    val roundRadius = ringWidth / 2F

    val offset1: Float = (roundRadius * cos(radians) - roundRadius * sin(radians))
    val offset2: Float = (roundRadius * cos(radians) + roundRadius * sin(radians))

    relativeLineTo(offset1, offset2)
    relativeLineTo(offset2, -offset1)
}
