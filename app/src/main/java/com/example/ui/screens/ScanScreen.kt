package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.PhotoLibrary
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
import com.example.viewmodel.MythicViewModel

@Composable
fun ScanScreen(
    viewModel: MythicViewModel
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    val currentTheme = MaterialTheme.colorScheme
    val isDark = currentTheme.background == Color(0xFF000000)

    val textPrimary = currentTheme.onBackground
    val accentColor = currentTheme.primary

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

    // Available mock targets to select for a high-fidelity scan simulation
    val mockTargets = listOf(
        Pair("Sigiriya Rock Fortress", "https://images.unsplash.com/photo-1588598126781-db26040a4cfc?w=600"),
        Pair("Temple of the Tooth", "https://images.unsplash.com/photo-1586861335167-e5223aadc9fe?w=600"),
        Pair("Dambulla Cave Temple", "https://images.unsplash.com/photo-1608958416744-8846c071d2b0?w=600"),
        Pair("Galle Fort", "https://images.unsplash.com/photo-1546708973-b339540b5162?w=600"),
        Pair("Anuradhapura", "https://images.unsplash.com/photo-1600100397608-f010e9df0782?w=600"),
        Pair("Polonnaruwa Ancient City", "https://images.unsplash.com/photo-1578593139888-39620e59c164?w=600")
    )

    var selectedMockTarget by remember { mutableStateOf(mockTargets[0]) }
    var isFlashOn by remember { mutableStateOf(false) }

    val isScanningWithVision by viewModel.isScanningWithVision.collectAsState()
    val visionScanError by viewModel.visionScanError.collectAsState()

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri ->
            if (uri != null) {
                try {
                    val inputStream = context.contentResolver.openInputStream(uri)
                    val bytes = inputStream?.readBytes()
                    if (bytes != null) {
                        val base64 = android.util.Base64.encodeToString(bytes, android.util.Base64.NO_WRAP)
                        val mimeType = context.contentResolver.getType(uri) ?: "image/jpeg"
                        viewModel.scanWithGeminiVision(
                            base64Image = base64,
                            mimeType = mimeType,
                            defaultSiteName = "Scanned Heritage Site",
                            fallbackImageUrl = uri.toString()
                        )
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (hasCameraPermission) {
            // CameraX preview implementation
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
        } else {
            // Permission request placeholder
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Camera Permission Required",
                    color = Color.White,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "MYTHIC requires camera access to scan historical landmarks and heritage structures.",
                    color = Color.LightGray,
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9AF04D), contentColor = Color.Black)
                ) {
                    Text("Grant Camera Access", fontWeight = FontWeight.Bold)
                }
            }
        }

        // Green overlay scanning frame targeting box
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 120.dp),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(280.dp)
                    .border(3.dp, Color(0xFF9AF04D), RoundedCornerShape(24.dp))
            ) {
                // Framing corners simulation
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(12.dp)
                ) {
                    // Central scan line aiming crosshair
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(Color(0xFF9AF04D))
                            .align(Alignment.Center)
                    )
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(1.dp)
                            .background(Color(0xFF9AF04D))
                            .align(Alignment.Center)
                    )
                }
            }

            // Target Text helper
            Text(
                text = "Point camera at Sri Lankan monument to scan",
                color = Color.White,
                fontSize = 13.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 100.dp)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }

        // Target site simulation row selector
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp)
        ) {
            Text(
                text = "Simulate Scanning Site:",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 20.dp, bottom = 6.dp)
            )
            LazyRow(
                contentPadding = PaddingValues(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(mockTargets) { target ->
                    val isSelected = target.first == selectedMockTarget.first
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) Color(0xFF9AF04D) else Color.Black.copy(alpha = 0.7f))
                            .border(1.dp, Color(0xFF9AF04D).copy(alpha = 0.5f), RoundedCornerShape(100.dp))
                            .clickable { selectedMockTarget = target }
                            .padding(horizontal = 14.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = target.first,
                            color = if (isSelected) Color.Black else Color.White,
                            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold)
                        )
                    }
                }
            }
        }

        // Camera control action bar (Flash, Capture, Gallery)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(Color.Black.copy(alpha = 0.85f))
                .padding(bottom = 40.dp, top = 20.dp, start = 30.dp, end = 30.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Flash Toggle
            IconButton(
                onClick = { isFlashOn = !isFlashOn },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
            ) {
                Icon(
                    imageVector = if (isFlashOn) Icons.Default.FlashOn else Icons.Default.FlashOff,
                    contentDescription = "Flash",
                    tint = Color.White
                )
            }

            // Capture Trigger Button
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF9AF04D))
                    .border(5.dp, Color.Black, CircleShape)
                    .clickable {
                        viewModel.triggerScan(selectedMockTarget.first, selectedMockTarget.second)
                    }
                    .testTag("scan_trigger_button"),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .border(3.dp, Color.Black, CircleShape)
                )
            }

            // Gallery Import (Real-time Gemini Vision Scan)
            IconButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .size(48.dp)
                    .background(Color.DarkGray.copy(alpha = 0.5f), CircleShape)
                    .testTag("gallery_import_button")
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoLibrary,
                    contentDescription = "Gallery",
                    tint = Color.White
                )
            }
        }

        // --- GEMINI VISION SCAN LOADING OVERLAY ---
        if (isScanningWithVision) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(32.dp)
                ) {
                    CircularProgressIndicator(
                        color = Color(0xFF9AF04D),
                        strokeWidth = 4.dp,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                    Text(
                        text = "Gemini AI Vision Scanning...",
                        color = Color.White,
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Analyzing architectural styles, stone carvings, and historical motifs in real-time.",
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        // --- VISION SCAN ERROR NOTIFICATION ---
        if (visionScanError != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(top = 140.dp, start = 20.dp, end = 20.dp)
                    .background(Color.Red.copy(alpha = 0.9f), RoundedCornerShape(16.dp))
                    .border(1.dp, Color.Red, RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = visionScanError ?: "",
                    color = Color.White,
                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
