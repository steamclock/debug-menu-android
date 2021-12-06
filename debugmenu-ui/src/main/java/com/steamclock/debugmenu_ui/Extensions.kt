package com.steamclock.debugmenu_ui

import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.input.pointer.pointerInteropFilter
import com.steamclock.debugmenu.DebugMenu
import kotlinx.coroutines.runBlocking

/**
 * debugmenu
 * Created by jake on 2021-12-06, 3:20 p.m.
 */
private const val longClickDuration = 3000L

// Adds a gesture handler to an Android View
fun View.showOnGesture(menuKey: String,
                       longPressDuration: Long = longClickDuration) {
    var clickTime = 0L

    setOnTouchListener { view, event ->
        return@setOnTouchListener when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                clickTime = System.currentTimeMillis()
                true
            }
            MotionEvent.ACTION_UP -> {
                if ((System.currentTimeMillis() - clickTime) > longPressDuration) {
                    runBlocking {
                        DebugMenu.instance.show(menuKey)
                    }
                    false
                } else {
                    view.performClick()
                    true
                }
            }
            else -> true
        }
    }
}

// A modifier we can add to a compose component to add the long-press gesture.
// Note that if you're using this class on a clickable element, this
// will override the normal onClick method
@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.showOnGesture(menuKey: String = DebugMenu.DEBUG_GLOBAL_MENU,
                           longPressDuration: Long = longClickDuration,
                           onClick: (() -> Unit)? = null) = composed(
    inspectorInfo = {
        name = "Debug Gesture"
    },
    factory = {
        val clickTime = remember { mutableStateOf(0L) }
        this.pointerInteropFilter {
            when (it.action) {
                MotionEvent.ACTION_DOWN -> {
                    clickTime.value = System.currentTimeMillis()
                    true
                }
                MotionEvent.ACTION_UP -> {
                    if ((System.currentTimeMillis() - clickTime.value) > longPressDuration) {
                        runBlocking {
                            DebugMenu.instance.show(menuKey)
                        }
                        false
                    } else {
                        onClick?.invoke()
                        true
                    }
                }
                else -> true
            }
        }
    }
)