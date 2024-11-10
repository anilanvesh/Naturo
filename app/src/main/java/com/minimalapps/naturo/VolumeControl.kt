package com.minimalapps.naturo

class VolumeControl(private val soundPlayer: SoundPlayer) {

    // Method to set volume for a specific sound clip
    fun setSoundClipVolume(soundId: Int, volume: Float) {
        // Validate volume is between 0 and 1
        val normalizedVolume = volume.coerceIn(0.0f, 1.0f)

        // Delegate volume control to SoundPlayer
        soundPlayer.setIndividualSoundVolume(soundId, normalizedVolume)
    }

    // Method to get current volume for a specific sound clip
    fun getSoundClipVolume(soundId: Int): Float {
        // Returns the current volume for the specified sound clip
        return soundPlayer.getIndividualSoundVolume(soundId)
    }

    // Method to reset volume to default for a specific sound clip
    fun resetSoundClipVolume(soundId: Int) {
        // Set volume to a default value (50%)
        soundPlayer.setIndividualSoundVolume(soundId, 0.5f)
    }

    // Reset volumes for all active sounds
    fun resetAllSoundVolumes() {
        val activeSounds = soundPlayer.getActiveSounds()
        activeSounds.forEach { soundId ->
            resetSoundClipVolume(soundId)
        }
    }

    // Get volumes for all active sounds
    fun getActiveSoundsVolumes(): Map<Int, Float> {
        val activeSounds = soundPlayer.getActiveSounds()
        return activeSounds.associateWith {
            soundPlayer.getIndividualSoundVolume(it)
        }
    }

    // Set volume for multiple sound clips
    fun setMultipleSoundClipVolumes(volumeMap: Map<Int, Float>) {
        volumeMap.forEach { (soundId, volume) ->
            setSoundClipVolume(soundId, volume)
        }
    }

    // Reset volumes for all sound clips (active or not)
    fun resetAllSoundClipVolumes() {
        // List of all sound IDs in the app
        val allSoundIds = listOf(
            Constants.BAMBOO_SOUND,
            Constants.BIRDS_SOUND,
            Constants.CAMPFIRE_SOUND,
            Constants.CHIMES_SOUND,
            Constants.CLOCK_TICKING_SOUND,
            Constants.FLUTE_SOUND,
            Constants.FROGS_SOUND,
            Constants.INSECTS_SOUND,
            Constants.KEYBOARD_SOUND,
            Constants.LIGHT_RAIN_SOUND,
            Constants.RIVER_STREAM_SOUND,
            Constants.SINGING_BOWL_SOUND,
            Constants.THUNDER_SOUND,
            Constants.WATER_DROPS_SOUND,
            Constants.WAVES_SOUND
        )

        // Reset volume for all sound IDs
        allSoundIds.forEach { soundId ->
            resetSoundClipVolume(soundId)
        }
    }
}