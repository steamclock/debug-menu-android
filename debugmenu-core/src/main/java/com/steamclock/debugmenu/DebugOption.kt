package com.steamclock.debugmenu

/**
 * debugmenu
 * Created by jake on 2021-12-03, 2:33 p.m.
 */
sealed class DebugOption(open val title: String, open val key: String)
data class BooleanValue(override val title: String, override val key: String, val defaultValue: Boolean = false): DebugOption(title, key)
data class IntValue(override val title: String, override val key: String, val defaultValue: Int = 0): DebugOption(title, key)
data class DoubleValue(override val title: String, override val key: String, val defaultValue: Double = 0.0): DebugOption(title, key)
data class LongValue(override val title: String, override val key: String, val defaultValue: Long = 0L): DebugOption(title, key)
data class Action(override val title: String, val onClick: suspend () -> Unit): DebugOption(title, title)
data class OptionSelection(override val title: String, override val key: String, val options: List<String>, val defaultIndex: Int? = null): DebugOption(title, key)
data class TextDisplay(val text: String): DebugOption(text, "")