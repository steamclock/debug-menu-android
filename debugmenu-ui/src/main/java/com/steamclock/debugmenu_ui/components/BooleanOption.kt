package com.steamclock.debugmenu_ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import com.steamclock.debugmenu.DebugMenu
import com.steamclock.debugmenu.BooleanValue
import com.steamclock.debugmenu.flow
import com.steamclock.debugmenu.update
import kotlinx.coroutines.launch

/**
 * debugmenu
 * Created by jake on 2021-12-06, 2:15 p.m.
 */
@Composable
fun BooleanOption(option: BooleanValue) {
    val switchState = DebugMenu.instance.flow<Boolean>(option.key).collectAsState(initial = option.defaultValue)
    val composableScope = rememberCoroutineScope()
    Row(modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween) {
        Text(option.title)
        Switch(switchState.value, onCheckedChange = {
            composableScope.launch {
                DebugMenu.instance.update(option.key, it)
            }
        }, colors = SwitchDefaults.colors(
            checkedThumbColor = MaterialTheme.colors.primary,
            checkedTrackColor = MaterialTheme.colors.primaryVariant
        ))
    }
}
