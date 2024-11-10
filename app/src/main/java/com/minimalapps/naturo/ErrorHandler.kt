package com.minimalapps.naturo

import android.content.Context
import android.widget.Toast
import androidx.annotation.StringRes

class ErrorHandler(private val context: Context) {

    // Show an error message based on predefined error codes
    fun showError(errorCode: Int) {
        val errorMessage = getErrorMessage(errorCode)
        showToast(errorMessage)
    }

    // Method to get the appropriate error message
    private fun getErrorMessage(errorCode: Int): String {
        return context.getString(
            when (errorCode) {
                Constants.ERROR_SOUND_LOAD -> R.string.error_sound_load
                Constants.ERROR_PLAYBACK -> R.string.error_playback
                Constants.ERROR_VOLUME_CONTROL -> R.string.error_volume_control
                Constants.ERROR_TIMER -> R.string.error_timer
                Constants.ERROR_DARK_MODE -> R.string.error_dark_mode
                else -> R.string.error_unknown
            }
        )
    }

    // Helper method to display a toast message
    private fun showToast(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    // Method to show a custom error message directly by resource ID
    fun showErrorMessage(@StringRes messageResId: Int) {
        showToast(context.getString(messageResId))
    }
}
