package com.example.camerax.analyzer

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import java.util.concurrent.Executors

data class DetectedObjectResult(
    val boundingBox: Rect,
    val label: String,
    val confidence: Float
)

class ObjectDetectionAnalyzer(
    private val onObjectsDetected: (List<DetectedObjectResult>) -> Unit
) : ImageAnalysis.Analyzer {

    private val options = ObjectDetectorOptions.Builder()
        .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
        .enableClassification()
        .build()

    private val detector = ObjectDetection.getClient(options)
    // ✅ CORRECIÓN: Single-threaded executor para evitar congelamiento
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    // ✅ CORRECIÓN: Rate limiting - procesa solo 1 frame cada 300ms (optimizado para mejor fluidez)
    private var lastAnalysisTime = 0L
    private var lastDetectedObjects: List<DetectedObjectResult>? = null
    private val ANALYSIS_INTERVAL_MS = 300L

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        val currentTime = System.currentTimeMillis()

        // ✅ CORRECIÓN: Throttle - saltar frames si procesamos muy frecuentemente
        if (currentTime - lastAnalysisTime < ANALYSIS_INTERVAL_MS) {
            imageProxy.close()
            return
        }

        lastAnalysisTime = currentTime

        analysisExecutor.execute {
            try {
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    detector.process(image)
                        .addOnSuccessListener { objects ->
                            try {
                                val results = objects.map { obj ->
                                    DetectedObjectResult(
                                        boundingBox = obj.boundingBox,
                                        label = obj.labels.firstOrNull()?.text ?: "Unknown",
                                        confidence = obj.labels.firstOrNull()?.confidence ?: 0f
                                    )
                                }
                                // ✅ CORRECIÓN: Evitar actualización si los objetos no cambiaron significativamente
                                if (shouldUpdateObjects(results)) {
                                    lastDetectedObjects = results
                                    onObjectsDetected(results)
                                }
                            } catch (e: Exception) {
                                // Error silencioso
                            }
                        }
                        .addOnFailureListener { _ ->
                            // Error silencioso en procesamiento
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            } catch (e: Exception) {
                try {
                    imageProxy.close()
                } catch (_: Exception) {
                    // Ya cerrado
                }
            }
        }
    }

    // ✅ CORRECIÓN: Debounce - solo actualizar si hay cambios significativos
    private fun shouldUpdateObjects(newResults: List<DetectedObjectResult>): Boolean {
        if (lastDetectedObjects == null) return true
        if (newResults.size != lastDetectedObjects?.size) return true

        // Comparar si los objetos cambiaron significativamente
        return newResults.zip(lastDetectedObjects!!).any { (new, old) ->
            new.label != old.label ||
            Math.abs(new.confidence - old.confidence) > 0.1f
        }
    }

    fun release() {
        analysisExecutor.shutdown()
        detector.close()
    }
}
