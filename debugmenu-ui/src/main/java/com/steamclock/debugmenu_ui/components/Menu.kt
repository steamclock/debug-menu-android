package com.steamclock.debugmenu_ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.steamclock.debugmenu.Action
import com.steamclock.debugmenu.DebugOption
import com.steamclock.debugmenu.Toggle

/**
 * debugmenu
 * Created by jake on 2021-12-06, 2:15 p.m.
 */
@Composable
fun Menu(title: String, options: List<DebugOption>) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = title,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 24.dp)
        )
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(options) { option ->
                when (option) {
                    is Action -> ActionOption(option)
                    is Toggle -> ToggleOption(option)
                }
            }
        }
    }
}