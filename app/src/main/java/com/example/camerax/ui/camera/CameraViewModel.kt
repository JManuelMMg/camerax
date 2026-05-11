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
            try {
                recording?.stop()
                recording = null
                _state.update { it.copy(isRecording = false) }
            } catch (e: Exception) {
                Log.e("CameraVM", "Error stopping recording: ${e.message}")
            }
            return
        }

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

            Log.d("CameraVM", "Starting video recording with audio enabled...")

            // Intentar grabar con audio, si falla, grabar sin audio
            try {
                recording = controller.startRecording(
                    mediaStoreOutputOptions,
                    AudioConfig.create(true),
                    ContextCompat.getMainExecutor(context)
                ) { event ->
                    handleVideoRecordEvent(event, context, name)
                }
            } catch (audioException: Exception) {
                Log.w("CameraVM", "Failed to record with audio, trying without audio: ${audioException.message}")
                Log.d("CameraVM", "Starting video recording without audio...")
                recording = controller.startRecording(
                    mediaStoreOutputOptions,
                    AudioConfig.create(false),
                    ContextCompat.getMainExecutor(context)
                ) { event ->
                    handleVideoRecordEvent(event, context, name)
                }
            }

            if (recording == null) {
                Log.e("CameraVM", "Failed to start recording - got null")
                Toast.makeText(context, "❌ No se pudo iniciar grabación", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("CameraVM", "Exception during video recording: ${e.message}", e)
            e.printStackTrace()
            Toast.makeText(context, "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun handleVideoRecordEvent(event: VideoRecordEvent, context: Context, name: String) {
        when (event) {
            is VideoRecordEvent.Start -> {
                _state.update { it.copy(isRecording = true) }
                Log.d("CameraVM", "Recording started")
                Toast.makeText(context, "🎥 Grabando...", Toast.LENGTH_SHORT).show()
            }
            is VideoRecordEvent.Pause -> {
                Log.d("CameraVM", "Recording paused")
            }
            is VideoRecordEvent.Resume -> {
                Log.d("CameraVM", "Recording resumed")
            }
            is VideoRecordEvent.Finalize -> {
                if (event.hasError()) {
                    recording?.close()
                    recording = null
                    _state.update { it.copy(isRecording = false) }
                    val errorCode = event.error
                    Log.e("CameraVM", "Video recording error: $errorCode")
                    Log.e("CameraVM", "Error cause: ${event.cause}")
                    
                    // Mapeo completo de códigos de error de CameraX
                    val errorMessage = getErrorMessage(errorCode)
                    Log.e("CameraVM", "Error Message: $errorMessage")
                    
                    Toast.makeText(context, "❌ Error: $errorMessage", Toast.LENGTH_LONG).show()
                } else {
                    val uri = event.outputResults.outputUri.toString()
                    _state.update { it.copy(isRecording = false, lastCapturedUri = uri) }
                    recording?.close()
                    recording = null
                    Toast.makeText(context, "🎥 Video guardado", Toast.LENGTH_SHORT).show()
                    Log.d("CameraVM", "Video saved: $uri")
                }
            }
        }
    }

    private fun getErrorMessage(errorCode: Int): String {
        return when (errorCode) {
            0 -> "Error en opciones de salida (Invalid Output Options)"
            1 -> "Error en codificación de video"
            2 -> "Error en mezcla de audio/video (Muxer Error)"
            3 -> "Límite de tamaño de archivo alcanzado"
            4 -> "Error de audio o codec no soportado - Grabando sin audio"
            5 -> "Límite de tamaño de archivo alcanzado"
            6 -> "Límite de duración alcanzado"
            7 -> "Almacenamiento insuficiente disponible"
            8 -> "Fuente de video inactiva"
            9 -> "Audio deshabilitado por inactividad"
            else -> "Error desconocido ($errorCode) - Verifica los logs para más detalles"
        }
    }
}
