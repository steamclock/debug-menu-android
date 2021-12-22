package com.steamclock.debugmenu.persistence

import kotlinx.coroutines.flow.*

/**
 * debugmenu
 * Created by jake on 2021-12-06, 1:35 p.m.
 */
class InMemoryPersistence: DebugMenuPersistence {
    private val data = hashMapOf<String, MutableStateFlow<*>>()

    override suspend fun <T : Any> writeValue(keyName: String, value: T, type: Class<T>) {
        val dataFlow = data[keyName] as? MutableStateFlow<T> ?: MutableStateFlow(value)
        dataFlow.update { value }
        data[keyName] = dataFlow
    }

    override suspend fun <T : Any> readValue(keyName: String, type: Class<T>): T? {
        val dataFlow = data[keyName] as? MutableStateFlow<T> ?: return null
        return dataFlow.value
    }

    override fun <T : Any> flowValue(keyName: String, type: Class<T>): Flow<T> {
        return data[keyName] as? MutableStateFlow<T> ?: return flow { }
    }
}