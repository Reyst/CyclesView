package com.github.reyst.cycles.utils

import kotlin.math.PI

internal const val RadiansToDegrees = (180.0 / PI).toFloat()
internal fun radians(degrees: Float) = degrees / RadiansToDegrees
