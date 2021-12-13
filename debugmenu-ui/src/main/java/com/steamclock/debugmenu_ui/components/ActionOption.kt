package com.steamclock.debugmenu_ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.steamclock.debugmenu.Action

/**
 * debugmenu
 * Created by jake on 2021-12-06, 2:15 p.m.
 */
@Composable
fun ActionOption(option: Action) {
    Button(modifier = Modifier.fillMaxWidth(),
        onClick = option.onClick) {
        Text(option.title)
    }
}