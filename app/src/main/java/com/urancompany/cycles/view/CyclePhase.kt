package com.urancompany.cycles.view

data class CyclePhase(val period: IntRange, val name: String = "") : ClosedRange<Int> by period {

    val length: Int
        get() = period.count()

    val daysBefore: Int
        get() = period.first - 1

}