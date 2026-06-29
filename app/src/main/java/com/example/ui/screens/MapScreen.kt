package com.example.ui.screens

import android.os.Bundle
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import com.google.maps.android.compose.*
import com.example.ui.components.HeritageQuizDialog
import com.google.android.gms.maps.model.CameraPosition
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.res.painterResource
import coil.compose.AsyncImage
import com.example.R
import com.example.ui.components.GlassCard
import com.example.viewmodel.MythicViewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

data class MapHeritageSite(
    val name: String,
    val province: String,
    val description: String,
    val era: String,
    val imageUrl: String,
    val latLng: LatLng,
    val isUnesco: Boolean = true
)

val heritageSites = listOf(
    MapHeritageSite(
        name = "Sigiriya Rock Fortress",
        province = "Central Province",
        description = "An ancient rock fortress built by King Kashyapa in the 5th century AD. Rising 200m high, it is famed for its mirror wall, frescoes, and monumental Lion's Gate.",
        era = "5th Century AD",
        imageUrl = "https://images.unsplash.com/photo-1588598126781-db26040a4cfc?w=600",
        latLng = LatLng(7.9570, 80.7603)
    ),
    MapHeritageSite(
        name = "Temple of the Sacred Tooth Relic",
        province = "Central Province",
        description = "Located in the royal palace complex of Kandy, this sacred temple houses Sri Lanka's most prized relic—the tooth of Gautama Buddha.",
        era = "16th Century AD",
        imageUrl = "https://images.unsplash.com/photo-1586861335167-e5223aadc9fe?w=600",
        latLng = LatLng(7.2936, 80.6413)
    ),
    MapHeritageSite(
        name = "Galle Fort",
        province = "Southern Province",
        description = "A star-shaped coastal citadel built by the Portuguese in 1588 and extensively fortified by the Dutch, showing a beautiful blend of European and South Asian styles.",
        era = "16th Century AD",
        imageUrl = "https://images.unsplash.com/photo-1546708973-b339540b5162?w=600",
        latLng = LatLng(6.0271, 80.2170)
    ),
    MapHeritageSite(
        name = "Dambulla Cave Temple",
        province = "Central Province",
        description = "A spectacular cave temple complex containing 153 Buddha statues, ancient murals, and sacred shrines inside 5 majestic rock caves.",
        era = "1st Century BC",
        imageUrl = "https://images.unsplash.com/photo-1608958416744-8846c071d2b0?w=600",
        latLng = LatLng(7.8564, 80.6485)
    ),
    MapHeritageSite(
        name = "Anuradhapura Sacred City",
        province = "North Central Province",
        description = "One of the ancient capitals of Sri Lanka, renowned for its beautifully preserved ruins of colossal stupas and the sacred Jaya Sri Maha Bodhi tree.",
        era = "4th Century BC",
        imageUrl = "https://images.unsplash.com/photo-1600100397608-f010e9df0782?w=600",
        latLng = LatLng(8.3542, 80.3967)
    ),
    MapHeritageSite(
        name = "Polonnaruwa Ancient City",
        province = "North Central Province",
        description = "Sri Lanka's second ancient kingdom, famous for its magnificent stone-carved Buddha statues at Gal Vihara and the monumental royal palace ruins.",
        era = "11th Century AD",
        imageUrl = "https://images.unsplash.com/photo-1625126392582-4299b9cfcb0c?w=600",
        latLng = LatLng(7.9403, 81.0029)
    ),
    MapHeritageSite(
        name = "Pidurangala Rock",
        province = "Central Province",
        description = "A massive rock situated adjacent to Sigiriya. Used as an ancient Buddhist monastery cave complex, it offers a legendary 360-degree sunrise view over Sigiriya.",
        era = "5th Century AD",
        imageUrl = "https://images.unsplash.com/photo-1578593139811-2921a7d904f0?w=600",
        latLng = LatLng(7.9624, 80.7628),
        isUnesco = false
    ),
    MapHeritageSite(
        name = "Nine Arch Bridge (Ella)",
        province = "Uva Province",
        description = "A masterpiece of early 20th-century colonial engineering built entirely of stone, brick, and cement without any steel, situated amid lush green tea hills.",
        era = "1921 AD",
        imageUrl = "https://images.unsplash.com/photo-1543872084-c7bd3822856f?w=600",
        latLng = LatLng(6.8768, 81.0494),
        isUnesco = false
    )
)

