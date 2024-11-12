package com.minimalapps.naturo

import android.app.AlertDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

class TimerPopup(private val context: Context) {

    private var onTimerSetListener: ((Int) -> Unit)? = null
    private var onTimerCancelListener: (() -> Unit)? = null

    fun show() {
        val dialogView: View = LayoutInflater.from(context).inflate(R.layout.timer_popup, null)
        val editTextMinutes = dialogView.findViewById<EditText>(R.id.editTextMinutes)
        val textViewTimerRange = dialogView.findViewById<TextView>(R.id.textViewTimerRange)
        val buttonSetTimer = dialogView.findViewById<Button>(R.id.buttonSetTimer)
        val buttonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)

        // Remove duplicate hint by setting only one hint
        editTextMinutes.hint = context.getString(R.string.enter_minutes)

        // Set the timer range text with formatted values
        textViewTimerRange.text = context.getString(R.string.timer_range, Constants.TIMER_MIN_LIMIT, Constants.TIMER_MAX_LIMIT)

        val alertDialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        buttonSetTimer.setOnClickListener {
            val minutesInput = editTextMinutes.text.toString()
            if (minutesInput.isNotEmpty()) {
                val minutes = minutesInput.toIntOrNull()
                if (minutes != null && minutes in Constants.TIMER_MIN_LIMIT..Constants.TIMER_MAX_LIMIT) {
                    onTimerSetListener?.invoke(minutes)
                    alertDialog.dismiss()
                } else {
                    // Show error if the entered minutes are out of range
                    Toast.makeText(
                        context,
                        context.getString(R.string.timer_out_of_range, Constants.TIMER_MIN_LIMIT, Constants.TIMER_MAX_LIMIT),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            } else {
                // Show error if the input field is empty
                Toast.makeText(context, context.getString(R.string.enter_minutes), Toast.LENGTH_SHORT).show()
            }
        }

        buttonCancel.setOnClickListener {
            onTimerCancelListener?.invoke()
            alertDialog.dismiss()
        }

        alertDialog.show()
    }

    fun setOnTimerSetListener(listener: (Int) -> Unit) {
        this.onTimerSetListener = listener
    }

    fun setOnTimerCancelListener(listener: () -> Unit) {
        this.onTimerCancelListener = listener
    }
}