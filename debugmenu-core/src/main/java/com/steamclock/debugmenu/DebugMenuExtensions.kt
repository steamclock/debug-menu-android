package com.steamclock.debugmenu

import com.steamclock.debugmenu.persistence.flowValue
import com.steamclock.debugmenu.persistence.readValue
import com.steamclock.debugmenu.persistence.writeValue
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.runBlocking
import java.nio.charset.StandardCharsets
import java.security.MessageDigest

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

inline fun <reified T: Any?> DebugMenu.flow(key: String): Flow<T> {
    return state.persistence.flowValue(key)
}

// convenience function, adds options to the menu under the global namespace
suspend fun DebugMenu.addOptions(vararg newOptions: DebugOption) {
    addOptions(DebugMenu.DEBUG_GLOBAL_MENU, *newOptions)
}

fun String.sha256(): String = MessageDigest.getInstance("SHA-256")
    .digest(this.toByteArray(StandardCharsets.UTF_8))
    .fold("", { str, it -> str + "%02x".format(it) })