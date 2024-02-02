package com.github.reyst.example_view

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.github.reyst.cycles.model.Cycle
import com.github.reyst.cycles.ui.view.CycleView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val cycleView = findViewById<CycleView>(R.id.cycle)
        cycleView.setOnCycleDaySelectedListener { cycle, day ->
            val phase = cycle.getPhaseByDay(day)
            Log.wtf("INSPECT", "day: $day, phase: [${phase.period.first} - ${phase.period.last}]")
        }

        findViewById<Button>(R.id.length)
            .setOnClickListener {
                cycleView.daysInCycle = (cycleView.daysInCycle + 4)
                    .takeIf { it in Cycle.AVAILABLE_DURATIONS }
                    ?: Cycle.AVAILABLE_DURATIONS.first()
            }

        findViewById<Button>(R.id.day)
            .setOnClickListener {
                cycleView.isEnabled = !cycleView.isEnabled
                cycleView.selectDay(cycleView.daysInCycle / 3)
            }

    }
}