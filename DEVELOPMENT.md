# Guía de Desarrollo

Documentación técnica detallada para desarrolladores que trabajan en **Camera Pro ML**.

## Configuración del Ambiente

### Herramientas Requeridas
- **Android Studio**: 2024.1+
- **JDK**: 11+
- **Gradle**: 8.7.3+
- **Git**: 2.34+
- **Android SDK**:
  - Minimum SDK: 24
  - Target SDK: 35
  - Build Tools: 35

### Setup Inicial

```bash
# 1. Clonar repositorio
git clone https://github.com/usuario/camerax.git
cd camerax

# 2. Sincronizar Gradle
./gradlew sync

# 3. Compilar proyecto
./gradlew build

# 4. Ejecutar tests
./gradlew test
```

## Arquitectura

### Capas

#### 1. **Data Layer** (No implementado aún)
- Repositorios
- Modelos de datos
- Acceso a base de datos

#### 2. **Domain Layer** (No implementado aún)
- Use cases
- Lógica de negocio
- Interfaces de repositorios

#### 3. **UI Layer** (Implementado)
```
ui/
├── camera/
│   ├── CameraScreen.kt       # Pantalla principal
│   ├── CameraViewModel.kt    # ViewModel
│   ├── CameraControls.kt     # Componentes UI
│   └── GalleryScreen.kt      # Galería
└── theme/
    ├── Color.kt              # Colores
    ├── Theme.kt              # Tema Material 3
    └── Type.kt               # Tipografía
```

### Patrón MVVM

El proyecto usa **Model-View-ViewModel**:

```kotlin
// ViewModel
class CameraViewModel : ViewModel() {
    private val _state = MutableStateFlow(CameraState())
    val state: StateFlow<CameraState> = _state.asStateFlow()
    
    fun onAction() {
        _state.update { /* cambios */ }
    }
}

// Composable
@Composable
fun CameraScreen() {
    val state by viewModel.state.collectAsState()
    // Observa cambios de state
}
```

## Componentes Tecnológicos

### CameraX
Captura de fotos y videos a través de `LifecycleCameraController`.

**Características**:
- IMAGE_CAPTURE: Fotos
- VIDEO_CAPTURE: Videos
- IMAGE_ANALYSIS: Análisis en vivo

```kotlin
controller.setEnabledUseCases(
    LifecycleCameraController.IMAGE_CAPTURE or
    LifecycleCameraController.VIDEO_CAPTURE or
    LifecycleCameraController.IMAGE_ANALYSIS
)
```

### ML Kit

#### Barcode Scanning
```kotlin
val scanner = BarcodeScanning.getClient()
scanner.process(image)
    .addOnSuccessListener { barcodes ->
        val qrValue = barcodes.first().rawValue
    }
```

#### Object Detection
```kotlin
val options = ObjectDetectorOptions.Builder()
    .setDetectorMode(ObjectDetectorOptions.STREAM_MODE)
    .enableClassification()
    .build()
val detector = ObjectDetection.getClient(options)
```

### Jetpack Compose

**State Management**:
```kotlin
// StateFlow para estado
private val _state = MutableStateFlow(initialValue)
val state = _state.asStateFlow()

// Observar en Composables
val state by viewModel.state.collectAsState()
```

**Side Effects**:
```kotlin
LaunchedEffect(key) {
    // Se ejecuta cuando 'key' cambia
}

SideEffect {
    // Se ejecuta después de cada recomposición
}
```

## Flujos de Trabajo

### Captura de Foto

```
Usuario presiona botón
    ↓
CameraViewModel.capturePhoto()
    ↓
LifecycleCameraController.takePicture()
    ↓
Guardado en MediaStore
    ↓
URI actualizado en state
    ↓
UI se recompone con thumbnail
```

### Detección de QR

```
Usuario activa modo QR
    ↓
setImageAnalysisAnalyzer(QrCodeAnalyzer)
    ↓
Cada frame se analiza
    ↓
QR detectado
    ↓
onQrDetected(value) actualiza state
    ↓
Overlay muestra valor
```

## Testing

### Tests Unitarios
```kotlin
@Test
fun testCameraStateDefaults() {
    val state = CameraState()
    assertTrue(state.isPhotoMode)
}
```

**Ubicación**: `app/src/test/java/`

### Tests Instrumentados
```kotlin
@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("com.example.camerax", appContext.packageName)
    }
}
```

**Ubicación**: `app/src/androidTest/java/`

## Debugging

### Logging
```kotlin
import android.util.Log

val TAG = "CameraVM"
Log.d(TAG, "Debug message")
Log.e(TAG, "Error message", exception)
```

### Android Studio Profiler
- Memory: Detectar memory leaks
- CPU: Identificar bottlenecks
- Battery: Consumo de batería
- Network: Actividad de red

### Debugger
```
Ejecutar → Debug 'app'
Breakpoints en líneas de código
Variables en Debug window
Step over/into/out
```

## Build Variants

### Debug
```bash
./gradlew assembleDebug
```
- ProGuard deshabilitado
- Debuggeable
- Tamaño mayor

### Release
```bash
./gradlew assembleRelease
```
- ProGuard habilitado (minificación)
- No debuggeable
- Tamaño optimizado

## Instalación en Dispositivo

### Conectar dispositivo
1. Habilitar "Opciones de desarrollador" (7 toques en Build Number)
2. Habilitar "Depuración USB"
3. Conectar por USB

### Instalar app
```bash
# Automático desde Android Studio
# O manualmente
./gradlew installDebug
```

### Ver logs
```bash
adb logcat
adb logcat | grep CameraVM
```

## Performance

### Optimizaciones Aplicadas
1. **LaunchedEffect**: Evita recomposiciones innecesarias
2. **remember**: Cachea valores calculados
3. **StateFlow**: Estado reactivo eficiente
4. **Coil**: Carga lazy de imágenes

### Área de Mejora
1. Coordinate Transform: Mapeo sensor → canvas
2. Object Detection Throttling: Reducir análisis por frame
3. Memory Management: Limpiar analizadores

## Convenciones de Código

### Nombres de Variables
```kotlin
// ViewModel
private val _state = MutableStateFlow(...)
val state = _state.asStateFlow()

// Composables
@Composable
fun MyComposable() { }

// Funciones
fun capturePhoto() { }

// Constantes
const val TIMEOUT_MS = 5000
```

### Organización de Archivos
```
camerax/
├── analyzer/         # ML Kit analyzers
├── ui/
│   ├── camera/       # Camera screens
│   └── theme/        # Styling
└── util/            # Utilidades
```

## Próximos Pasos

1. **Implementar arquitectura multi-capa** (Data, Domain, UI)
2. **Agregar Room Database** para historial
3. **Usar Hilt** para inyección de dependencias
4. **Tests con MockK** o Mockito
5. **CI/CD con GitHub Actions**

---

¿Dudas? Revisa los archivos de código o abre una issue 📝

