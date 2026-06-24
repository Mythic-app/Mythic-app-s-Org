package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

data class ARMarker(
    val id: String,
    val label: String,
    val distance: String,
    val description: String,
    val alignment: Alignment,
    val offsetX: Int,
    val offsetY: Int
)

@Composable
fun ARScreen(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Camera Permissions
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasCameraPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // AR floating tags
    val arMarkers = remember {
        listOf(
            ARMarker("m1", "Sigiriya Sky Palace", "14m", "The ultimate royal residence of King Kashyapa built on flat rock top.", Alignment.Center, 0, -120),
            ARMarker("m2", "Lion's Paw Gate", "110m", "The majestic massive masonry entrance guarding the final summit climb.", Alignment.CenterStart, 40, 80),
            ARMarker("m3", "Mirror Wall", "45m", "An incredibly polished plaster wall covered in 8th-century visitor graffiti.", Alignment.CenterEnd, -40, -40),
            ARMarker("m4", "Royal Water Gardens", "220m", "One of the oldest landscaped gardens in Asia, with working hydraulic systems.", Alignment.BottomCenter, 0, -180)
        )
    }

    var selectedMarker by remember { mutableStateOf<ARMarker?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .testTag("ar_screen_root")
    ) {
        if (hasCameraPermission) {
            // CameraX preview as HUD background
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        val cameraProvider = cameraProviderFuture.get()
                        val preview = Preview.Builder().build().apply {
                            surfaceProvider = previewView.surfaceProvider
                        }
                        val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                        try {
                            cameraProvider.unbindAll()
                            cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                cameraSelector,
                                preview
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier.fillMaxSize()
            )
        }

        // Starfield HUD Overlay Simulation
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.15f))
        ) {
            // Compass heading header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 50.dp),
                contentAlignment = Alignment.TopCenter
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "12° N • SIGIRIYA ROCK",
                        color = Color(0xFF9AF04D),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 2.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Box(
                        modifier = Modifier
                            .width(180.dp)
                            .height(6.dp)
                            .border(1.dp, Color(0xFF9AF04D).copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(20.dp)
                                .offset(x = 80.dp)
                                .background(Color(0xFF9AF04D))
                        )
                    }
                }
            }

            // Floating AR Nodes
            arMarkers.forEach { marker ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = marker.alignment
                ) {
                    Box(
                        modifier = Modifier
                            .offset(x = marker.offsetX.dp, y = marker.offsetY.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Pulsing radar ring
                            Box(
                                modifier = Modifier
                                    .size(24.dp)
                                    .border(2.dp, Color(0xFF9AF04D), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(10.dp)
                                        .background(Color(0xFF9AF04D), CircleShape)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))

                            // Anchor Tag Info Card
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.Black.copy(alpha = 0.75f))
                                    .border(1.dp, Color(0xFF9AF04D).copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                    .clickable { selectedMarker = marker }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Text(
                                        text = marker.label,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "•",
                                        color = Color(0xFF9AF04D),
                                        fontSize = 12.sp
                                    )
                                    Text(
                                        text = marker.distance,
                                        color = Color(0xFF9AF04D),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Marker Detail Modal bottom sheet simulation
            AnimatedVisibility(
                visible = selectedMarker != null,
                enter = scaleIn() + fadeIn(),
                exit = scaleOut() + fadeOut(),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(20.dp)
                    .fillMaxWidth()
            ) {
                selectedMarker?.let { marker ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.9f)),
                        shape = RoundedCornerShape(24.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color(0xFF9AF04D), RoundedCornerShape(24.dp))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Info,
                                        contentDescription = "Info",
                                        tint = Color(0xFF9AF04D)
                                    )
                                    Text(
                                        text = marker.label,
                                        color = Color.White,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                IconButton(
                                    onClick = { selectedMarker = null },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Close",
                                        tint = Color.White
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(10.dp))
                            Text(
                                text = "DISTANCE: ${marker.distance} AWAY",
                                color = Color(0xFF9AF04D),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = marker.description,
                                color = Color.LightGray,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            }

            // Close AR HUD button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(top = 50.dp, start = 20.dp)
                    .align(Alignment.TopStart)
                    .size(40.dp)
                    .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.4f), CircleShape)
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Exit AR",
                    tint = Color.White
                )
            }
        }
    }
}
