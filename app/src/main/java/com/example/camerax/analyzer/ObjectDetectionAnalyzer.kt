package com.example.camerax.analyzer

import android.graphics.Rect
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.objects.ObjectDetection
import com.google.mlkit.vision.objects.defaults.ObjectDetectorOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private val analyzerScope = CoroutineScope(Dispatchers.Default)

    @OptIn(ExperimentalGetImage::class)
    override fun analyze(imageProxy: ImageProxy) {
        analyzerScope.launch {
            try {
                val mediaImage = imageProxy.image
                if (mediaImage != null) {
                    val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                    detector.process(image)
                        .addOnSuccessListener { objects ->
                            val results = objects.map { obj ->
                                DetectedObjectResult(
                                    boundingBox = obj.boundingBox,
                                    label = obj.labels.firstOrNull()?.text ?: "Unknown",
                                    confidence = obj.labels.firstOrNull()?.confidence ?: 0f
                                )
                            }
                            onObjectsDetected(results)
                        }
                        .addOnFailureListener {
                            // Error silencioso
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            } catch (e: Exception) {
                imageProxy.close()
            }
        }
    }
}
