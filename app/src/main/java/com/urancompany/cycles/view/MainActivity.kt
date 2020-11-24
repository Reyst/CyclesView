package com.urancompany.cycles.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        findViewById<CyclesView>(R.id.cycle)
            .setOnCycleDaySelectedListener { cycle, day ->
                val phase = cycle.getPhaseByDay(day)
                Log.wtf("INSPECT", "day: $day, phase: [${phase.period.first} - ${phase.period.last}]")
            }
    }
}