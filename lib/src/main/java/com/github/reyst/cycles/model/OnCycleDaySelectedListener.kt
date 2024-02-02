package com.github.reyst.cycles.model

fun interface OnCycleDaySelectedListener {
    fun onCycleDaySelected(cycle: Cycle, day: Int)
}