@Composable
fun MapScreen(
    viewModel: MythicViewModel,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    var selectedSite by remember { mutableStateOf<MapHeritageSite?>(null) }
    var selectedFilter by remember { mutableStateOf("All") }
    
    val filters = listOf("All", "UNESCO", "Temples", "Forts", "Ancient Cities")
    
    val filteredSites = remember(selectedFilter) {
        when (selectedFilter) {
            "All" -> heritageSites
            "UNESCO" -> heritageSites.filter { it.isUnesco }
            "Temples" -> heritageSites.filter { it.name.contains("Temple", ignoreCase = true) }
            "Forts" -> heritageSites.filter { it.name.contains("Fort", ignoreCase = true) }
            "Ancient Cities" -> heritageSites.filter { it.name.contains("City", ignoreCase = true) || it.name.contains("Fortress", ignoreCase = true) }
            else -> heritageSites
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(7.8731, 80.7718), 7.5f)
    }
    
    val currentQuiz by viewModel.currentQuiz.collectAsState()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsState()

    if (currentQuiz != null) {
        HeritageQuizDialog(
            quiz = currentQuiz!!,
            onDismiss = { viewModel.clearQuiz() }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // --- 1. Google Map View (Compose) ---
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = MapUiSettings(
                zoomControlsEnabled = false,
                myLocationButtonEnabled = true
            ),
            properties = MapProperties(
                isMyLocationEnabled = true,
                mapType = MapType.NORMAL
            ),
            onMapClick = { selectedSite = null }
        ) {
            filteredSites.forEach { site ->
                Marker(
                    state = MarkerState(position = site.latLng),
                    title = site.name,
                    snippet = site.province,
                    onClick = {
                        selectedSite = site
                        true
                    }
                )
            }
        }

        // --- 2. Custom Header & Filters Overlay ---
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Back Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        .clickable { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }

                // Title
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color.Black.copy(alpha = 0.7f))
                        .border(1.dp, Color.White.copy(alpha = 0.2f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Mythic Heritage Map",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        fontSize = 14.sp
                    )
                }

                Box(modifier = Modifier.size(44.dp))
            }

            // Category Filters
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(filters) { filter ->
                    val isSelected = filter == selectedFilter
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) Color(0xFF9AF04D) else Color.Black.copy(alpha = 0.7f))
                            .clickable { selectedFilter = filter }
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = filter,
                            color = if (isSelected) Color.Black else Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- 3. Interactive Detail Glass Panel ---
        AnimatedVisibility(
            visible = selectedSite != null,
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .navigationBarsPadding()
                .padding(16.dp)
        ) {
            val site = selectedSite
            if (site != null) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Thumbnail Image
                            AsyncImage(
                                model = site.imageUrl,
                                contentDescription = site.name,
                                placeholder = painterResource(R.drawable.app_logo_custom),
                                error = painterResource(R.drawable.app_logo_custom),
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(80.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            )

                            // Main Text Block
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    if (site.isUnesco) {
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF9AF04D).copy(alpha = 0.2f), RoundedCornerShape(4.dp))
                                                .border(0.5.dp, Color(0xFF9AF04D), RoundedCornerShape(4.dp))
                                                .padding(horizontal = 4.dp, vertical = 1.dp)
                                        ) {
                                            Text(
                                                text = "UNESCO",
                                                color = Color(0xFF9AF04D),
                                                fontSize = 8.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                    Text(
                                        text = site.era,
                                        color = Color(0xFF9AF04D),
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                
                                Text(
                                    text = site.name,
                                    color = Color.White,
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.ExtraBold
                                )

                                Text(
                                    text = "📍 ${site.province}",
                                    color = Color.LightGray,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        // Short details text
                        Text(
                            text = site.description,
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodyMedium,
                            fontSize = 13.sp
                        )

                        Divider(color = Color.White.copy(alpha = 0.1f))

                        // Action Buttons Row
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            // Close/Dismiss Button
                            TextButton(
                                onClick = { selectedSite = null },
                                modifier = Modifier.weight(0.4f)
                            ) {
                                Text("Close", color = Color.Gray, fontWeight = FontWeight.Bold)
                            }

                            // Start Virtual Navigation/Guide
                            Button(
                                onClick = {
                                    // Trigger camera zooming or navigate internally
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9AF04D),
                                    contentColor = Color.Black
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.6f)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Navigation,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Text("Explore Site", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                }
                            }

                            // Quiz Button
                            Button(
                                onClick = { viewModel.generateQuiz(site.name) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF9AF04D).copy(alpha = 0.2f),
                                    contentColor = Color(0xFF9AF04D)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(0.5f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9AF04D)),
                                enabled = !isGeneratingQuiz
                            ) {
                                if (isGeneratingQuiz) {
                                    CircularProgressIndicator(modifier = Modifier.size(16.dp), color = Color(0xFF9AF04D), strokeWidth = 2.dp)
                                } else {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.School,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Text("Quiz", fontWeight = FontWeight.Black, fontSize = 12.sp)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
