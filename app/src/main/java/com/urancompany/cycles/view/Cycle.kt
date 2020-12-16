package com.urancompany.cycles.view

import android.content.Context

class Cycle(val duration: Int) {

    val phases: List<CyclePhase> = CycleSettings.getPhases(duration)

    val phase1: CyclePhase
        get() = phases[0]
    val phase2: CyclePhase
        get() = phases[1]
    val phase3: CyclePhase
        get() = phases[2]
    val phase4: CyclePhase
        get() = phases[3]

    fun getPhaseByDay(day: Int): CyclePhase = day.takeIf { day in 1..duration }
        ?.let { selectedDay -> phases.first { selectedDay in it } }
        ?: throw IllegalArgumentException(("day must be in range [1..$duration]"))

    fun getPhaseName(context: Context, phase: CyclePhase): String =
        if (phase.name.isNotBlank()) phase.name
        else context.getString(getPhaseNameId(phase))

    fun getPhaseNameId(phase: CyclePhase): Int = getPhaseNameId(phases.indexOf(phase))

    private fun getPhaseNameId(index: Int): Int = when (index) {
        0 -> R.string.phase1_name
        1 -> R.string.phase2_name
        2 -> R.string.phase3_name
        else -> R.string.phase4_name
    }


    companion object {
        val AVAILABLE_DURATIONS: List<Int>
            get() = CycleSettings.durations.toList()
    }
}

