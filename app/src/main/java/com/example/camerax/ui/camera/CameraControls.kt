package com.example.camerax.ui.camera

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.camerax.R

@Composable
fun CameraControls(
    state: CameraState,
    onFlipCamera: () -> Unit,
    onCapture: () -> Unit,
    onToggleMode: () -> Unit,
    onToggleQr: () -> Unit,
    onToggleObjects: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .safeDrawingPadding()
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top: Mode Status Display - Mejorado
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .padding(bottom = 16.dp),
            color = Color.Black.copy(alpha = 0.7f),
            shape = RoundedCornerShape(16.dp),
            shadowElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
        // Modo de captura
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    androidx.compose.foundation.Image(
                        painter = if (state.isPhotoMode) painterResource(R.drawable.camera) else painterResource(R.drawable.videocamera),
                        contentDescription = if (state.isPhotoMode) "Camera" else "Video Camera",
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 4.dp)
                    )
                    Text(
                        text = if (state.isPhotoMode) "FOTO" else "VIDEO",
                        color = Color(0xFF00BCD4),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }

                HorizontalDivider(
                    color = Color.White.copy(alpha = 0.2f),
                    modifier = Modifier
                        .height(30.dp)
                        .width(1.dp)
                )

                // Modo de análisis
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    androidx.compose.foundation.Image(
                        painter = when {
                            state.isQrMode -> painterResource(R.drawable.qr)
                            state.isObjectDetectionEnabled -> painterResource(R.drawable.deteccion)
                            else -> painterResource(R.drawable.camera)
                        },
                        contentDescription = when {
                            state.isQrMode -> "QR"
                            state.isObjectDetectionEnabled -> "Detection"
                            else -> "Normal"
                        },
                        modifier = Modifier
                            .size(24.dp)
                            .padding(bottom = 4.dp)
                    )
                    Text(
                        text = when {
                            state.isQrMode -> "QR"
                            state.isObjectDetectionEnabled -> "OBJETOS"
                            else -> "NORMAL"
                        },
                        color = if (state.isQrMode || state.isObjectDetectionEnabled) Color(0xFF00BCD4) else Color.White.copy(alpha = 0.5f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // Mode Selection Row - Mejorado con mejor spacing
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // QR Mode
            ModeButtonWithImage(
                isActive = state.isQrMode,
                painter = painterResource(R.drawable.qr),
                label = "QR",
                onClick = onToggleQr,
                modifier = Modifier.weight(1f)
            )

            // Object Detection
            ModeButtonWithImage(
                isActive = state.isObjectDetectionEnabled,
                painter = painterResource(R.drawable.deteccion),
                label = "OBJETOS",
                onClick = onToggleObjects,
                modifier = Modifier.weight(1f)
            )

            // Flip Camera
            ModeButtonWithImage(
                isActive = false,
                painter = painterResource(R.drawable.camera),
                label = "GIRAR",
                onClick = onFlipCamera,
                modifier = Modifier.weight(1f)
            )
        }

        // Main Capture Controls - Mejorado
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Mode Toggle (Photo/Video)
            FilledIconButton(
                onClick = onToggleMode,
                modifier = Modifier.size(56.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (state.isPhotoMode) Color(0xFF00BCD4) else Color(0xFFFF5252)
                )
            ) {
                Icon(
                    imageVector = if (state.isPhotoMode) Icons.Default.Settings else Icons.Default.Add,
                    contentDescription = "Toggle Mode",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Capture/Record Button (Large Center Button)
            val captureButtonColor by animateColorAsState(
                targetValue = if (state.isRecording) Color(0xFFFF6B6B) else Color(0xFF00BCD4),
                animationSpec = tween(300),
                label = "CaptureButtonColor"
            )

            Button(
                onClick = onCapture,
                modifier = Modifier.size(90.dp),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = captureButtonColor
                ),
                contentPadding = PaddingValues(0.dp),
                elevation = ButtonDefaults.elevatedButtonElevation(
                    defaultElevation = 12.dp,
                    pressedElevation = 8.dp
                )
            ) {
                Icon(
                    imageVector = if (state.isRecording) Icons.Default.Close else Icons.Default.FavoriteBorder,
                    contentDescription = if (state.isRecording) "Stop Recording" else "Capture",
                    tint = Color.White,
                    modifier = Modifier.size(40.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Recording Status or Empty Space
            Box(
                modifier = Modifier.size(56.dp),
                contentAlignment = Alignment.Center
            ) {
                if (state.isRecording) {
                    RecordingIndicator()
                }
            }
        }

        // Detection Results Display - Mejorado
        if (state.detectedQrText != null && state.isQrMode) {
            Spacer(modifier = Modifier.height(16.dp))
            DetectionCard(
                iconPainter = painterResource(R.drawable.qr),
                iconContentDescription = "QR",
                title = "QR DETECTADO",
                content = state.detectedQrText ?: "Unknown",
                backgroundColor = Color(0xFF6650a4)
            )
        }

        if (state.detectedObjects.isNotEmpty() && state.isObjectDetectionEnabled) {
            Spacer(modifier = Modifier.height(16.dp))
            DetectionCard(
                iconPainter = painterResource(R.drawable.deteccion),
                iconContentDescription = "Objects",
                title = "OBJETOS",
                content = "${state.detectedObjects.size} detectado${if (state.detectedObjects.size > 1) "s" else ""}",
                backgroundColor = Color(0xFF00BCD4),
                textColor = Color.Black
            )
        }
    }
}

@Composable
fun ModeButtonWithImage(
    isActive: Boolean,
    painter: Painter,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
    ) {
        IconButton(
            onClick = onClick,
            modifier = Modifier
                .size(50.dp)
                .background(
                    color = if (isActive) Color(0xFF6650a4) else Color.White.copy(alpha = 0.1f),
                    shape = CircleShape
                )
        ) {
            androidx.compose.foundation.Image(
                painter = painter,
                contentDescription = label,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            color = Color.White,
            fontSize = 9.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(top = 4.dp),
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun RecordingIndicator() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .background(Color.Red.copy(alpha = 0.95f), RoundedCornerShape(10.dp))
            .padding(8.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "●",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 16.sp
        )
        Text(
            text = "REC",
            color = Color.White,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
fun DetectionCard(
    iconPainter: Painter,
    iconContentDescription: String,
    title: String,
    content: String,
    backgroundColor: Color,
    textColor: Color = Color.White
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(0.85f)
            .padding(horizontal = 16.dp),
        color = backgroundColor.copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp),
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            androidx.compose.foundation.Image(
                painter = iconPainter,
                contentDescription = iconContentDescription,
                modifier = Modifier.size(24.dp)
            )
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    color = textColor,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.5.sp
                )
                Text(
                    text = content,
                    color = textColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }
        }
    }
}

