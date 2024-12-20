package com.minimalisticapps.naturo

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

// Extension property for DataStore
private val Context.dataStore by preferencesDataStore(name = "naturo_prefs")

class PreferencesManager(private val context: Context) {
    // Note: Removed dark mode-related preferences and methods
    // Future preferences can be added here as needed
}