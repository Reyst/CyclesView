package com.github.reyst.cycles.ui.compose

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
internal fun DrawHandle(
    handleOuterRadius: Float,
    handleInnerRadius: Float,
    shift: Float,
    handleOuterColor: Color,
    handleInnerColor: Color,
    modifier: Modifier = Modifier,
) {
    val diameterDp: Dp = with(LocalDensity.current) { (2 * handleOuterRadius).toDp() }

    Canvas(
        modifier = modifier
            .width(diameterDp)
            .height(diameterDp)
    ) {
        translate(top = shift) {
            drawCircle(
                color = handleOuterColor,
                radius = handleOuterRadius,
                center = Offset(handleOuterRadius, handleOuterRadius),
            )
            drawCircle(
                color = handleInnerColor,
                radius = handleInnerRadius,
                center = Offset(handleOuterRadius, handleOuterRadius),
            )
        }
    }
}