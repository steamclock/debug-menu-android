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
import com.steamclock.debugmenu.Action
import com.steamclock.debugmenu.DebugMenu
import com.steamclock.debugmenu.addOptions
import com.steamclock.debugmenu.generated.*
import com.steamclock.debugmenu_annotation.DebugToggle
import com.steamclock.debugmenu_ui.showOnGesture
import com.steamclock.debugmenusample.ui.theme.DebugmenuTheme
import kotlinx.coroutines.runBlocking

@DebugToggle(title = "Enable testing")
class GlobalTempToggle

class MainActivity : AppCompatActivity() {
    @DebugToggle(title = "Show secret text", menuKey = "TestingMenu")
    class ShowSecretTextToggle

    @DebugToggle(title = "Alt Button Text", menuKey = "ButtonMenu")
    class AltButtonTextToggle

    @DebugToggle(title = "Alt Button Colour", menuKey = "ButtonMenu")
    class AltButtonColourToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        runBlocking {
            DebugMenu.instance.addOptions(menuKey = TestingMenu.key, Action("Buttons Menu") {
                ButtonMenu.show()
            })

            DebugMenu.instance.addOptions(Action("Show Testing Menu") {
                TestingMenu.show()
            })
        }

        setContent {
            DebugmenuTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val showSecretText = TestingMenu.ShowSecretTextToggle.flow.collectAsState(initial = false)
                    val useAltButtonText = ButtonMenu.AltButtonTextToggle.flow.collectAsState(initial = false)
                    val useAltButtonColour = ButtonMenu.AltButtonColourToggle.flow.collectAsState(initial = false)
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
            modifier = Modifier.showOnGesture(onClick = { buttonClicked() }),
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
                it.showOnGesture(ButtonMenu.key)
            })
        }
    }
}