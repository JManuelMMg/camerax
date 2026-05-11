package com.example.camerax.analyzer

import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.math.abs

class QrCodeAnalyzer(
    private val onQrCodeDetected: (String?) -> Unit
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    // ✅ CORRECIÓN: Single-threaded executor para evitar congelamiento
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    // ✅ CORRECIÓN: Rate limiting - procesa solo 1 frame cada 500ms
    private var lastAnalysisTime = 0L
    private var lastDetectedValue: String? = null
    private val ANALYSIS_INTERVAL_MS = 500L
    private val DUPLICATE_DETECTION_SKIP = 2000L

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
                    scanner.process(image)
                        .addOnSuccessListener { barcodes ->
                            try {
                                if (barcodes.isNotEmpty()) {
                                    val detectedValue = barcodes.first().rawValue
                                    // ✅ CORRECIÓN: Evitar actualización si es el mismo valor
                                    if (detectedValue != lastDetectedValue) {
                                        lastDetectedValue = detectedValue
                                        onQrCodeDetected(detectedValue)
                                    }
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

    fun release() {
        analysisExecutor.shutdown()
        scanner.close()
    }
}
