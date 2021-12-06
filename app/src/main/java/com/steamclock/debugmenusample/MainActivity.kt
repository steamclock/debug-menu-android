package com.steamclock.debugmenusample

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.steamclock.debugmenu.*
import com.steamclock.debugmenusample.ui.theme.DebugmenuTheme
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            DebugMenu.instance.addOptions(Toggle("Show secret text", key = "show secret text"))
            DebugMenu.instance.addOptions(Action("Buttons Menu") {
                DebugMenu.instance.show("menu2")
            })
            DebugMenu.instance.addOptions("menu2",
                Toggle("Alt Button Text", key = "alt button text"),
                Toggle("Alt Button Colour", key = "alt button color")
            )
            DebugMenu.instance.show()
        }

        setContent {
            DebugmenuTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val showSecretText = DebugMenu.instance.flow<Boolean>("show secret text").collectAsState(initial = false)
                    val useAltButtonText = DebugMenu.instance.flow<Boolean>("alt button text").collectAsState(initial = false)
                    val useAltButtonColour = DebugMenu.instance.flow<Boolean>("alt button color").collectAsState(initial = false)
                    DebugMenuSample(
                        showSecretText = showSecretText.value,
                        altButtonText = useAltButtonText.value,
                        altButtonColour = useAltButtonColour.value)
                }
            }
        }
    }
}

@Composable
fun DebugMenuSample(showSecretText: Boolean, altButtonText: Boolean, altButtonColour: Boolean) {
    val coroutineScope = rememberCoroutineScope()
    Column(verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)) {
        if (showSecretText) {
            Text("Secret Text!")
        }
        val colors = ButtonDefaults.buttonColors(
            backgroundColor = if (altButtonColour) Color.Red else Color.Green
        )
        Button(
            colors = colors,
            onClick = {
            coroutineScope.launch {
                DebugMenu.instance.show()
            }
        }) {
            val text = if (altButtonText) {
                "Reveal Debug Menu"
            } else {
                "Show Debug Menu"
            }
            Text(text)
        }
    }
}