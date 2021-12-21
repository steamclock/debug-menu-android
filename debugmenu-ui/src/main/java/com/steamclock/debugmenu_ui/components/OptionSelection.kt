package com.steamclock.debugmenu_ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem
import androidx.compose.material.OutlinedButton
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.steamclock.debugmenu.DebugMenu
import com.steamclock.debugmenu.OptionSelection
import com.steamclock.debugmenu.flow
import com.steamclock.debugmenu.update
import kotlinx.coroutines.launch

/**
 * debugmenu
 * Created by jake on 2021-12-13, 1:16 p.m.
 */
@Composable
fun OptionSelection(selection: OptionSelection) {
    val composableScope = rememberCoroutineScope()
    val expanded = remember { mutableStateOf(false) }
    val selectedIndex = DebugMenu.instance.flow<Int>(selection.key).collectAsState(initial = selection.defaultIndex)
    val selectedIndexValue = selectedIndex.value
    val defaultOption = selection.options[selection.defaultIndex ?: 0]

    val value = if (selectedIndexValue != null)
        selection.options.getOrNull(selectedIndexValue) ?: defaultOption
    else
        defaultOption

    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                expanded.value = !expanded.value
            }
        ) {
            Row(modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Text(selection.title)
                Text(value)
            }
        }
        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false },
        ) {
            selection.options.forEachIndexed { index, option ->
                DropdownMenuItem(onClick = {
                    expanded.value = false
                    composableScope.launch {
                        DebugMenu.instance.update(selection.key, index)
                    }
                }) {
                    Text(option)
                }
            }
        }
    }
}