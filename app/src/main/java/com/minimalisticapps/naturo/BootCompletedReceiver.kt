package com.minimalisticapps.naturo

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start the SoundService after device reboot
            context?.let {
                val serviceIntent = Intent(it, SoundService::class.java)
                it.startForegroundService(serviceIntent)
            }
        }
    }
}
