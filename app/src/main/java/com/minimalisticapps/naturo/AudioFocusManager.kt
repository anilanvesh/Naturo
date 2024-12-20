package com.minimalisticapps.naturo

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Build

class AudioFocusManager(private val context: Context) {

    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    // Audio focus request for the entire app's sound playback
    private var audioFocusRequest: AudioFocusRequest? = null

    // Callback for audio focus changes
    private var audioFocusChangeCallback: ((Int) -> Unit)? = null

    // Request audio focus for sound playback
    fun requestAudioFocus(onAudioFocusChange: (Int) -> Unit): Boolean {
        // Store the callback
        audioFocusChangeCallback = onAudioFocusChange

        // Create audio attributes for nature sounds
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        // Build audio focus request
        audioFocusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
            .setAudioAttributes(audioAttributes)
            .setOnAudioFocusChangeListener { focusChange ->
                // Handle audio focus changes
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> {
                        // Completely lost audio focus, pause all sounds
                        audioFocusChangeCallback?.invoke(focusChange)
                    }
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                        // Temporarily lost audio focus, pause sounds
                        audioFocusChangeCallback?.invoke(focusChange)
                    }
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        // Regained audio focus, resume sounds
                        audioFocusChangeCallback?.invoke(focusChange)
                    }
                }
            }
            .build()

        // Request audio focus
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioManager.requestAudioFocus(audioFocusRequest!!) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(
                { focusChange ->
                    audioFocusChangeCallback?.invoke(focusChange)
                },
                AudioManager.STREAM_MUSIC,
                AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
            ) == AudioManager.AUDIOFOCUS_REQUEST_GRANTED
        }
    }

    // Abandon audio focus when all sounds are stopped
    fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            audioFocusRequest?.let {
                audioManager.abandonAudioFocusRequest(it)
            }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus { }
        }

        // Clear the audio focus request and callback
        audioFocusRequest = null
        audioFocusChangeCallback = null
    }

    // Check if audio focus is currently granted
    fun isAudioFocusGranted(): Boolean {
        return audioFocusRequest != null
    }
}