package com.minimalapps.naturo

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DarkModeManager(private val preferencesManager: PreferencesManager) {

    // Apply the saved dark mode setting
    fun applyDarkMode() {
        CoroutineScope(Dispatchers.Main).launch {
            val isDarkModeEnabled = preferencesManager.darkModeEnabled.first()
            setDarkMode(isDarkModeEnabled)
        }
    }

    // Toggle dark mode based on the user's preference
    fun setDarkMode(enabled: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES else AppCompatDelegate.MODE_NIGHT_NO
        )
        // Save the preference within a coroutine scope
        CoroutineScope(Dispatchers.IO).launch {
            saveDarkModePreference(enabled)
        }
    }

    // Save the user's dark mode preference
    private suspend fun saveDarkModePreference(enabled: Boolean) {
        preferencesManager.setDarkModeEnabled(enabled)
    }
}
