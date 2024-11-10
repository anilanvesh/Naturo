package com.minimalapps.naturo

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "naturo_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        val DARK_MODE_ENABLED = booleanPreferencesKey("dark_mode_enabled")
    }

    // Save Dark Mode preference
    suspend fun setDarkModeEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[DARK_MODE_ENABLED] = enabled
        }
    }

    // Get Dark Mode preference
    val darkModeEnabled: Flow<Boolean> = context.dataStore.data
        .map { prefs ->
            prefs[DARK_MODE_ENABLED] ?: false
        }
}
