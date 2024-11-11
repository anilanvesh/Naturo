package com.minimalapps.naturo

import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.switchmaterial.SwitchMaterial
import kotlinx.coroutines.launch
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var soundAdapter: SoundAdapter
    private lateinit var switchDarkMode: SwitchMaterial
    private lateinit var buttonPlayPause: ImageButton
    private lateinit var buttonClear: ImageButton
    private lateinit var buttonTimer: ImageButton
    private lateinit var soundPlayer: SoundPlayer
    private lateinit var volumeControl: VolumeControl
    private lateinit var timerPopup: TimerPopup
    private lateinit var errorHandler: ErrorHandler
    private lateinit var darkModeManager: DarkModeManager
    private lateinit var audioFocusManager: AudioFocusManager
    private lateinit var recyclerViewSounds: RecyclerView
    private lateinit var textViewTimer: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
        setupListeners()

        // Apply saved dark mode setting in a coroutine
        lifecycleScope.launch {
            darkModeManager.applyDarkMode()
        }

        // Start sound service for background sound support
        startService(Intent(this, SoundService::class.java))
    }

    private fun initUI() {
        // Initialize the RecyclerView with responsive grid layout
        recyclerViewSounds = findViewById(R.id.recyclerViewSounds)
        setupRecyclerViewLayout()

        soundPlayer = SoundPlayer(this)
        volumeControl = VolumeControl(soundPlayer)

        // Calculate screen width in dp
        val displayMetrics = resources.displayMetrics
        val screenWidthDp = (displayMetrics.widthPixels / displayMetrics.density).toInt()

        // Pass screenWidthDp to SoundAdapter with a callback for granular updates
        soundAdapter = SoundAdapter(
            soundPlayer,
            volumeControl,
            screenWidthDp
        ) { soundId, isPlaying ->
            // Update play/pause button state based on active sounds
            updatePlayPauseButtonState()
        }
        recyclerViewSounds.adapter = soundAdapter

        // Initialize buttons and timer TextView
        buttonPlayPause = findViewById(R.id.buttonPlayPause)
        buttonClear = findViewById(R.id.buttonClear)
        buttonTimer = findViewById(R.id.buttonTimer)
        switchDarkMode = findViewById(R.id.switchDarkMode)
        textViewTimer = findViewById(R.id.textViewTimer)

        // Initialize error handler, dark mode manager, and audio focus manager
        errorHandler = ErrorHandler(this)
        darkModeManager = DarkModeManager(PreferencesManager(this))
        audioFocusManager = AudioFocusManager(this)
        timerPopup = TimerPopup(this)
    }

    // Update play/pause button state based on active sounds
    private fun updatePlayPauseButtonState() {
        runOnUiThread {
            if (soundAdapter.hasActiveSounds()) {
                buttonPlayPause.setImageResource(R.drawable.ic_pause)
            } else {
                buttonPlayPause.setImageResource(R.drawable.ic_play)
            }
        }
    }

    // Responsive grid layout configuration
    private fun setupRecyclerViewLayout() {
        val spanCount = calculateSpanCount()
        val layoutManager = GridLayoutManager(this, spanCount)
        recyclerViewSounds.layoutManager = layoutManager
    }

    // Dynamic span count based on screen size and orientation
    private fun calculateSpanCount(): Int {
        val configuration = resources.configuration
        val screenWidthDp = configuration.screenWidthDp
        val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

        return when {
            // Landscape orientation
            isLandscape -> when {
                screenWidthDp < 600 -> 4
                screenWidthDp < 900 -> 5
                else -> 6
            }
            // Portrait orientation
            else -> when {
                screenWidthDp < 600 -> 3
                screenWidthDp < 900 -> 4
                else -> 5
            }
        }
    }

    // Configuration change listener for responsive layout
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        setupRecyclerViewLayout()
    }

    private fun setupListeners() {
        // Play/Pause button listener - Only for active sounds
        buttonPlayPause.setOnClickListener {
            try {
                if (soundAdapter.hasActiveSounds()) {
                    // Pause active sounds
                    val focusGranted = audioFocusManager.requestAudioFocus { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> {
                                soundAdapter.pauseAllSounds()
                                updatePlayPauseButtonState()
                                Log.d("MainActivity", "Lost audio focus, paused sounds")
                            }
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                soundAdapter.playAllSounds()
                                updatePlayPauseButtonState()
                                Log.d("MainActivity", "Regained audio focus, resumed sounds")
                            }
                        }
                    }

                    if (focusGranted) {
                        soundAdapter.pauseAllSounds()
                        updatePlayPauseButtonState()
                        audioFocusManager.abandonAudioFocus()
                        Log.d("MainActivity", "Paused active sounds")
                    }
                } else {
                    // Play sounds
                    val focusGranted = audioFocusManager.requestAudioFocus { focusChange ->
                        when (focusChange) {
                            AudioManager.AUDIOFOCUS_LOSS -> {
                                soundAdapter.pauseAllSounds()
                                updatePlayPauseButtonState()
                                Log.d("MainActivity", "Lost audio focus, paused sounds")
                            }
                            AudioManager.AUDIOFOCUS_GAIN -> {
                                soundAdapter.playAllSounds()
                                updatePlayPauseButtonState()
                                Log.d("MainActivity", "Regained audio focus, resumed sounds")
                            }
                        }
                    }

                    if (focusGranted) {
                        soundAdapter.playAllSounds()
                        updatePlayPauseButtonState()
                        Log.d("MainActivity", "Played active sounds")
                    }
                }
            } catch (e: Exception) {
                errorHandler.showError(Constants.ERROR_PLAYBACK)
                Log.e("MainActivity", "Error in play/pause", e)
            }
        }

        // Clear button listener
        buttonClear.setOnClickListener {
            try {
                soundAdapter.clearAllSounds()
                soundAdapter.resetAllSoundVolumes()
                updatePlayPauseButtonState()
                audioFocusManager.abandonAudioFocus()
                textViewTimer.visibility = TextView.GONE
                Log.d("MainActivity", "Cleared all sounds")
            } catch (e: Exception) {
                errorHandler.showError(Constants.ERROR_PLAYBACK)
                Log.e("MainActivity", "Error clearing sounds", e)
            }
        }

        // Timer button listener
        buttonTimer.setOnClickListener {
            try {
                timerPopup.show()
                timerPopup.setOnTimerSetListener { minutes ->
                    textViewTimer.text = String.format(Locale.getDefault(), "%02d:%02d", minutes / 60, minutes % 60)
                    textViewTimer.visibility = TextView.VISIBLE
                    soundAdapter.startTimerForSounds(minutes * 60 * 1000L)
                    Log.d("MainActivity", "Timer set for $minutes minutes")
                }
            } catch (e: Exception) {
                errorHandler.showError(Constants.ERROR_TIMER)
                Log.e("MainActivity", "Error setting timer", e)
            }
        }

        // Dark mode toggle listener
        switchDarkMode.setOnCheckedChangeListener { _, isChecked ->
            try {
                darkModeManager.setDarkMode(isChecked)
                Log.d("MainActivity", "Dark mode set to: $isChecked")
            } catch (e: Exception) {
                errorHandler.showError(Constants.ERROR_DARK_MODE)
                Log.e("MainActivity", "Error changing dark mode", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        soundPlayer.release()
        audioFocusManager.abandonAudioFocus()
        Log.d("MainActivity", "Activity destroyed, resources released")
    }
}