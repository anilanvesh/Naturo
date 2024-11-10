package com.minimalapps.naturo

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.util.Log

class SoundPlayer(private val context: Context) {

    // Map to store active MediaPlayers for each sound
    private val activePlayers = mutableMapOf<Int, MediaPlayer>()

    // Map to store individual sound volumes (0.0 to 1.0)
    private val individualVolumes = mutableMapOf<Int, Float>().apply {
        // Initialize default volumes for all sounds
        putAll(
            mapOf(
                Constants.BAMBOO_SOUND to 0.5f,
                Constants.BIRDS_SOUND to 0.5f,
                Constants.CAMPFIRE_SOUND to 0.5f,
                Constants.CHIMES_SOUND to 0.5f,
                Constants.CLOCK_TICKING_SOUND to 0.5f,
                Constants.FLUTE_SOUND to 0.5f,
                Constants.FROGS_SOUND to 0.5f,
                Constants.INSECTS_SOUND to 0.5f,
                Constants.KEYBOARD_SOUND to 0.5f,
                Constants.LIGHT_RAIN_SOUND to 0.5f,
                Constants.RIVER_STREAM_SOUND to 0.5f,
                Constants.SINGING_BOWL_SOUND to 0.5f,
                Constants.THUNDER_SOUND to 0.5f,
                Constants.WATER_DROPS_SOUND to 0.5f,
                Constants.WAVES_SOUND to 0.5f
            )
        )
    }

    // Check if a specific sound is currently playing
    fun isSoundPlaying(soundId: Int): Boolean {
        return activePlayers.containsKey(soundId)
    }

    // Get list of currently active sounds
    fun getActiveSounds(): List<Int> {
        return activePlayers.keys.toList()
    }

    // Play a sound based on its ID
    fun playSound(soundId: Int) {
        // Stop the sound if it's already playing
        if (activePlayers.containsKey(soundId)) {
            stopSound(soundId)
        }

        try {
            val mediaPlayer = createMediaPlayer(soundId)
            activePlayers[soundId] = mediaPlayer
            Log.d("SoundPlayer", "Playing sound: $soundId")
        } catch (e: Exception) {
            Log.e("SoundPlayer", "Error playing sound $soundId", e)
        }
    }

    // Create MediaPlayer for a specific sound
    private fun createMediaPlayer(soundId: Int): MediaPlayer {
        return MediaPlayer.create(context, getSoundResource(soundId)).apply {
            // Configure audio attributes for better sound quality
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            isLooping = true

            // Get individual volume for the sound, default to 0.5 if not set
            val volume = individualVolumes[soundId] ?: 0.5f
            setVolume(volume, volume)

            // Add completion listener to remove from active players
            setOnCompletionListener {
                activePlayers.remove(soundId)
            }

            start()
        }
    }

    // Set volume for a specific sound clip
    fun setIndividualSoundVolume(soundId: Int, volume: Float) {
        // Ensure volume is between 0.0 and 1.0
        val normalizedVolume = volume.coerceIn(0.0f, 1.0f)

        // Store the volume for future reference
        individualVolumes[soundId] = normalizedVolume

        // Update volume if the sound is currently playing
        activePlayers[soundId]?.setVolume(normalizedVolume, normalizedVolume)

        Log.d("SoundPlayer", "Set volume for sound $soundId: $normalizedVolume")
    }

    // Get current volume for a specific sound clip
    fun getIndividualSoundVolume(soundId: Int): Float {
        return individualVolumes[soundId] ?: 0.5f
    }

    // Stop a specific sound
    fun stopSound(soundId: Int) {
        activePlayers[soundId]?.let { player ->
            player.stop()
            player.release()
            activePlayers.remove(soundId)
            Log.d("SoundPlayer", "Stopped sound: $soundId")
        }
    }

    // Pause all active sounds
    fun pauseAllSounds() {
        activePlayers.values.forEach { it.pause() }
        Log.d("SoundPlayer", "Paused all sounds: ${activePlayers.size}")
    }

    // Resume all active sounds
    fun playAllSounds() {
        activePlayers.values.forEach { it.start() }
        Log.d("SoundPlayer", "Resumed all sounds: ${activePlayers.size}")
    }

    // Private method for internal sound clearing
    private fun clearAllSounds() {
        activePlayers.values.forEach { player ->
            player.stop()
            player.release()
        }
        activePlayers.clear()
        Log.d("SoundPlayer", "Cleared all sounds")
    }

    // Release all media players
    fun release() {
        clearAllSounds()
        Log.d("SoundPlayer", "Released all resources")
    }

    // Get sound resource based on sound ID
    private fun getSoundResource(soundId: Int): Int {
        return when (soundId) {
            Constants.BAMBOO_SOUND -> R.raw.bamboo
            Constants.BIRDS_SOUND -> R.raw.birds
            Constants.CAMPFIRE_SOUND -> R.raw.campfire
            Constants.CHIMES_SOUND -> R.raw.chimes
            Constants.CLOCK_TICKING_SOUND -> R.raw.clock_ticking
            Constants.FLUTE_SOUND -> R.raw.flute
            Constants.FROGS_SOUND -> R.raw.frogs
            Constants.INSECTS_SOUND -> R.raw.insects
            Constants.KEYBOARD_SOUND -> R.raw.keyboard
            Constants.LIGHT_RAIN_SOUND -> R.raw.light_rain
            Constants.RIVER_STREAM_SOUND -> R.raw.river_stream
            Constants.SINGING_BOWL_SOUND -> R.raw.singing_bowl
            Constants.THUNDER_SOUND -> R.raw.thunder
            Constants.WATER_DROPS_SOUND -> R.raw.water_drops
            Constants.WAVES_SOUND -> R.raw.waves
            else -> throw IllegalArgumentException("Invalid sound ID")
        }
    }

    // Optional: Reset volume for a specific sound
    fun resetVolume(soundId: Int) {
        setIndividualSoundVolume(soundId, 0.5f)
    }

    // Optional: Reset volumes for all active sounds
    fun resetAllVolumes() {
        getActiveSounds().forEach { soundId ->
            setIndividualSoundVolume(soundId, 0.5f)
        }
    }
}