package com.example.camerax.ui.camera

// ...existing imports...
import android.Manifest
import android.util.Log
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.camera.core.CameraSelector
import androidx.camera.view.LifecycleCameraController
import androidx.camera.view.PreviewView
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner as ComposeLocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.camerax.analyzer.ObjectDetectionAnalyzer
import com.example.camerax.analyzer.QrCodeAnalyzer
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraScreen(viewModel: CameraViewModel = viewModel()) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var showGallery by remember { mutableStateOf(false) }

    val permissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        )
    )

    LaunchedEffect(Unit) {
        permissionsState.launchMultiplePermissionRequest()
    }

    Crossfade(targetState = showGallery, label = "ScreenTransition") { isGalleryVisible ->
        if (isGalleryVisible && state.lastCapturedUri != null) {
            GalleryScreen(uri = state.lastCapturedUri!!) {
                showGallery = false
            }
        } else {
            CameraPreviewContent(
                state = state,
                viewModel = viewModel,
                permissionsState = permissionsState,
                onOpenGallery = { showGallery = true }
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun CameraPreviewContent(
    state: CameraState,
    viewModel: CameraViewModel,
    permissionsState: com.google.accompanist.permissions.MultiplePermissionsState,
    onOpenGallery: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = ComposeLocalLifecycleOwner.current

    if (!permissionsState.allPermissionsGranted) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(32.dp)
            ) {
                Text(
                    "Se requieren permisos de Cámara y Micrófono",
                    color = Color.White,
                    modifier = Modifier.padding(bottom = 16.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
                Button(onClick = { permissionsState.launchMultiplePermissionRequest() }) {
                    Text("Solicitar Permisos")
                }
            }
        }
        return
    }

    var isCameraReady by remember { mutableStateOf(false) }

    // ✅ CORRECIÓN: Guardar referencia a los analizadores para limpiarlos
    var currentQrAnalyzer: QrCodeAnalyzer? by remember { mutableStateOf(null) }
    var currentObjectAnalyzer: ObjectDetectionAnalyzer? by remember { mutableStateOf(null) }

    val controller = remember {
        LifecycleCameraController(context).apply {
            setEnabledUseCases(
                LifecycleCameraController.IMAGE_CAPTURE or
                        LifecycleCameraController.VIDEO_CAPTURE or
                        LifecycleCameraController.IMAGE_ANALYSIS
            )
        }
    }

    LaunchedEffect(Unit) {
        try {
            controller.bindToLifecycle(lifecycleOwner)
            isCameraReady = true
        } catch (e: Exception) {
            Log.e("CameraScreen", "Error binding camera to lifecycle", e)
        }
    }

    SideEffect {
        try {
            controller.cameraSelector = CameraSelector.Builder()
                .requireLensFacing(state.lensFacing)
                .build()
        } catch (e: Exception) {
            Log.e("CameraScreen", "Error changing camera selector", e)
        }
    }

    LaunchedEffect(state.isQrMode, state.isObjectDetectionEnabled) {
        try {
            // ✅ CORRECIÓN: Limpiar analizadores anteriores
            currentQrAnalyzer?.release()
            currentObjectAnalyzer?.release()

            if (state.isQrMode) {
                val qrAnalyzer = QrCodeAnalyzer { viewModel.onQrDetected(it) }
                currentQrAnalyzer = qrAnalyzer
                controller.setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    qrAnalyzer
                )
            } else if (state.isObjectDetectionEnabled) {
                val objectAnalyzer = ObjectDetectionAnalyzer { viewModel.onObjectsDetected(it) }
                currentObjectAnalyzer = objectAnalyzer
                controller.setImageAnalysisAnalyzer(
                    ContextCompat.getMainExecutor(context),
                    objectAnalyzer
                )
            } else {
                controller.clearImageAnalysisAnalyzer()
            }
        } catch (e: Exception) {
            Log.e("CameraScreen", "Error setting analyzer", e)
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // ✅ CORRECIÓN: Limpiar recursos al salir
            currentQrAnalyzer?.release()
            currentObjectAnalyzer?.release()
            controller.unbind()
        }
    }

    if (!isCameraReady) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = Color.White)
        }
        return
    }

    Box(modifier = Modifier.fillMaxSize()) {
        // Camera Preview
        AndroidView(
            factory = { context ->
                PreviewView(context).apply {
                    layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.PERFORMANCE
                    this.controller = controller
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                previewView.controller = controller
            }
        )

        // Object Detection Overlay
        if (state.isObjectDetectionEnabled && state.detectedObjects.isNotEmpty()) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                state.detectedObjects.forEach { obj ->
                    // Draw bounding box
                    drawRect(
                        color = Color.Cyan,
                        topLeft = androidx.compose.ui.geometry.Offset(
                            obj.boundingBox.left.toFloat(),
                            obj.boundingBox.top.toFloat()
                        ),
                        size = androidx.compose.ui.geometry.Size(
                            obj.boundingBox.width().toFloat(),
                            obj.boundingBox.height().toFloat()
                        ),
                        style = Stroke(width = 3f)
                    )

                    // Draw label background
                    drawRect(
                        color = Color.Cyan.copy(alpha = 0.6f),
                        topLeft = androidx.compose.ui.geometry.Offset(
                            obj.boundingBox.left.toFloat(),
                            (obj.boundingBox.top - 24).toFloat()
                        ),
                        size = androidx.compose.ui.geometry.Size(150f, 24f)
                    )
                }
            }
        }

        // QR Detection Overlay
        if (state.isQrMode && state.detectedQrText != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
                    .padding(top = 32.dp),
                color = Color.Black.copy(alpha = 0.7f),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    state.detectedQrText!!,
                    color = Color.White,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        // Thumbnail Gallery Access
        if (state.lastCapturedUri != null) {
            Box(
                modifier = Modifier
                    .safeDrawingPadding()
                    .padding(16.dp)
                    .size(60.dp)
                    .align(Alignment.TopEnd)
                    .clip(RoundedCornerShape(8.dp))
                    .border(2.dp, Color.White, RoundedCornerShape(8.dp))
                    .clickable { onOpenGallery() }
            ) {
                AsyncImage(
                    model = state.lastCapturedUri,
                    contentDescription = "Last Capture",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }

        // Camera Controls
        CameraControls(
            state = state,
            onFlipCamera = viewModel::onFlipCamera,
            onCapture = {
                if (state.isPhotoMode) viewModel.capturePhoto(controller, context)
                else viewModel.recordVideo(controller, context)
            },
            onToggleMode = viewModel::toggleCameraMode,
            onToggleQr = viewModel::toggleQrMode,
            onToggleObjects = viewModel::toggleObjectDetection,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

