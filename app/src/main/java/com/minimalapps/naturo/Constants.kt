package com.minimalapps.naturo

class Constants {
    companion object {
        // Notification constants
        const val NOTIFICATION_CHANNEL_ID = "naturo_sound_service_channel"
        const val NOTIFICATION_ID = 1

        // Sound IDs
        const val BAMBOO_SOUND = 1
        const val BIRDS_SOUND = 2
        const val CAMPFIRE_SOUND = 3
        const val CHIMES_SOUND = 4
        const val CLOCK_TICKING_SOUND = 5
        const val FLUTE_SOUND = 6
        const val FROGS_SOUND = 7
        const val INSECTS_SOUND = 8
        const val KEYBOARD_SOUND = 9
        const val LIGHT_RAIN_SOUND = 10
        const val RIVER_STREAM_SOUND = 11
        const val SINGING_BOWL_SOUND = 12
        const val THUNDER_SOUND = 13
        const val WATER_DROPS_SOUND = 14
        const val WAVES_SOUND = 15

        // Error Codes for ErrorHandler
        const val ERROR_SOUND_LOAD = 1001
        const val ERROR_PLAYBACK = 1002
        const val ERROR_VOLUME_CONTROL = 1003
        const val ERROR_TIMER = 1004
        const val ERROR_DARK_MODE = 1005

        // Timer limits
        const val TIMER_MIN_LIMIT = 1  // Minimum timer duration (in minutes)
        const val TIMER_MAX_LIMIT = 120 // Maximum timer duration (in minutes)
    }
}
