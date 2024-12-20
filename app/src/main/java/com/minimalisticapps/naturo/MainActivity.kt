package com.minimalisticapps.naturo

import android.content.Intent
import android.content.res.Configuration
import android.media.AudioManager
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import java.util.Locale
import com.minimalisticapps.naturo.utils.GridUtils

class MainActivity : AppCompatActivity() {

    private lateinit var soundAdapter: SoundAdapter
    private lateinit var buttonPlayPause: ImageButton
    private lateinit var buttonClear: ImageButton
    private lateinit var buttonTimer: ImageButton
    private lateinit var soundPlayer: SoundPlayer
    private lateinit var volumeControl: VolumeControl
    private lateinit var timerPopup: TimerPopup
    private lateinit var errorHandler: ErrorHandler
    private lateinit var audioFocusManager: AudioFocusManager
    private lateinit var recyclerViewSounds: RecyclerView
    private lateinit var textViewTimer: TextView
    private var countDownTimer: CountDownTimer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initUI()
        setupListeners()

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
        textViewTimer = findViewById(R.id.textViewTimer)

        // Initialize error handler, dark mode manager, and audio focus manager
        errorHandler = ErrorHandler(this)
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

    // Responsive grid layout configuration using GridUtils
    private fun setupRecyclerViewLayout() {
        val spanCount = GridUtils.calculateOptimalSpanCount(this)
        val layoutManager = GridLayoutManager(this, spanCount)
        recyclerViewSounds.layoutManager = layoutManager
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
                // Cancel any existing timer
                countDownTimer?.cancel()

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
                    // Cancel any existing timer
                    countDownTimer?.cancel()

                    // Convert minutes to milliseconds
                    val timerDurationMillis = minutes * 60 * 1000L

                    // Create and start new CountDownTimer
                    countDownTimer = object : CountDownTimer(timerDurationMillis, 1000) {
                        override fun onTick(millisUntilFinished: Long) {
                            val remainingMinutes = millisUntilFinished / (60 * 1000)
                            val remainingSeconds = (millisUntilFinished % (60 * 1000)) / 1000
                            textViewTimer.text = String.format(
                                Locale.getDefault(),
                                "%02d:%02d",
                                remainingMinutes,
                                remainingSeconds
                            )
                            textViewTimer.visibility = TextView.VISIBLE
                        }

                        override fun onFinish() {
                            textViewTimer.visibility = TextView.GONE
                            soundAdapter.clearAllSounds()
                            soundAdapter.resetAllSoundVolumes()
                            updatePlayPauseButtonState()
                        }
                    }.start()

                    // Start timer for sounds
                    soundAdapter.startTimerForSounds(timerDurationMillis)
                    Log.d("MainActivity", "Timer set for $minutes minutes")
                }

                // Optional: Add a cancel listener
                timerPopup.setOnTimerCancelListener {
                    Log.d("MainActivity", "Timer setup cancelled")
                }
            } catch (e: Exception) {
                errorHandler.showError(Constants.ERROR_TIMER)
                Log.e("MainActivity", "Error setting timer", e)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the countdown timer to prevent memory leaks
        countDownTimer?.cancel()
        soundPlayer.release()
        audioFocusManager.abandonAudioFocus()
        Log.d("MainActivity", "Activity destroyed, resources released")
    }
}