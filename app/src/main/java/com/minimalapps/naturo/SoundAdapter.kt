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

    // Track active sounds
    private val activeSounds = mutableSetOf<Int>()

    private val handler = Handler(Looper.getMainLooper())
    private var stopRunnable: Runnable? = null

    // Responsive icon and text sizing
    private val iconSize = calculateIconSize()
    private val textSize = calculateTextSize()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SoundViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.sound_item, parent, false)
        return SoundViewHolder(view)
    }

    override fun onBindViewHolder(holder: SoundViewHolder, position: Int) {
        val soundItem = sounds[position]
        holder.bind(soundItem)
    }

    override fun getItemCount(): Int = sounds.size

    inner class SoundViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        private val soundIcon: ImageView = view.findViewById(R.id.imageViewSoundIcon)
        private val soundName: TextView = view.findViewById(R.id.textViewSoundName)
        private val volumeSeekBar: SeekBar = view.findViewById(R.id.seekBarVolume)
        private var soundItem: SoundItem? = null

        init {
            // Set initial volume seek bar to hidden
            volumeSeekBar.visibility = View.GONE

            // Direct sound icon click to toggle sound
            soundIcon.setOnClickListener {
                soundItem?.let { item ->
                    toggleSound(item)
                }
            }
        }

        fun bind(soundItem: SoundItem) {
            this.soundItem = soundItem

            // Set responsive icon and text
            val layoutParams = soundIcon.layoutParams
            layoutParams.width = iconSize
            layoutParams.height = iconSize
            soundIcon.layoutParams = layoutParams
            soundIcon.setImageResource(soundItem.iconResId)

            soundName.textSize = textSize
            soundName.setText(soundItem.nameResId)

            // Update UI based on current sound state
            updateSoundState(soundItem)

            // Volume control setup
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

        private fun updateSoundState(soundItem: SoundItem) {
            val isCurrentlyPlaying = activeSounds.contains(soundItem.soundId)
            volumeSeekBar.visibility = if (isCurrentlyPlaying) View.VISIBLE else View.GONE
        }

        private fun toggleSound(soundItem: SoundItem) {
            if (activeSounds.contains(soundItem.soundId)) {
                // Stop sound
                soundPlayer.stopSound(soundItem.soundId)
                activeSounds.remove(soundItem.soundId)
                volumeSeekBar.visibility = View.GONE
                onSoundStateChanged?.invoke(soundItem.soundId, false)
            } else {
                // Play sound
                soundPlayer.playSound(soundItem.soundId)
                activeSounds.add(soundItem.soundId)
                volumeSeekBar.visibility = View.VISIBLE
                onSoundStateChanged?.invoke(soundItem.soundId, true)
            }
            notifyItemChanged(sounds.indexOf(soundItem))
        }
    }

    // Get list of currently active sound IDs
    fun getActiveSoundIds(): List<Int> = activeSounds.toList()

    // Check if any sounds are currently active
    fun hasActiveSounds(): Boolean = activeSounds.isNotEmpty()

    fun playAllSounds() {
        sounds.forEachIndexed { index, soundItem ->
            if (!activeSounds.contains(soundItem.soundId)) {
                soundPlayer.playSound(soundItem.soundId)
                activeSounds.add(soundItem.soundId)
                notifyItemChanged(index)
                onSoundStateChanged?.invoke(soundItem.soundId, true)
            }
        }
    }

    fun pauseAllSounds() {
        val currentActiveSounds = activeSounds.toList()
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

    fun resetAllSoundVolumes() {
        sounds.forEach { soundItem ->
            volumeControl.setSoundClipVolume(soundItem.soundId, 0.5f)
        }
    }

    fun clearAllSounds() {
        val currentActiveSounds = activeSounds.toList()
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

    // Responsive sizing methods
    private fun calculateIconSize(): Int {
        return when {
            screenWidthDp < 600 -> 144  // 3x original size
            screenWidthDp < 900 -> 192
            else -> 240
        }
    }

    private fun calculateTextSize(): Float {
        return when {
            screenWidthDp < 600 -> 10f
            screenWidthDp < 900 -> 12f
            else -> 14f
        }
    }

    fun startTimerForSounds(duration: Long) {
        stopRunnable?.let { handler.removeCallbacks(it) }
        stopRunnable = Runnable {
            clearAllSounds()
        }
        handler.postDelayed(stopRunnable!!, duration)
    }
}

data class SoundItem(val iconResId: Int, val nameResId: Int, val soundId: Int)