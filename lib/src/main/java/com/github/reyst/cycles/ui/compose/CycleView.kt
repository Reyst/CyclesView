package com.github.reyst.cycles.ui.compose

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.github.reyst.cycles.model.CycleGraphicsCalc
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
    state: CycleViewState,
    modifier: Modifier = Modifier,
    colors: List<Color> = provideRandomColors(state.cycle.phases.size),
    ringWidth: Dp = 40.dp,
    angleOffset: Float = -45F,
    handleOuterRadius: Dp = 60.dp,
    handleOuterColor: Color = Color(0, 0, 0, 0xCE),
    handleInnerRadius: Dp = 8.dp,
    handleInnerColor: Color = Color.White,
    daySelectListener: OnCycleDaySelectedListener? = null,
) {

    val scope = rememberCoroutineScope()

    val density = LocalDensity.current.density

    var circleSize by remember { mutableStateOf(Size(0F, 0F)) }
    var circleCenter by remember { mutableStateOf(Offset(0F, 0F)) }
    var handleCenter by remember { mutableStateOf(Offset(0F, 0F)) }
    var outRadius by remember { mutableFloatStateOf(0F) }

    val handleOutR = remember(
        key1 = handleOuterRadius,
        key2 = density,
    ) { handleOuterRadius.value * density }
    val handleInR = remember(
        key1 = handleInnerRadius,
        key2 = density,
    ) { handleInnerRadius.value * density }
    val ringWidthPx = remember(
        key1 = ringWidth.value,
        key2 = density,
    ) { ringWidth.value * density }


    if (daySelectListener != null) {
        LaunchedEffect(key1 = state.day) {
            daySelectListener.onCycleDaySelected(state.cycle, state.day)
        }
    }

    Box(
        modifier = modifier
            .onSizeChanged {
                val (width, height) = it
                if (circleSize.height.toInt() != height || circleSize.width.toInt() != width) {
                    CycleGraphicsCalc(width, height)
                        .calculate(
                            ringWidth = ringWidthPx,
                            handleOuterRadius = handleOutR,
                            angleOffset = angleOffset,
                        )
                        .also { data ->
                            circleSize = data.size
                            circleCenter = data.center
                            handleCenter = data.handleCenter
                            outRadius = data.outRadius
                        }
                }
            }
    ) {
        DrawCircle(
            center = circleCenter,
            angles = state.angles,
            colors = colors,
            outRadius = outRadius,
            ringWidth = ringWidthPx,
            modifier = Modifier
                .graphicsLayer {
                    transformOrigin = TransformOrigin(
                        circleCenter.x / circleSize.width,
                        circleCenter.y / circleSize.height,
                    )
                    rotationZ = state.angle + angleOffset
                },
        )

        DrawHandle(
            handleOutR,
            handleInR,
            shift = state.handleShift,
            handleOuterColor = handleOuterColor,
            handleInnerColor = handleInnerColor,
            modifier = Modifier
                .offset {
                    IntOffset(
                        x = (handleCenter.x - handleOutR).toInt(),
                        y = (handleCenter.y - handleOutR).toInt(),
                    )
                }
                .shiftable(
                    onTouch = { _, y ->
                        state.isHandlePressed = true
                        state.handlePressedAt = y

                        scope.launch {
                            while (state.isHandlePressed) {
                                delay(150)
                                state.updateAngle(state.handleShift / handleOutR)
                            }
                        }
                    },
                    onRelease = { state.isHandlePressed = false },
                ) { _, y ->
                    state.handleShift = (y - state.handlePressedAt)
                        .coerceIn(-handleOutR, handleOutR)
                }
        )
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
        CycleViewState(31),
        handleOuterRadius = 42.dp,
        handleInnerRadius = 8.dp,
        modifier = Modifier
            .fillMaxHeight()
            .width(350.dp)
            .padding(vertical = 50.dp)
    )
}