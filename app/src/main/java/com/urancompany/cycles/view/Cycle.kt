package com.urancompany.cycles.view

class Cycle(
    @androidx.annotation.IntRange(from = 20, to = 42) val duration: Int,
) {
    val phases: List<CyclePhase> = calculatePhasesByDuration(duration)

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

    private fun calculatePhasesByDuration(duration: Int): List<CyclePhase> {

        return when (duration) {
            20 -> listOf(
                CyclePhase(1..3),
                CyclePhase(4..10),
                CyclePhase(11..13),
                CyclePhase(14..20),
            )
            21 -> listOf(
                CyclePhase(1..4),
                CyclePhase(5..11),
                CyclePhase(12..15),
                CyclePhase(16..21),
            )
            22 -> listOf(
                CyclePhase(1..4),
                CyclePhase(5..12),
                CyclePhase(13..16),
                CyclePhase(17..22),
            )
            23 -> listOf(
                CyclePhase(1..4),
                CyclePhase(5..12),
                CyclePhase(13..16),
                CyclePhase(17..23),
            )
            24 -> listOf(
                CyclePhase(1..4),
                CyclePhase(5..13),
                CyclePhase(14..17),
                CyclePhase(18..24),
            )
            25 -> listOf(
                CyclePhase(1..4),
                CyclePhase(5..13),
                CyclePhase(14..18),
                CyclePhase(19..25),
            )
            26 -> listOf(
                CyclePhase(1..5),
                CyclePhase(6..14),
                CyclePhase(15..19),
                CyclePhase(20..26),
            )
            27 -> listOf(
                CyclePhase(1..5),
                CyclePhase(6..15),
                CyclePhase(16..20),
                CyclePhase(21..27),
            )
            28 -> listOf(
                CyclePhase(1..5),
                CyclePhase(6..15),
                CyclePhase(16..20),
                CyclePhase(21..28),
            )
            29 -> listOf(
                CyclePhase(1..5),
                CyclePhase(6..16),
                CyclePhase(17..21),
                CyclePhase(22..29),
            )
            30 -> listOf(
                CyclePhase(1..5),
                CyclePhase(6..16),
                CyclePhase(17..21),
                CyclePhase(22..30),
            )
            31 -> listOf(
                CyclePhase(1..5),
                CyclePhase(6..16),
                CyclePhase(17..22),
                CyclePhase(23..31),
            )
            32 -> listOf(
                CyclePhase(1..6),
                CyclePhase(7..17),
                CyclePhase(18..23),
                CyclePhase(24..32),
            )
            33 -> listOf(
                CyclePhase(1..6),
                CyclePhase(7..18),
                CyclePhase(19..24),
                CyclePhase(25..33),
            )
            34 -> listOf(
                CyclePhase(1..6),
                CyclePhase(7..18),
                CyclePhase(19..24),
                CyclePhase(25..34),
            )
            35 -> listOf(
                CyclePhase(1..6),
                CyclePhase(7..19),
                CyclePhase(20..25),
                CyclePhase(26..35),
            )
            36 -> listOf(
                CyclePhase(1..6),
                CyclePhase(7..19),
                CyclePhase(20..26),
                CyclePhase(27..36),
            )
            37 -> listOf(
                CyclePhase(1..7),
                CyclePhase(8..20),
                CyclePhase(21..27),
                CyclePhase(28..37),
            )
            38 -> listOf(
                CyclePhase(1..7),
                CyclePhase(8..20),
                CyclePhase(21..27),
                CyclePhase(28..38),
            )
            39 -> listOf(
                CyclePhase(1..7),
                CyclePhase(8..21),
                CyclePhase(22..28),
                CyclePhase(29..39),
            )
            40 -> listOf(
                CyclePhase(1..7),
                CyclePhase(8..22),
                CyclePhase(23..29),
                CyclePhase(30..40),
            )
            41 -> listOf(
                CyclePhase(1..7),
                CyclePhase(8..22),
                CyclePhase(23..29),
                CyclePhase(30..41),
            )
            42 -> listOf(
                CyclePhase(1..7),
                CyclePhase(8..22),
                CyclePhase(23..29),
                CyclePhase(30..42),
            )
            else -> throw IllegalArgumentException("duration mast be in 20..42")
        }


    }
}

