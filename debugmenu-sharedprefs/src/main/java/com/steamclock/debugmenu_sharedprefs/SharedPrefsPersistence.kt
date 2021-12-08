package com.steamclock.debugmenu_sharedprefs

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import com.steamclock.debugmenu.persistence.DebugMenuPersistence
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * debugmenu
 * Created by jake on 2021-12-03, 1:08 p.m.
 */
class SharedPrefsPersistence(private var context: Context, preferenceTitle: String = "debug_menu"): DebugMenuPersistence {
    init {
        // always use app context
        context = context.applicationContext
    }

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = preferenceTitle)
    private val preferenceKeys: MutableMap<String, Preferences.Key<Any>> = mutableMapOf()
    private val preferenceFlows: MutableMap<String, Flow<Any?>> = mutableMapOf()

    private fun <T: Any> createPreferenceKey(keyName: String, type: Class<T>): Preferences.Key<T> {

        val key = when (type) {
            String::class.java -> stringPreferencesKey(keyName)
            Int::class.javaObjectType -> intPreferencesKey(keyName)
            Double::class.javaObjectType -> doublePreferencesKey(keyName)
            Long::class.javaObjectType -> longPreferencesKey(keyName)
            Boolean::class.javaObjectType -> booleanPreferencesKey(keyName)
            else -> throw RuntimeException("Unable to persist type ${type.name}. Supported types are String, Int, Long, Double, Boolean")
        }
        preferenceKeys[keyName] = key as Preferences.Key<Any>
        return key as Preferences.Key<T>
    }

    private fun <T: Any> createPreferenceFlow(keyName: String, type: Class<T>): Flow<T?> {
        val key: Preferences.Key<T> = preferenceKeys[keyName] as? Preferences.Key<T>
            ?: createPreferenceKey(keyName, type = type)
        val flow: Flow<T?> = context.dataStore.data.map { preference ->
            preference[key]
        }
        preferenceFlows[keyName] = flow
        return flow
    }

    override suspend fun <T: Any> writeValue(keyName: String, value: T, type: Class<T>) {
        val key: Preferences.Key<T> = preferenceKeys[keyName] as? Preferences.Key<T>
            ?: createPreferenceKey(keyName, type = type)

        context.dataStore.edit { settings ->
            settings[key] = value
        }
    }

    override suspend fun <T: Any> readValue(keyName: String, type: Class<T>): T? {
        val flow = preferenceFlows[keyName] ?: createPreferenceFlow(keyName, type = type)
        return flow.first() as? T
    }

    override fun <T: Any> flowValue(keyName: String, type: Class<T>): Flow<T> {
        return (preferenceFlows[keyName] ?: createPreferenceFlow(keyName, type = type)) as Flow<T>
    }
}