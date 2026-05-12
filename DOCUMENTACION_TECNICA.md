# Documentación Técnica: Sistema de Visión Inteligente Pro

## 1. Propósito de la Aplicación
La aplicación **CameraX Vision Pro** es una solución avanzada de visión artificial para dispositivos Android. Su objetivo principal es proporcionar una interfaz de cámara de alto rendimiento capaz de realizar **detección de objetos y escaneo de códigos QR en tiempo real**, permitiendo a los usuarios interactuar con su entorno físico a través de modelos de Machine Learning (ML) integrados directamente en el dispositivo (On-Device Inference).

---

## 2. Arquitectura del Sistema
El sistema sigue una arquitectura **MVVM (Model-View-ViewModel)** complementada con una canalización de procesamiento de imágenes asíncrona.

### Flujo de Datos
1.  **Captura (CameraX):** El `LifecycleCameraController` gestiona el hardware de la cámara y expone un flujo de frames a través del caso de uso `ImageAnalysis`.
2.  **Análisis (Analyzers):** Los frames se interceptan en implementaciones de `ImageAnalysis.Analyzer`. Para optimizar el rendimiento, se implementa un mecanismo de *throttling* (procesando aprox. 1.2 frames por segundo) para evitar la saturación del procesador.
3.  **Inferencia (ML Kit):** El frame convertido a `InputImage` se envía al SDK de Google ML Kit para la detección de objetos o códigos de barras.
4.  **Actualización de Estado (ViewModel):** Los resultados de la inferencia se filtran mediante una lógica de *debounce* y se emiten a través de un `StateFlow` en el `CameraViewModel`.
5.  **Renderizado (Jetpack Compose):** La interfaz de usuario observa el estado y dibuja superposiciones (Overlays) gráficas (bounding boxes y etiquetas) sobre el `PreviewView`.

---

## 3. Stack Tecnológico
*   **Lenguaje:** Kotlin 1.9+ (Coroutines & Flow).
*   **UI Framework:** Jetpack Compose (Material 3).
*   **Cámara:** Android CameraX (v1.4.1).
*   **Machine Learning:** Google ML Kit (Object Detection & Barcode Scanning).
*   **Arquitectura:** Jetpack ViewModel, StateFlow.
*   **Manejo de Imágenes:** Coil para carga de miniaturas.
*   **Requerimiento Mínimo:** Android SDK 24 (Nougat).

---

## 4. Detalle de Clases Principales

### `ObjectDetectionAnalyzer`
Implementa la interfaz `ImageAnalysis.Analyzer`. Se encarga de la detección de objetos.
*   **Modo:** `STREAM_MODE` para seguimiento continuo.
*   **Optimización:** Utiliza un `SingleThreadExecutor` dedicado para desacoplar el análisis del hilo de la UI y gestiona el cierre de `ImageProxy` para evitar fugas de memoria.

### `QrCodeAnalyzer`
Especializado en el escaneo de códigos de barras y QR.
*   **Lógica de Debounce:** Implementa una pausa de 500ms entre escaneos exitosos para evitar lecturas duplicadas innecesarias en el `ViewModel`.

### `CameraViewModel`
El motor lógico de la aplicación.
*   **Gestión de Estado:** Mantiene el `CameraState` que incluye modos de cámara, estado de grabación y resultados de detección.
*   **Fallback de Audio:** Implementa una lógica robusta que detecta fallos en el codec de audio durante la grabación de video, reintentando automáticamente la captura sin audio para asegurar la persistencia del archivo.

### `CameraScreen`
Componente Composable que integra la vista previa de la cámara con la UI de control.
*   **Overlays:** Utiliza un `Canvas` de Compose para dibujar las cajas delimitadoras de los objetos detectados basándose en las coordenadas normalizadas del modelo.

---

## 5. Guía de Implementación

### Requisitos de Software
1.  **Android Studio:** Jellyfish o superior.
2.  **Gradle:** Versión 8.0+ con soporte para Kotlin DSL.
3.  **Dependencias Críticas:**
    ```kotlin
    implementation("androidx.camera:camera-mlkit-vision:1.4.1")
    implementation("com.google.mlkit:object-detection:17.0.2")
    implementation("com.google.mlkit:barcode-scanning:17.3.0")
    ```

### Requisitos de Hardware
*   **Cámara:** Sensor trasero/frontal con soporte para Camera2 API.
*   **Procesador:** Recomendado ARM64 con soporte para instrucciones de aceleración de ML para mantener FPS estables.
*   **Memoria:** Mínimo 2GB RAM.

### Configuración de Permisos
La app requiere los siguientes permisos declarados en el `AndroidManifest.xml`:
*   `android.permission.CAMERA`
*   `android.permission.RECORD_AUDIO`
*   `android.permission.WRITE_EXTERNAL_STORAGE` (para niveles de API ≤ 28)

---

## 6. Consideraciones Técnicas
*   **Ciclo de Vida:** El uso de `LifecycleCameraController` garantiza que los recursos de hardware se liberen automáticamente cuando la aplicación entra en segundo plano.
*   **Rendimiento:** El procesamiento de imágenes se realiza de forma no bloqueante. Si el buffer de la cámara se llena, los frames excedentes se descartan automáticamente para priorizar la latencia actual.
