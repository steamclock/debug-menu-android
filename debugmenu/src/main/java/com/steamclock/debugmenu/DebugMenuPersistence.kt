package com.steamclock.debugmenu

import kotlinx.coroutines.flow.Flow

/**
 * debugmenu
 * Created by jake on 2021-12-03, 2:32 p.m.
 */
interface DebugMenuPersistence {
    suspend fun <T: Any> writeValue(keyName: String, value: T, type: Class<T>)
    suspend fun <T: Any> readValue(keyName: String, type: Class<T>): T?
    fun <T: Any> flowValue(keyName: String, type: Class<T>): Flow<T>
}

suspend inline fun <reified T: Any> DebugMenuPersistence.readValue(keyName: String, type: Class<T> = T::class.java): T? {
    return readValue(keyName, type)
}


inline fun <reified T: Any> DebugMenuPersistence.flowValue(keyName: String, type: Class<T> = T::class.java): Flow<T> {
    return flowValue(keyName, type)
}

suspend inline fun <reified T: Any> DebugMenuPersistence.writeValue(keyName: String, value: T, type: Class<T> = T::class.java) {
    writeValue(keyName, value, type)
}