package com.steamclock.debugmenusample

import android.os.Bundle
import android.util.Log
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
import com.steamclock.debugmenu.generated.ButtonMenu
import com.steamclock.debugmenu.generated.GlobalDebugMenu
import com.steamclock.debugmenu.generated.TestingMenu
import com.steamclock.debugmenu.generated.initDebugMenus
import com.steamclock.debugmenu_annotation.*
import com.steamclock.debugmenu_ui.showDebugMenuOnGesture
import com.steamclock.debugmenusample.ui.theme.DebugmenuTheme

private object Debug {
    const val TestingMenu = "TestingMenu"
    const val ButtonMenu = "ButtonMenu"
}

@DebugBoolean(title = "Enable easy debug menu")
object EasyDebugMenuToggle

@DebugAction(title = "Global action", menuKey = Debug.TestingMenu)
fun doGlobalAction() {
    Log.d("TAG", "doGlobalAction: ")
}

class MainActivity : AppCompatActivity() {
    @DebugBoolean(title = "Show secret text", menuKey = Debug.TestingMenu)
    object ShowSecretTextToggle

    @DebugBoolean(title = "Alt Button Text", menuKey = Debug.ButtonMenu)
    object AltButtonTextToggle

    @DebugBoolean(title = "Alt Button Colour", menuKey = Debug.ButtonMenu)
    object AltButtonColourToggle

    @DebugAction(title = "Buttons Menu", menuKey = Debug.TestingMenu)
    fun showButtonsMenu() {
        ButtonMenu.show()
    }

    @DebugSelection("Selection Test", options = ["Testing", "1", "2", "3"])
    object SelectionKey

    @DebugInt(title = "Int")
    object IntKey

    @DebugDouble(title = "Double")
    object DoubleKey

    @DebugLong(title = "Long")
    object LongKey

    @DebugAction(title = "Show Testing Menu")
    fun showTestingMenu() {
        TestingMenu.show()
    }


    @DebugAction(title = "Show Testing Menu2")
    fun showTestingMenu2() {
        TestingMenu.show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initDebugMenus()

        setContent {
            DebugmenuTheme {
                Surface(color = MaterialTheme.colors.background) {
                    val easyDebugMenuToggle = GlobalDebugMenu.EasyDebugMenuToggle.flow.collectAsState(initial = false)
                    val showSecretText = TestingMenu.ShowSecretTextToggle.flow.collectAsState(initial = false)
                    val useAltButtonText = ButtonMenu.AltButtonTextToggle.flow.collectAsState(initial = false)
                    val useAltButtonColour = ButtonMenu.AltButtonColourToggle.flow.collectAsState(initial = false)
                    DebugMenuSample(
                        easyDebugMenuToggle = easyDebugMenuToggle.value,
                        showSecretText = showSecretText.value,
                        altButtonText = useAltButtonText.value,
                        altButtonColour = useAltButtonColour.value)
                }
            }
        }
    }
}

@Composable
fun DebugMenuSample(easyDebugMenuToggle: Boolean, showSecretText: Boolean, altButtonText: Boolean, altButtonColour: Boolean) {
    val context = LocalContext.current
    Column(verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.padding(16.dp)) {
        val colors = ButtonDefaults.buttonColors(
            backgroundColor = if (altButtonColour) Color.Red else Color.Green
        )
        val seconds = if (easyDebugMenuToggle) 1 else 3
        fun buttonClicked() {
            Toast.makeText(context, "Long Click For $seconds Seconds", Toast.LENGTH_SHORT).show()
        }
        Button(
            colors = colors,
            onClick = { buttonClicked() },
            modifier = Modifier.showDebugMenuOnGesture(longPressDuration = 1000L * seconds, onClick = { buttonClicked() }),
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
                it.text = "$seconds second long press for menu2!"
                it.showDebugMenuOnGesture(ButtonMenu.key, longPressDuration = 1000L * seconds)
            })
        }
    }
}