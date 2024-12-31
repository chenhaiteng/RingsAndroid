package com.chenhaiteng.rings

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.sp
import com.chenhaiteng.rings.ui.theme.RingsAndroidTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RingsAndroidTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    RingText(
        modifier = Modifier.fillMaxSize(),
        name,
        "A་BC་A་B་C་A་B་C་A་B་C་A་B་C་A་B་C".split("་").map {
            RingTextComponent(it, Color.Red, TextStyle(fontSize = 40.sp))
        }.toTypedArray(),
        fontRatio = 0.2f,
        insetRatio = 0.2f,
        outline = 5f,
        outlineColor = Color.White,
        showBlueprint = true
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    RingsAndroidTheme {
        Greeting("Android")
    }
}