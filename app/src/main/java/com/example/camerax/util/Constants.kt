package com.example.camerax.util

// Camera Configuration
const val CAMERA_ANALYZER_THROTTLE_MS = 500L

// File & Storage
const val PICTURES_DIRECTORY = "Pictures/CameraProML"
const val VIDEOS_DIRECTORY = "Movies/CameraProML"

// Audio Configuration - Professional Audio Codec Handling
object AudioConfig {
    const val DEFAULT_SAMPLE_RATE = 44100
    const val DEFAULT_BIT_RATE = 128000
    const val MAX_RETRY_ATTEMPTS = 2
    const val AUDIO_ENABLED_DEFAULT = true

    // Estrategia de fallback para codec de audio
    enum class AudioFallbackStrategy {
        WITH_AUDIO,        // First attempt: con audio
        WITHOUT_AUDIO,     // Second attempt: sin audio
        DISABLED          // Final fallback: audio completamente deshabilitado
    }
}

// ML Kit Configuration
object MLKitConfig {
    const val QR_CODE_SCAN_THROTTLE_MS = 300L
    const val OBJECT_DETECTION_THROTTLE_MS = 500L
    const val MAX_DETECTION_CONFIDENCE = 0.5f
}

// UI Configuration
object UIConfig {
    const val CAMERA_TRANSITION_DURATION_MS = 500
    const val DETECTION_DISPLAY_TIMEOUT_MS = 5000L
}

