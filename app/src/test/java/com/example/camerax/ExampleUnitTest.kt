package com.example.camerax

import org.junit.Test

import org.junit.Assert.*

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun testCameraStateDefaults() {
        val state = com.example.camerax.ui.camera.CameraState()
        assertFalse(state.isRecording)
        assertTrue(state.isPhotoMode)
        assertFalse(state.isQrMode)
        assertFalse(state.isObjectDetectionEnabled)
        assertNull(state.lastCapturedUri)
        assertNull(state.detectedQrText)
        assertTrue(state.detectedObjects.isEmpty())
    }

    @Test
    fun testCameraViewModelInitialization() {
        val viewModel = com.example.camerax.ui.camera.CameraViewModel()
        val state = viewModel.state.value
        assertFalse(state.isRecording)
        assertTrue(state.isPhotoMode)
    }
}

