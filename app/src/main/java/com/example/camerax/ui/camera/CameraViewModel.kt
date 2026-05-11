package com.example.camerax.ui.camera

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Recording
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.video.AudioConfig
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import com.example.camerax.analyzer.DetectedObjectResult
import com.example.camerax.util.AudioConfig as AudioConfigUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.text.SimpleDateFormat
import java.util.Locale

data class CameraState(
    val lensFacing: Int = CameraSelector.LENS_FACING_BACK,
    val isRecording: Boolean = false,
    val isPhotoMode: Boolean = true,
    val isQrMode: Boolean = false,
    val isObjectDetectionEnabled: Boolean = false,
    val lastCapturedUri: String? = null,
    val detectedQrText: String? = null,
    val detectedObjects: List<DetectedObjectResult> = emptyList()
)

class CameraViewModel : ViewModel() {

    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()

    private var recording: Recording? = null
    private var currentAudioStrategy: AudioConfigUtils.AudioFallbackStrategy =
        AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO
    private var recordingAttempts: Int = 0

    fun onFlipCamera() {
        _state.update {
            it.copy(
                lensFacing = if (it.lensFacing == CameraSelector.LENS_FACING_BACK) {
                    CameraSelector.LENS_FACING_FRONT
                } else {
                    CameraSelector.LENS_FACING_BACK
                }
            )
        }
    }

    fun toggleCameraMode() {
        _state.update { it.copy(isPhotoMode = !it.isPhotoMode) }
    }

    fun toggleQrMode() {
        _state.update { 
            it.copy(
                isQrMode = !it.isQrMode, 
                isObjectDetectionEnabled = false,
                detectedQrText = null,
                detectedObjects = emptyList()
            ) 
        }
    }

    fun toggleObjectDetection() {
        _state.update { 
            it.copy(
                isObjectDetectionEnabled = !it.isObjectDetectionEnabled, 
                isQrMode = false,
                detectedQrText = null,
                detectedObjects = emptyList()
            ) 
        }
    }

    fun onQrDetected(text: String?) {
        _state.update { it.copy(detectedQrText = text) }
    }

    fun onObjectsDetected(objects: List<DetectedObjectResult>) {
        _state.update { it.copy(detectedObjects = objects) }
    }

