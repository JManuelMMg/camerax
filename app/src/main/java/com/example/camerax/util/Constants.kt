package com.example.camerax.util

// Camera Configuration
const val CAMERA_ANALYZER_THROTTLE_MS = 500L

// File & Storage
const val PICTURES_DIRECTORY = "Pictures/CameraProML"
const val VIDEOS_DIRECTORY = "Movies/CameraProML"

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

