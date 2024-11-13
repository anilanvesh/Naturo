package com.minimalapps.naturo

import android.app.Activity
import android.content.Context
import android.os.Build
import android.view.View
import android.view.WindowInsetsController
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class DarkModeManager(
    private val preferencesManager: PreferencesManager,
    private val context: Context
) {
    // Apply the saved dark mode setting
    suspend fun applyDarkMode() {
        val isDarkModeEnabled = preferencesManager.darkModeEnabled.first()
        setDarkMode(isDarkModeEnabled)
    }

    // Toggle dark mode based on the user's preference
    fun setDarkMode(enabled: Boolean) {
        // Set night mode
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )

        // Save the preference within a coroutine scope
        CoroutineScope(Dispatchers.IO).launch {
            saveDarkModePreference(enabled)
        }

        // Update system UI colors
        updateSystemUI(enabled)
    }

    // Save the user's dark mode preference
    private suspend fun saveDarkModePreference(enabled: Boolean) {
        preferencesManager.setDarkModeEnabled(enabled)
    }

    // Update system UI colors based on dark mode
    private fun updateSystemUI(isDarkMode: Boolean) {
        // Update status bar color
        val window = (context as? Activity)?.window ?: return

        // Set status bar background color
        val statusBarColor = if (isDarkMode) {
            ContextCompat.getColor(context, R.color.background_dark)
        } else {
            ContextCompat.getColor(context, R.color.background)
        }
        window.statusBarColor = statusBarColor

        // Handle status bar appearance for different Android versions
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Modern approach for Android 11+
            window.insetsController?.let { controller ->
                if (isDarkMode) {
                    controller.setSystemBarsAppearance(
                        0,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                } else {
                    controller.setSystemBarsAppearance(
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS,
                        WindowInsetsController.APPEARANCE_LIGHT_STATUS_BARS
                    )
                }
            }
        } else {
            // Compatibility approach for older versions
            @Suppress("DEPRECATION")
            window.decorView.systemUiVisibility = if (isDarkMode) {
                window.decorView.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
            } else {
                window.decorView.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            }
        }
    }

    // If you decide to keep this method, ensure it is called somewhere in your code
    fun updateTheme(activity: Activity) {
        CoroutineScope(Dispatchers.Main).launch {
            val isDarkMode = preferencesManager.darkModeEnabled.first()

            // Apply appropriate theme
            activity.setTheme(
                if (isDarkMode) R.style.Theme_Naturo_Dark
                else R.style.Theme_Naturo
            )

            // Recreate activity to apply theme
            activity.recreate()
        }
    }
}