package com.steamclock.debugmenu

/**
 * debugmenu
 * Created by jake on 2021-12-03, 2:33 p.m.
 */
sealed class DebugOption(open val title: String)
data class BooleanValue(override val title: String, val key: String, val defaultValue: Boolean = false): DebugOption(title)
data class IntValue(override val title: String, val key: String, val defaultValue: Int = 0): DebugOption(title)
data class DoubleValue(override val title: String, val key: String, val defaultValue: Double = 0.0): DebugOption(title)
data class LongValue(override val title: String, val key: String, val defaultValue: Long = 0L): DebugOption(title)
data class Action(override val title: String, val onClick: suspend () -> Unit): DebugOption(title)
data class OptionSelection(override val title: String, val key: String, val options: List<Any>, val defaultIndex: Int = 0): DebugOption(title)