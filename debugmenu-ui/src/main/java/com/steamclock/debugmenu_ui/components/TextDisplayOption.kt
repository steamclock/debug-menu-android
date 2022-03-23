package com.steamclock.debugmenu_ui.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import com.steamclock.debugmenu.TextDisplay

/**
 * debugmenu
 * Created by jake on 2022-03-23, 1:58 p.m.
 */

@Composable
fun TextOption(option: TextDisplay) {
    Text(option.text)
}