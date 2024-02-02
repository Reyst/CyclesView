package com.github.reyst.cyclesview

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.reyst.cycles.model.Cycle
import com.github.reyst.cycles.ui.compose.CycleView
import com.github.reyst.cyclesview.ui.theme.CyclesViewTheme

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


    Box {
        CycleView(
            Cycle(31),
            colors = listOf(
                Color(0xFFFF5500),
                Color(0xFF00FF55),
                Color(0xFF5500FF),
                Color(0xFF000000),
            ),
            handleOuterRadius = 40.dp,
            handleInnerRadius = 10.dp,
            modifier = Modifier
                .width(350.dp)
                .fillMaxHeight()
//                .height(450.dp)
                .padding(vertical = 50.dp, horizontal = 20.dp)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun DemoPreview() {
    CyclesViewTheme {
        Demo()
    }
}