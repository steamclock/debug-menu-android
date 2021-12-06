package com.steamclock.debugmenu

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.lang.RuntimeException
import java.util.*

data class DebugMenuState(val title: String,
                          val options: Map<String, List<DebugOption>> = mapOf(),
                          val persistence: DebugMenuPersistence? = null,
                          val display: DebugMenuDisplay? = null)

class DebugMenu private constructor(private val code: String = UUID.randomUUID().toString()) {
    lateinit var state: DebugMenuState
        private set

    suspend fun addOptions(vararg newOptions: DebugOption) {
        addOptions(DEBUG_GLOBAL_MENU, *newOptions)
    }

    suspend fun addOptions(menu: String, vararg newOptions: DebugOption) {
        val previousOptions = state.options.toMutableMap()
        val previousOptionsMenu = previousOptions[menu]?.toMutableList() ?: mutableListOf()
        initializeNewOptions(newOptions.toList())
        previousOptionsMenu.addAll(newOptions)
        previousOptions[menu] = previousOptionsMenu
        state = state.copy(
            options = previousOptions.toMap()
        )
    }

    private suspend fun initializeNewOptions(newOptions: List<DebugOption>) {
        val persistence = state.persistence ?: throw RuntimeException("Persistence not initialized!")
        newOptions.forEach {
            when (it) {
                is Action -> {}
                is Toggle -> {
                    if (persistence.readValue<Boolean>(it.key) == null) {
                        state.persistence?.writeValue(it.key, it.defaultValue)
                    }
                }
            }
        }
    }

    fun enterCode(code: String) {
        val persistence = state.persistence ?: throw RuntimeException("Persistence not initialized!")
        runBlocking {
            persistence.writeValue(DEBUG_MENU_CODE_KEY, code)
        }
    }

    suspend fun show(menu: String = DEBUG_GLOBAL_MENU) {
        if (!hasSetPassword()) {
            state.display?.displayCodeEntry()
            return
        }
        state.display?.displayMenu(state.title, state.options[menu]!!)
    }

    suspend inline fun <reified T: Any> update(key: String, value: T) {
        val persistence = state.persistence ?: throw RuntimeException("Persistence not initialized!")
        persistence.writeValue(key, value)
    }

    inline fun <reified T: Any> valueBlocking(key: String): T? {
        return runBlocking {
            value(key)
        }
    }

    suspend inline fun <reified T: Any> value(key: String): T? {
        val persistence = state.persistence ?: throw RuntimeException("Persistence not initialized!")
        return persistence.readValue(key)
    }

    inline fun <reified T: Any> flow(key: String): Flow<T> {
        val persistence = state.persistence ?: throw RuntimeException("Persistence not initialized!")
        return persistence.flowValue(key)
    }

    private suspend fun hasSetPassword(): Boolean {
        val enteredCode = value<String>(DEBUG_MENU_CODE_KEY)
        return enteredCode == code
    }

    companion object {
        private var _instance: DebugMenu? = null
        private const val DEBUG_MENU_CODE_KEY = "DEBUG_MENU_CODE_KEY"
        private const val DEBUG_GLOBAL_MENU = "DEBUG_GLOBAL_MENU"

        fun initialize(code: String, display: DebugMenuDisplay, persistence: DebugMenuPersistence) {
            val previousState = _instance?.state ?: DebugMenuState(title = "Debug Menu")
            val newState = previousState.copy(
                display = display,
                persistence = persistence
            )
            _instance = DebugMenu(code)
            _instance?.state = newState
        }

        val instance: DebugMenu
            get() = if (_instance != null) {
                _instance!!
            } else {
                throw RuntimeException("Call initialize before usage")
            }

    }
}