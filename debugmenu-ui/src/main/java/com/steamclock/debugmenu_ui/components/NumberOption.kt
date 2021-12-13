package com.steamclock.debugmenu_ui.components

import android.view.KeyEvent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import com.steamclock.debugmenu.*
import kotlinx.coroutines.launch

/**
 * debugmenu
 * Created by jake on 2021-12-13, 11:25 a.m.
 */
@Composable
private fun <T> NumberOption(title: String, value: T, onUpdate: suspend (String) -> Unit) {
    val textState = remember { mutableStateOf(TextFieldValue()) }
    val focusManager = LocalFocusManager.current
    val composableScope = rememberCoroutineScope()

    fun submit() {
        composableScope.launch {
            onUpdate(textState.value.text.trim())
        }
    }
    Column(modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.SpaceBetween) {
        TextField(value.toString(),
            singleLine = true,
            label = {
                Text(title)
            },
            placeholder = {
                Text(value.toString())
            },
            keyboardActions = KeyboardActions {
                submit()
                focusManager.clearFocus()
            },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Number),
            onValueChange = {
                textState.value = textState.value.copy(it)
                submit()
            },
            modifier = Modifier.onKeyEvent {
                return@onKeyEvent if (it.nativeKeyEvent.keyCode == KeyEvent.KEYCODE_ENTER) {
                    submit()
                    true
                } else {
                    false
                }
            }
        )
    }
}

@Composable
fun IntOption(option: IntValue) {
    val intState = DebugMenu.instance.flow<Int>(option.key).collectAsState(initial = option.defaultValue)
    NumberOption(title = option.title, value = intState.value, onUpdate = {
        DebugMenu.instance.update(option.key, it.toIntOrNull() ?: option.defaultValue)
    })
}

@Composable
fun DoubleOption(option: DoubleValue) {
    val doubleState = DebugMenu.instance.flow<Double>(option.key).collectAsState(initial = option.defaultValue)
    val lastValue = remember { mutableStateOf(0.0) }

    LaunchedEffect(key1 = option, block = {
        lastValue.value = option.defaultValue
    })

    NumberOption(title = option.title, value = doubleState.value, onUpdate = {
        // because a string like 11. doesn't actually convert to a double properly, we need
        // to keep the last value so the user can continue punching in the value
        lastValue.value = it.toDoubleOrNull() ?: lastValue.value
        DebugMenu.instance.update(option.key, lastValue.value)
    })
}

@Composable
fun LongOption(option: LongValue) {
    val longState = DebugMenu.instance.flow<Long>(option.key).collectAsState(initial = option.defaultValue)
    NumberOption(title = option.title, value = longState.value, onUpdate = {
        DebugMenu.instance.update(option.key, it.toLongOrNull() ?: option.defaultValue)
    })
}