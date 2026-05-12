package com.example.camerax.analyzer

import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.math.abs

class QrCodeAnalyzer(
    private val onQrCodeDetected: (String?, Boolean) -> Unit // Añadido parámetro para indicar si es un enlace
) : ImageAnalysis.Analyzer {

    private val scanner = BarcodeScanning.getClient()
    // ✅ CORRECIÓN: Single-threaded executor para evitar congelamiento
    private val analysisExecutor = Executors.newSingleThreadExecutor()

    // ✅ CORRECIÓN: Rate limiting - procesa solo 1 frame cada 300ms (optimizado para mejor detección)
    private var lastAnalysisTime = 0L
    private var lastDetectedValue: String? = null
    private val ANALYSIS_INTERVAL_MS = 300L
    private val DUPLICATE_DETECTION_SKIP = 2000L
    private var lastReportedTime = 0L

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
                                    if (detectedValue != null) {
                                        // Reportar si es diferente del último o ha pasado tiempo suficiente
                                        if (detectedValue != lastDetectedValue || 
                                            (currentTime - lastReportedTime) > DUPLICATE_DETECTION_SKIP) {
                                            lastDetectedValue = detectedValue
                                            lastReportedTime = currentTime
                                            val isLink = detectedValue.startsWith("http://") || detectedValue.startsWith("https://")
                                            Log.d(TAG, "📱 QR Detectado - Valor: $detectedValue, Es Link: $isLink")
                                            onQrCodeDetected(detectedValue, isLink) // Indicar si es un enlace
                                        }
                                    } else {
                                        Log.d(TAG, "⚠️ QR detectado pero el valor es null")
                                    }
                                }
                            } catch (e: Exception) {
                                Log.e(TAG, "❌ Error procesando QR detectado: ${e.message}", e)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e(TAG, "❌ Error en scanner: ${exception.message}", exception)
                        }
                        .addOnCompleteListener {
                            imageProxy.close()
                        }
                } else {
                    imageProxy.close()
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error en analyze: ${e.message}", e)
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
        Log.d(TAG, "QrCodeAnalyzer liberado")
    }

    companion object {
        private const val TAG = "QrCodeAnalyzer"
    }
}
