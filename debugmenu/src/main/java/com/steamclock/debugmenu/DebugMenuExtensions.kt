package com.steamclock.debugmenu

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking

/**
 * debugmenu
 * Created by jake on 2021-12-06, 1:51 p.m.
 */
suspend inline fun <reified T: Any> DebugMenu.update(key: String, value: T) {
    state.persistence.writeValue(key, value)
}

inline fun <reified T: Any> DebugMenu.valueBlocking(key: String): T? {
    return runBlocking { value(key) }
}

suspend inline fun <reified T: Any> DebugMenu.value(key: String): T? {
    return state.persistence.readValue(key)
}

inline fun <reified T: Any> DebugMenu.flow(key: String): Flow<T> {
    return state.persistence.flowValue(key)
}

// convenience function, adds options to the menu under the global namespace
suspend fun DebugMenu.addOptions(vararg newOptions: DebugOption) {
    addOptions(DebugMenu.DEBUG_GLOBAL_MENU, *newOptions)
}