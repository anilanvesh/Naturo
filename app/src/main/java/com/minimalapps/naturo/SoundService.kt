package com.minimalapps.naturo

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat

class SoundService : Service() {

    private lateinit var soundPlayer: SoundPlayer
    private lateinit var audioFocusManager: AudioFocusManager

    companion object {
        // Action constants for service intents
        const val ACTION_PLAY_SOUND = "com.minimalapps.naturo.ACTION_PLAY_SOUND"
        const val ACTION_STOP_SOUND = "com.minimalapps.naturo.ACTION_STOP_SOUND"
        const val ACTION_CLEAR_ALL = "com.minimalapps.naturo.ACTION_CLEAR_ALL"
        const val ACTION_RESUME_SOUNDS = "com.minimalapps.naturo.ACTION_RESUME_SOUNDS"
        const val EXTRA_SOUND_ID = "sound_id"
    }

    override fun onCreate() {
        super.onCreate()
        soundPlayer = SoundPlayer(this)
        audioFocusManager = AudioFocusManager(this)

        // Start foreground service with notification
        createNotificationChannel()
        startForeground(Constants.NOTIFICATION_ID, createNotification())
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.let { handleIntent(it) }
        return START_STICKY
    }

    private fun handleIntent(intent: Intent) {
        when (intent.action) {
            ACTION_PLAY_SOUND -> handlePlaySound(intent)
            ACTION_STOP_SOUND -> handleStopSound(intent)
            ACTION_CLEAR_ALL -> handleClearAllSounds()
            ACTION_RESUME_SOUNDS -> handleResumeSounds()
        }
    }

    private fun handlePlaySound(intent: Intent) {
        val soundId = intent.getIntExtra(EXTRA_SOUND_ID, -1)
        if (soundId != -1) {
            val focusGranted = audioFocusManager.requestAudioFocus { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        soundPlayer.pauseAllSounds()
                        Log.d("SoundService", "Paused sounds due to audio focus loss")
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        soundPlayer.playAllSounds()
                        Log.d("SoundService", "Resumed sounds after regaining audio focus")
                    }
                }
            }

            if (focusGranted) {
                soundPlayer.playSound(soundId)
                Log.d("SoundService", "Played sound: $soundId")
            } else {
                Log.d("SoundService", "Failed to get audio focus for sound: $soundId")
            }
        }
    }

    private fun handleStopSound(intent: Intent) {
        val soundId = intent.getIntExtra(EXTRA_SOUND_ID, -1)
        if (soundId != -1) {
            soundPlayer.stopSound(soundId)
            Log.d("SoundService", "Stopped sound: $soundId")

            // Check if no sounds are playing
            if (soundPlayer.getActiveSounds().isEmpty()) {
                audioFocusManager.abandonAudioFocus()
                Log.d("SoundService", "Abandoned audio focus")
            }
        }
    }

    private fun handleClearAllSounds() {
        val activeSounds = soundPlayer.getActiveSounds()
        activeSounds.forEach { soundId ->
            soundPlayer.stopSound(soundId)
        }
        audioFocusManager.abandonAudioFocus()
        Log.d("SoundService", "Cleared all sounds")
    }

    private fun handleResumeSounds() {
        val activeSounds = soundPlayer.getActiveSounds()

        if (activeSounds.isEmpty()) {
            Log.d("SoundService", "No active sounds to resume")
            return
        }

        val focusGranted = audioFocusManager.requestAudioFocus { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_LOSS, AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    soundPlayer.pauseAllSounds()
                    Log.d("SoundService", "Paused sounds during resume")
                }
                AudioManager.AUDIOFOCUS_GAIN -> {
                    soundPlayer.playAllSounds()
                    Log.d("SoundService", "Resumed sounds after regaining focus")
                }
            }
        }

        if (focusGranted) {
            activeSounds.forEach { soundId ->
                soundPlayer.playSound(soundId)
            }
            Log.d("SoundService", "Resumed ${activeSounds.size} sounds")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
        audioFocusManager.abandonAudioFocus()
        Log.d("SoundService", "Service destroyed, resources released")
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            Constants.NOTIFICATION_CHANNEL_ID,
            getString(R.string.notification_channel_name),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = getString(R.string.notification_channel_description)
        }
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(channel)
    }

    private fun createNotification(): Notification {
        return NotificationCompat.Builder(this, Constants.NOTIFICATION_CHANNEL_ID)
            .setContentTitle(getString(R.string.notification_title))
            .setContentText(getString(R.string.notification_text))
            .setSmallIcon(R.drawable.ic_notification)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOngoing(true)
            .build()
    }
}