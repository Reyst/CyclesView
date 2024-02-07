package com.github.reyst.cyclesview

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.reyst.cycles.ui.compose.CycleView
import com.github.reyst.cycles.ui.compose.rememberCycleViewState
import com.github.reyst.cyclesview.ui.theme.CyclesViewTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CyclesViewTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Demo()
                }
            }
        }
    }
}

@Composable
fun Demo() {

    val scope = rememberCoroutineScope()

    Box {

        val cycleState = rememberCycleViewState(31)

        CycleView(
            cycleState,
            colors = listOf(
                Color(0xFFFF5500),
                Color(0xFF00FF55),
                Color(0xFF5500FF),
                Color(0xFF000000),
            ),
            handleOuterRadius = 40.dp,
            handleInnerRadius = 10.dp,
            daySelectListener = { cycle, day ->
                Log.w("INSPECT", "day: $day, phase: ${cycle.getPhaseByDay(day)}")
            },
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight()
//                .height(450.dp)
                .padding(vertical = 50.dp, horizontal = 20.dp)
        )

        Column(
            modifier = Modifier.offset(x = 15.dp, y = 300.dp)
        ) {

            Button(
                onClick = {
                    scope.launch {
                        cycleState.setDay(1)
                    }
                }
            ) {
                Text(text = "day 1 fast")
            }

            Button(
                onClick = {
                    scope.launch {
                        cycleState.setDayTo(20, 1000)
                    }
                }
            ) {
                Text(text = "day 20 animated")
            }
        }
    }
}


@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DemoPreview() {
    CyclesViewTheme {
        Demo()
    }
}