    fun capturePhoto(controller: LifecycleCameraController, context: Context) {
        val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/CameraProML")
            }
        }

        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(context.contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
            .build()

        controller.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(context),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val uri = output.savedUri.toString()
                    _state.update { it.copy(lastCapturedUri = uri) }
                    Toast.makeText(context, "📷 Foto guardada", Toast.LENGTH_SHORT).show()
                    Log.d("CameraVM", "Photo saved: $uri")
                }

                override fun onError(exc: ImageCaptureException) {
                    Log.e("CameraVM", "Photo capture failed: ${exc.message}", exc)
                    Toast.makeText(context, "❌ Error al capturar", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    fun recordVideo(controller: LifecycleCameraController, context: Context) {
        if (recording != null) {
            stopRecording(context)
            return
        }

        recordingAttempts = 0
        currentAudioStrategy = AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO
        startVideoRecording(controller, context)
    }

    private fun stopRecording(context: Context) {
        try {
            recording?.stop()
            recording = null
            _state.update { it.copy(isRecording = false) }
            Log.d(TAG, "Recording stopped successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error stopping recording", e)
            Toast.makeText(context, "❌ Error al detener grabación", Toast.LENGTH_SHORT).show()
        }
    }

    private fun startVideoRecording(controller: LifecycleCameraController, context: Context) {
        try {
            val name = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(System.currentTimeMillis())
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, name)
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraProML")
                }
            }

             val mediaStoreOutputOptions = MediaStoreOutputOptions
                .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build()

            Log.d(TAG, "Iniciando grabación de video con estrategia: $currentAudioStrategy")

            val audioConfigWithFallback = createAudioConfig(currentAudioStrategy)
            recording = controller.startRecording(
                mediaStoreOutputOptions,
                audioConfigWithFallback,
                ContextCompat.getMainExecutor(context)
            ) { event ->
                handleVideoRecordEvent(event, context, name, controller)
            }

            if (recording == null) {
                Log.e(TAG, "Recording iniciado pero retornó null")
                Toast.makeText(context, "❌ Error: No se pudo iniciar grabación", Toast.LENGTH_SHORT).show()
                handleRecordingFailure(context)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Excepción durante inicio de grabación: ${e.message}", e)
            handleRecordingException(e, context, controller)
        }
    }

    private fun createAudioConfig(strategy: AudioConfigUtils.AudioFallbackStrategy): AudioConfig {
        return when (strategy) {
            AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO -> {
                Log.d(TAG, "AudioConfig: Grabando CON AUDIO")
                AudioConfig.create(true)
            }
            AudioConfigUtils.AudioFallbackStrategy.WITHOUT_AUDIO -> {
                Log.d(TAG, "AudioConfig: Grabando SIN AUDIO - Fallback activo")
                AudioConfig.create(false)
            }
            AudioConfigUtils.AudioFallbackStrategy.DISABLED -> {
                Log.d(TAG, "AudioConfig: Audio COMPLETAMENTE DESHABILITADO")
                AudioConfig.create(false)
            }
        }
    }

    private fun handleRecordingException(
        exception: Exception,
        context: Context,
        controller: LifecycleCameraController
    ) {
        recordingAttempts++

        val errorMessage = when {
            recordingAttempts < AudioConfigUtils.MAX_RETRY_ATTEMPTS &&
            currentAudioStrategy == AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO -> {
                currentAudioStrategy = AudioConfigUtils.AudioFallbackStrategy.WITHOUT_AUDIO
                Log.w(TAG, "Reintentando sin audio (intento $recordingAttempts/${AudioConfigUtils.MAX_RETRY_ATTEMPTS})")
                Toast.makeText(context, "⚠️ Reinintentando sin audio...", Toast.LENGTH_SHORT).show()
                startVideoRecording(controller, context)
                return
            }
            else -> {
                "No se pudo iniciar grabación: ${exception.message}"
            }
        }

        Toast.makeText(context, "❌ $errorMessage", Toast.LENGTH_LONG).show()
        handleRecordingFailure(context)
    }

    private fun handleRecordingFailure(context: Context) {
        recording = null
        _state.update { it.copy(isRecording = false) }
        recordingAttempts = 0
        currentAudioStrategy = AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO
    }

    private fun handleVideoRecordEvent(
        event: VideoRecordEvent,
        context: Context,
        name: String,
        controller: LifecycleCameraController
    ) {
        when (event) {
            is VideoRecordEvent.Start -> {
                _state.update { it.copy(isRecording = true) }
                recordingAttempts = 0
                Log.d(TAG, "✅ Grabación iniciada con éxito")
                val audioStatus = when (currentAudioStrategy) {
                    AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO -> "con audio"
                    AudioConfigUtils.AudioFallbackStrategy.WITHOUT_AUDIO -> "sin audio"
                    AudioConfigUtils.AudioFallbackStrategy.DISABLED -> "audio deshabilitado"
                }
                Toast.makeText(context, "🎥 Grabando... ($audioStatus)", Toast.LENGTH_SHORT).show()
            }

            is VideoRecordEvent.Pause -> {
                Log.d(TAG, "⏸️ Grabación pausada")
            }

            is VideoRecordEvent.Resume -> {
                Log.d(TAG, "▶️ Grabación reanudada")
            }

            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    handleVideoRecordingError(event, context, controller)
                } else {
                    handleVideoRecordingSuccess(event, context)
                }
            }
        }
    }

    private fun handleVideoRecordingError(
        event: VideoRecordEvent.Finalize,
        context: Context,
        controller: LifecycleCameraController
    ) {
        recording?.close()
        recording = null
        _state.update { it.copy(isRecording = false) }

        val errorCode = event.error
        val errorMessage = getDetailedErrorMessage(errorCode, event.cause)

        Log.e(TAG, "❌ Error de grabación: Código=$errorCode, Mensaje=$errorMessage")
        Log.e(TAG, "Error causa: ${event.cause}")

        // Estrategia de fallback para errores de audio/codec
        if (errorCode == VIDEO_RECORD_ERROR_AUDIO_CODEC &&
            currentAudioStrategy == AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO &&
            recordingAttempts < AudioConfigUtils.MAX_RETRY_ATTEMPTS) {

            Log.w(TAG, "Error de codec detectado - Reintentando sin audio...")
            currentAudioStrategy = AudioConfigUtils.AudioFallbackStrategy.WITHOUT_AUDIO
            recordingAttempts++
            Toast.makeText(context, "⚠️ Error de codec de audio - Reintentando sin audio", Toast.LENGTH_LONG).show()
            startVideoRecording(controller, context)
        } else {
            Toast.makeText(context, "❌ Error: $errorMessage", Toast.LENGTH_LONG).show()
            handleRecordingFailure(context)
        }
    }

    private fun handleVideoRecordingSuccess(
        event: VideoRecordEvent.Finalize,
        context: Context
    ) {
        try {
            val uri = event.outputResults.outputUri.toString()
            _state.update { it.copy(isRecording = false, lastCapturedUri = uri) }
            recording?.close()
            recording = null

            val audioStatus = when (currentAudioStrategy) {
                AudioConfigUtils.AudioFallbackStrategy.WITH_AUDIO -> "✅ Con audio"
                AudioConfigUtils.AudioFallbackStrategy.WITHOUT_AUDIO -> "⚠️ Sin audio"
                AudioConfigUtils.AudioFallbackStrategy.DISABLED -> "Audio deshabilitado"
            }

            Toast.makeText(context, "🎥 Video guardado ($audioStatus)", Toast.LENGTH_SHORT).show()
            Log.d(TAG, "✅ Video guardado exitosamente: $uri")

            handleRecordingFailure(context)
        } catch (e: Exception) {
            Log.e(TAG, "Error finalizando grabación: ${e.message}", e)
            Toast.makeText(context, "❌ Error al guardar video", Toast.LENGTH_SHORT).show()
            handleRecordingFailure(context)
        }
    }

    private fun getDetailedErrorMessage(errorCode: Int, cause: Throwable?): String {
        val baseMessage = getErrorMessage(errorCode)
        return if (cause != null) {
            "$baseMessage (${cause.javaClass.simpleName}: ${cause.message})"
        } else {
            baseMessage
        }
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            // Video Recording Error Codes (API Reference)
            0 -> "Error en opciones de salida - Verifica permisos de almacenamiento"
            1 -> "Error en codificación de video - Reintenta grabación"
            2 -> "Error en mezcla de audio/video - Codec incompatible"
            3 -> "Límite de tamaño de archivo alcanzado"
            4 -> "Error de audio o codec de audio no soportado"
            5 -> "Límite de tamaño de archivo alcanzado"
            6 -> "Límite de duración alcanzado"
            7 -> "Almacenamiento insuficiente disponible"
            8 -> "Fuente de video inactiva - Reactivar cámara"
            9 -> "Audio deshabilitado en configuración del sistema"

            // Additional common errors
            else -> {
                when {
                    errorCode < 0 -> "Error del sistema ($errorCode) - Contacta soporte"
                    else -> "Error desconocido ($errorCode) - Verifica los logs"
                }
            }
        }
    }

    companion object {
        private const val TAG = "CameraViewModel"
        private const val VIDEO_RECORD_ERROR_AUDIO_CODEC = 4
    }
}
