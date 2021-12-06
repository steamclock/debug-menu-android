package com.steamclock.debugmenu_ui.components

import android.view.KeyEvent
import android.view.KeyEvent.KEYCODE_ENTER
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

/**
 * debugmenu
 * Created by jake on 2021-12-06, 2:15 p.m.
 */
@Composable
fun CodeEntry(onSubmit: suspend (String) -> Unit) {
    val textState = remember { mutableStateOf(TextFieldValue()) }
    val coroutineScope = rememberCoroutineScope()

    fun submit() {
        coroutineScope.launch {
            onSubmit(textState.value.text.trim())
        }
    }

    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        Text(text = "Enter code")

        TextField(
            textState.value,
            singleLine = true,
            onValueChange = { textState.value = it },
            keyboardActions = KeyboardActions { submit() },
            keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp).onKeyEvent {
                return@onKeyEvent if (it.nativeKeyEvent.keyCode == KEYCODE_ENTER) {
                    submit()
                    true
                } else {
                    false
                }
            }
        )

        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = { submit() }
        ) {
            Text("Submit")
        }
    }
}