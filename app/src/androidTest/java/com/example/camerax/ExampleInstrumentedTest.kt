package com.example.camerax

import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.ext.junit.runners.AndroidJUnit4

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*

/**
 * Instrumented test, which will execute on an Android device.
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.camerax", appContext.packageName)
    }

    @Test
    fun testAppNameFromResources() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        val appName = appContext.getString(R.string.app_name)
        assertEquals("Camera Pro ML", appName)
    }

    @Test
    fun testStringsResourcesLoaded() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        // Test that all critical strings are available
        appContext.getString(R.string.capture)
        appContext.getString(R.string.flip_camera)
        appContext.getString(R.string.qr_scan)
        appContext.getString(R.string.object_detection)
        // If we get here without exception, strings are loaded correctly
        assertTrue(true)
    }
}