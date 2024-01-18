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
import com.steamclock.debugmenu.*

/**
 * debugmenu
 * Created by jake on 2021-12-06, 2:15 p.m.
 */
@Composable
fun Menu(state: DebugMenuState, menuKey: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = state.title,
            style = MaterialTheme.typography.h6,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        if (state.header != null) {
            Text(
                text = state.header!!,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(bottom = 24.dp)
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            items(state.options[menuKey]!!) { option ->
                if(option.isVisible) {
                    when (option) {
                        is Action -> ActionOption(option)
                        is BooleanValue -> BooleanOption(option)
                        is DoubleValue -> DoubleOption(option)
                        is IntValue -> IntOption(option)
                        is LongValue -> LongOption(option)
                        is OptionSelection -> OptionSelection(option)
                        is TextDisplay -> TextOption(option)
                    }
                }
            }
        }
        if (state.footer != null) {
            Text(
                text = state.footer!!,
                style = MaterialTheme.typography.subtitle2,
                modifier = Modifier.padding(top = 24.dp)
            )
        }
    }
}