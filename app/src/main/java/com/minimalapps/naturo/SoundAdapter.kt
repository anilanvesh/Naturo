package com.minimalapps.naturo

import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SoundAdapter(
    private val soundPlayer: SoundPlayer,
    private val volumeControl: VolumeControl,
    private val screenWidthDp: Int,
    private val onSoundStateChanged: ((Int, Boolean) -> Unit)? = null
) : RecyclerView.Adapter<SoundAdapter.SoundViewHolder>() {

    // List of all available sound items
    val sounds = listOf(
        SoundItem(R.drawable.ic_bamboo, R.string.bamboo, Constants.BAMBOO_SOUND),
        SoundItem(R.drawable.ic_birds, R.string.birds, Constants.BIRDS_SOUND),
        SoundItem(R.drawable.ic_campfire, R.string.campfire, Constants.CAMPFIRE_SOUND),
        SoundItem(R.drawable.ic_chimes, R.string.chimes, Constants.CHIMES_SOUND),
        SoundItem(R.drawable.ic_clock_ticking, R.string.clock_ticking, Constants.CLOCK_TICKING_SOUND),
        SoundItem(R.drawable.ic_flute, R.string.flute, Constants.FLUTE_SOUND),
        SoundItem(R.drawable.ic_frog, R.string.frogs, Constants.FROGS_SOUND),
        SoundItem(R.drawable.ic_insects, R.string.insects, Constants.INSECTS_SOUND),
        SoundItem(R.drawable.ic_keyboard, R.string.keyboard, Constants.KEYBOARD_SOUND),
        SoundItem(R.drawable.ic_light_rain, R.string.light_rain, Constants.LIGHT_RAIN_SOUND),
        SoundItem(R.drawable.ic_river_stream, R.string.river_stream, Constants.RIVER_STREAM_SOUND),
        SoundItem(R.drawable.ic_singing_bowl, R.string.singing_bowl, Constants.SINGING_BOWL_SOUND),
        SoundItem(R.drawable.ic_thunder, R.string.thunder, Constants.THUNDER_SOUND),
        SoundItem(R.drawable.ic_water_drops, R.string.water_drops, Constants.WATER_DROPS_SOUND),
        SoundItem(R.drawable.ic_waves, R.string.waves, Constants.WAVES_SOUND)
    )

    // Track active sounds with their play state (playing or paused)
    private val activeSounds = mutableMapOf<Int, Boolean>()

    // Handler for managing delayed actions
    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    // Calculate responsive icon and text sizes based on screen width
    private val iconSize = calculateIconSize()
    private val textSize = calculateTextSize()

    // Create a new ViewHolder when needed
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sound_item, parent, false)
        return SoundViewHolder(view)
    }

    // Bind data to the ViewHolder
    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val soundItem = sounds[position]
        holder.bind(soundItem)
    }

    // Total number of sound items
    override fun getItemCount(): Int = sounds.size

    // Inner class to manage individual sound item view
    inner class SoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val soundIcon: ImageView = view.findViewById(R.id.imageViewSoundIcon)
        private val soundName: TextView = view.findViewById(R.id.textViewSoundName)
        private val volumeSeekBar: SeekBar = view.findViewById(R.id.seekBarVolume)
        private var soundItem: SoundItem? = null

        // Initialize view interactions
        init {
            // Hide volume seek bar initially
            volumeSeekBar.visibility = View.GONE

            // Add click listener to sound icon
            soundIcon.setOnClickListener {
                soundItem?.let { item ->
                    toggleSound(item)
                }
            }
        }

        // Bind sound item details to the view
        fun bind(soundItem: SoundItem) {
            this.soundItem = soundItem

            // Set responsive icon size
            val layoutParams = soundIcon.layoutParams
            layoutParams.width = iconSize
            layoutParams.height = iconSize
            soundIcon.layoutParams = layoutParams
            soundIcon.setImageResource(soundItem.iconResId)

            // Set text size and content
            soundName.textSize = textSize
            soundName.setText(soundItem.nameResId)

            // Update sound state (playing/paused)
            updateSoundState(soundItem)

            // Setup volume control
            val currentVolume = volumeControl.getSoundClipVolume(soundItem.soundId)
            volumeSeekBar.progress = (currentVolume * 100).toInt()
            volumeSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        val volume = progress / 100f
                        volumeControl.setSoundClipVolume(soundItem.soundId, volume)
                    }
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {}
                override fun onStopTrackingTouch(seekBar: SeekBar?) {}
            })
        }

        // Update UI based on sound state
        private fun updateSoundState(soundItem: SoundItem) {
            val isCurrentlyPlaying = activeSounds[soundItem.soundId] == true
            volumeSeekBar.visibility = if (isCurrentlyPlaying) View.VISIBLE else View.GONE
        }

        // Toggle sound between play and pause
        private fun toggleSound(soundItem: SoundItem) {
            val currentState = activeSounds[soundItem.soundId]
            if (currentState == true) {
                // Pause sound
                soundPlayer.pauseSound(soundItem.soundId)
                activeSounds[soundItem.soundId] = false
                volumeSeekBar.visibility = View.GONE
                onSoundStateChanged?.invoke(soundItem.soundId, false)
            } else {
                // Play sound
                soundPlayer.playSound(soundItem.soundId)
                activeSounds[soundItem.soundId] = true
                volumeSeekBar.visibility = View.VISIBLE
                onSoundStateChanged?.invoke(soundItem.soundId, true)
            }
            notifyItemChanged(sounds.indexOf(soundItem))
        }
    }

    // Get list of currently active sound IDs
    fun getActiveSoundIds(): List<Int> = activeSounds.keys.toList()

    // Check if any sounds are currently playing
    fun hasActiveSounds(): Boolean = activeSounds.values.contains(true)

    // Resume all paused sounds
    fun playAllSounds() {
        activeSounds.forEach { (soundId, isPlaying) ->
            if (!isPlaying) {
                val index = sounds.indexOfFirst { it.soundId == soundId }
                if (index != -1) {
                    soundPlayer.playSound(soundId)
                    activeSounds[soundId] = true
                    notifyItemChanged(index)
                    onSoundStateChanged?.invoke(soundId, true)
                }
            }
        }
    }

    // Pause all playing sounds
    fun pauseAllSounds() {
        val currentActiveSounds = activeSounds.keys.toList()
        currentActiveSounds.forEach { soundId ->
            val index = sounds.indexOfFirst { it.soundId == soundId }
            if (index != -1) {
                soundPlayer.pauseSound(soundId)
                activeSounds[soundId] = false
                notifyItemChanged(index)
                onSoundStateChanged?.invoke(soundId, false)
            }
        }
    }

    // Reset volume for all sounds to default
    fun resetAllSoundVolumes() {
        sounds.forEach { soundItem ->
            volumeControl.setSoundClipVolume(soundItem.soundId, 0.5f)
        }
    }

    // Stop and clear all sounds
    fun clearAllSounds() {
        val currentActiveSounds = activeSounds.keys.toList()
        currentActiveSounds.forEach { soundId ->
            val index = sounds.indexOfFirst { it.soundId == soundId }
            if (index != -1) {
                soundPlayer.stopSound(soundId)
                activeSounds.remove(soundId)
                notifyItemChanged(index)
                onSoundStateChanged?.invoke(soundId, false)
            }
        }
    }

    // Calculate responsive icon size based on screen width
    private fun calculateIconSize(): Int {
        return when {
            screenWidthDp < 600 -> 144  // Small screens
            screenWidthDp < 900 -> 192  // Medium screens
            else -> 240  // Large screens
        }
    }

    // Calculate responsive text size based on screen width
    private fun calculateTextSize(): Float {
        return when {
            screenWidthDp < 600 -> 10f  // Small screens
            screenWidthDp < 900 -> 12f  // Medium screens
            else -> 14f  // Large screens
        }
    }

    // Start a timer to stop sounds after a specified duration
    fun startTimerForSounds(duration: Long) {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = Runnable {
            clearAllSounds()
        }
        handler.postDelayed(stopRunnable!!, duration)
    }
}

// Data class to represent a sound item
data class SoundItem(val iconResId: Int, val nameResId: Int, val soundId: Int)