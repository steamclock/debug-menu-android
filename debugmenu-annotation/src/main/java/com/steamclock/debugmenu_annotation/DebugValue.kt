package com.steamclock.debugmenu_annotation

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking

/**
 * DebugMenu
 * Created by jake on 2021-12-01, 3:05 p.m.
 * A wrapper for the dataStore flow, allowing us to grab the value more easily
 * Used in generated code
 */
class DebugValue<T>(val flow: Flow<T>) {
    fun blockingValue(): T = runBlocking {
        return@runBlocking flow.first()
    }

    suspend fun value(): T {
        return flow.first()
    }
}