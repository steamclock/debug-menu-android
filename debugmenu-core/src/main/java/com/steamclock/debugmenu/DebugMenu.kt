package com.steamclock.debugmenu

import com.steamclock.debugmenu.display.DebugMenuDisplay
import com.steamclock.debugmenu.persistence.DebugMenuPersistence
import com.steamclock.debugmenu.persistence.readValue
import com.steamclock.debugmenu.persistence.writeValue

class DebugMenu private constructor(private val code: String) {
    lateinit var state: DebugMenuState
        private set

    suspend fun addOptions(menuKey: String, vararg newOptions: DebugOption) {
        val previousOptions = state.options.toMutableMap()
        val previousOptionsMenu = previousOptions[menuKey]?.toMutableList() ?: mutableListOf()
        // TODO: 2021-12-08 check for duplicates based on menuKey and option title
        initializeNewOptions(newOptions.toList())
        previousOptionsMenu.addAll(newOptions)
        previousOptions[menuKey] = previousOptionsMenu
        state = state.copy(options = previousOptions.toMap())
    }

    private suspend fun initializeNewOptions(newOptions: List<DebugOption>) {
        newOptions.forEach {
            when (it) {
                is Action -> { /* no op */ }
                is BooleanValue -> {
                    state.persistence.apply {
                        if (readValue<Boolean>(it.key) == null) {
                            writeValue(it.key, it.defaultValue)
                        }
                    }
                }
                is DoubleValue -> {
                    state.persistence.apply {
                        if (readValue<Double>(it.key) == null) {
                            writeValue(it.key, it.defaultValue)
                        }
                    }
                }
                is IntValue -> {
                    state.persistence.apply {
                        if (readValue<Int>(it.key) == null) {
                            writeValue(it.key, it.defaultValue)
                        }
                    }
                }
                is LongValue -> {
                    state.persistence.apply {
                        if (readValue<Long>(it.key) == null) {
                            writeValue(it.key, it.defaultValue)
                        }
                    }
                }
                is OptionSelection -> {
                    state.persistence.apply {
                        val value = it.defaultIndex
                        if (value != null && readValue<Int>(it.key) == null) {
                            writeValue(it.key, value)
                        }
                    }
                }

                is TextDisplay -> { /* no op */ }
            }
        }
    }

    suspend fun enterCode(code: String) {
        state.persistence.writeValue(DEBUG_MENU_CODE_KEY, code.sha256())
    }

    suspend fun show(menu: String = DEBUG_GLOBAL_MENU) {
        if (!hasSetPassword()) {
            state.display.displayCodeEntry()
            return
        }
        state.display.displayMenu(state, menu)
    }

    fun optionForKey(key: String): DebugOption? {
        val allOptions = state.options.flatMap { it.value }
        return allOptions.firstOrNull { it.key == key }
    }

    private suspend fun hasSetPassword(): Boolean {
        val enteredCode = value<String>(DEBUG_MENU_CODE_KEY)
        return enteredCode == code
    }

    companion object {
        private const val DEBUG_MENU_CODE_KEY = "DEBUG_MENU_CODE_KEY"
        const val DEBUG_GLOBAL_MENU = "DEBUG_GLOBAL_MENU"

        private var _instance: DebugMenu? = null
        val instance: DebugMenu
            get() = if (_instance != null) {
                _instance!!
            } else {
                throw RuntimeException("Call initialize before usage")
            }

        fun initialize(encryptedCode: String, display: DebugMenuDisplay, persistence: DebugMenuPersistence, header: String? = null, footer: String? = null) {
            val previousState = _instance?.state ?: DebugMenuState(title = "Debug Menu")
            val newState = previousState.copy(
                display = display,
                persistence = persistence,
                header = header,
                footer = footer
            )
            _instance = DebugMenu(encryptedCode)
            _instance?.state = newState
        }
    }
}