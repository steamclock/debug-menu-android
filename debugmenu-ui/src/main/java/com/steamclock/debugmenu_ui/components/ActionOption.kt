package com.steamclock.debugmenu_ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.steamclock.debugmenu.Action
import kotlinx.coroutines.launch

/**
 * debugmenu
 * Created by jake on 2021-12-06, 2:15 p.m.
 */
@Composable
fun ActionOption(option: Action) {
    val scope = rememberCoroutineScope()
    Button(modifier = Modifier.fillMaxWidth(),
        onClick = { scope.launch { option.onClick() } }) {
        Text(option.title)
    }
}