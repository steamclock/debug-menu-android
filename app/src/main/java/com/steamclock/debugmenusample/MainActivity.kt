package com.steamclock.debugmenusample

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.steamclock.debugmenu.*
import com.steamclock.debugmenu_ui.showDebugMenuOnGesture
import com.steamclock.debugmenusample.ui.theme.DebugmenuTheme
import kotlinx.coroutines.runBlocking

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            DebugMenu.instance.addOptions(
                Toggle("Show secret text", key = "show secret text"),
                Action("Buttons Menu") {
                    DebugMenu.instance.show("menu2")
                }
            )

            DebugMenu.instance.addOptions("menu2",
                Toggle("Alt Button Text", key = "alt button text"),
                Toggle("Alt Button Colour", key = "alt button color")
            )
        }

        setContent {
            DebugmenuTheme {
                Surface(color = MaterialTheme.colors.background) {
                    // access via flow, which can be collected as state for Jetpack Compose
                    val showSecretText = DebugMenu.instance.flow<Boolean>("show secret text").collectAsState(initial = false)

                    // access directly, blocking to allow synchronous access
                    val showSecretTextValue = DebugMenu.instance.valueBlocking<Boolean>("show secret text")

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
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)) {
        val colors = ButtonDefaults.buttonColors(
            backgroundColor = if (altButtonColour) Color.Red else Color.Green
        )
        fun buttonClicked() {
            Toast.makeText(context, "Long Click For 3 Seconds", Toast.LENGTH_SHORT).show()
        }
        Button(
            colors = colors,
            onClick = { buttonClicked() },
            modifier = Modifier.showDebugMenuOnGesture(onClick = { buttonClicked() }),
        ) {
            val text = if (altButtonText) {
                "Reveal Debug Menu"
            } else {
                "Show Debug Menu"
            }
            Text(text)
        }
        if (showSecretText) {
            AndroidView(factory = {
                TextView(context)
            }, update = {
                it.text = "3 second long press for menu2!"
                it.showDebugMenuOnGesture("menu2")
            })
        }
    }
}