package com.github.reyst.cycles.model

class Cycle(val duration: Int) {

    val phases: List<CyclePhase> = CycleSettings.getPhases(duration)

    fun getPhaseByDay(day: Int): CyclePhase = day.takeIf { day in 1..duration }
        ?.let { selectedDay -> phases.first { selectedDay in it } }
        ?: throw IllegalArgumentException(("day must be in range [1..$duration]"))

    companion object {
        val AVAILABLE_DURATIONS: List<Int>
            get() = CycleSettings.durations.toList()
    }
}

