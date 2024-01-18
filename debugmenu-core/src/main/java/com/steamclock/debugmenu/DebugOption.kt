package com.steamclock.debugmenu

/**
 * debugmenu
 * Created by jake on 2021-12-03, 2:33 p.m.
 */
sealed class DebugOption(open val title: String, open val key: String, open val isVisible: Boolean)
data class BooleanValue(override val title: String, override val key: String, val defaultValue: Boolean = false, override val isVisible: Boolean = true): DebugOption(title, key, isVisible)
data class IntValue(override val title: String, override val key: String, val defaultValue: Int = 0, override val isVisible: Boolean = true): DebugOption(title, key, isVisible)
data class DoubleValue(override val title: String, override val key: String, val defaultValue: Double = 0.0, override val isVisible: Boolean = true): DebugOption(title, key, isVisible)
data class LongValue(override val title: String, override val key: String, val defaultValue: Long = 0L, override val isVisible: Boolean = true): DebugOption(title, key, isVisible)
data class Action(override val title: String, override val isVisible: Boolean = true, val onClick: suspend () -> Unit): DebugOption(title, title, isVisible)
data class OptionSelection(override val title: String, override val key: String, val options: List<String>, val defaultIndex: Int? = null, override val isVisible: Boolean = true): DebugOption(title, key, isVisible)
data class TextDisplay(val text: String, override val isVisible: Boolean = true): DebugOption(text, "", isVisible)