package com.steamclock.debugmenu

/**
 * debugmenu
 * Created by jake on 2021-12-03, 2:33 p.m.
 */
sealed class DebugOption(open val title: String)
data class Toggle(override val title: String, val key: String, val defaultValue: Boolean = false): DebugOption(title)
data class Action(override val title: String, val onClick: suspend () -> Unit): DebugOption(title)