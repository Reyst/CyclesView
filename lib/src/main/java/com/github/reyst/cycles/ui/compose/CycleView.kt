package com.github.reyst.cycles.ui.compose

import android.util.Log
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.asComposePath
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.reyst.cycles.model.Cycle
import com.github.reyst.cycles.model.CycleGraphicsCalc
import com.github.reyst.cycles.model.CycleGraphicsData
import com.github.reyst.cycles.model.CyclePhase
import com.github.reyst.cycles.model.CycleSettings
import com.github.reyst.cycles.model.OnCycleDaySelectedListener
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

fun provideRandomColors(amount: Int): List<Color> {
    val rnd = Random(System.currentTimeMillis())
    return List(amount) { Color(rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256)) }
}

@Composable
fun CycleView(
    cycle: Cycle,
    modifier: Modifier = Modifier,
    colors: List<Color> = provideRandomColors(cycle.phases.size),
    ringWidth: Dp = 40.dp,
    angleOffset: Float = -45F,
    handleOuterRadius: Dp = 60.dp,
    handleOuterColor: Color = Color(0, 0, 0, 0xCE),
    handleInnerRadius: Dp = 8.dp,
    handleInnerColor: Color = Color.White,
    daySelectListener: OnCycleDaySelectedListener? = null,
) {

    val density = LocalDensity.current.density
    var graphicsData by remember { mutableStateOf(CycleGraphicsData()) }

    var handleShift by remember { mutableFloatStateOf(0F) }
    var isHandlePressed by remember { mutableStateOf(false) }
    var handlePressedAt by remember { mutableFloatStateOf(0F) }
    val scope = rememberCoroutineScope()

    var angle by remember { mutableFloatStateOf(0F) }

    Box(
        modifier = modifier
            .onSizeChanged {
                val (width, height) = it
                Log.wtf("INSPECT", "Box: onSizeChanged($width, $height) - unchecked")

                if (graphicsData.height.toInt() != height || graphicsData.width.toInt() != width) {
                    Log.wtf("INSPECT", "Box: onSizeChanged($width, $height)")

                    graphicsData = CycleGraphicsCalc(width, height).calculate(
                        cycle = cycle,
                        ringWidth = (ringWidth.value * density),
                        handleInnerRadius = handleInnerRadius.value * density,
                        handleOuterRadius = handleOuterRadius.value * density,
                        angleOffset = angleOffset,
                    )
                }
            }
    ) {
        DrawCircle(
            graphicsData = graphicsData,
            colors = colors,
            modifier = Modifier
                .graphicsLayer {
                    transformOrigin = TransformOrigin(
                        graphicsData.centerX / graphicsData.width,
                        graphicsData.centerY / graphicsData.height,
                    )
                    rotationZ = angle + angleOffset
                },
        )

        DrawHandle(
            graphicsData = graphicsData,
            shift = handleShift,
            handleOuterColor = handleOuterColor,
            handleInnerColor = handleInnerColor,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (graphicsData.handleCenterX - graphicsData.handleOuterRadius).toInt(),
                        y = (graphicsData.handleCenterY - graphicsData.handleOuterRadius).toInt(),
                    )
                }
                .shiftable(
                    onTouch = { _, y ->
                        isHandlePressed = true
                        handlePressedAt = y

                        scope.launch {
                            while (isHandlePressed) {
                                delay(150)
                                val angleStep =
                                    graphicsData.anglePerDay * (handleShift / graphicsData.handleOuterRadius)
                                angle = (angle - angleStep).coerceIn(-graphicsData.maxAngle, 0F)
                            }
                        }
                    },
                    onRelease = {
                        isHandlePressed = false
                        handleShift = 0F
                    },
                ) { _, y ->
                    handleShift = (y - handlePressedAt)
                        .coerceIn(
                            -graphicsData.handleOuterRadius,
                            graphicsData.handleOuterRadius
                        )
                }
        )

    }
}

@Composable
private fun DrawHandle(
    graphicsData: CycleGraphicsData,
    shift: Float,
    handleOuterColor: Color,
    handleInnerColor: Color,
    modifier: Modifier = Modifier,
) {
    val diameterDp: Dp = with(LocalDensity.current) { (2 * graphicsData.handleOuterRadius).toDp() }

    Canvas(
        modifier = modifier
            .width(diameterDp)
            .height(diameterDp)
    ) {
        translate(top = shift) {
            drawCircle(
                color = handleOuterColor,
                radius = graphicsData.handleOuterRadius,
                center = Offset(
                    graphicsData.handleOuterRadius,
                    graphicsData.handleOuterRadius,
                ),
            )
            drawCircle(
                color = handleInnerColor,
                radius = graphicsData.handleInnerRadius,
                center = Offset(
                    graphicsData.handleOuterRadius,
                    graphicsData.handleOuterRadius,
                ),
            )
        }
    }
}

@Composable
private fun DrawCircle(
    graphicsData: CycleGraphicsData,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Canvas(modifier = modifier.fillMaxSize()) {
        graphicsData
            .arcs
            .forEachIndexed { index, path ->
                val colorIndex = index % colors.size
                drawPath(path = path.asComposePath(), color = colors[colorIndex])
            }
    }
}

@Preview(showBackground = true, showSystemUi = false)
@Composable
fun CycleViewPreview() {

    CycleSettings.setDurations(
        mapOf(
            31 to listOf(
                CyclePhase(1..5),
                CyclePhase(6..10),
                CyclePhase(11..16),
                CyclePhase(17..22),
                CyclePhase(23..31),
            )
        )
    )

    CycleView(
        Cycle(31),
        handleOuterRadius = 42.dp,
        handleInnerRadius = 8.dp,
        modifier = Modifier
            .fillMaxHeight()
            .width(350.dp)
            .padding(vertical = 50.dp)
    )
}