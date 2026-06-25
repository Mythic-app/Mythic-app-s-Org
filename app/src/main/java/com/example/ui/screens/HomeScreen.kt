package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.ui.components.GlassSurface
import com.example.viewmodel.MythicViewModel

@Composable
fun HomeScreen(
    viewModel: MythicViewModel,
    onNavigateToAR: () -> Unit,
    onNavigateToLumo: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()

    val currentTheme = MaterialTheme.colorScheme
    val isDark = currentTheme.background == Color(0xFF000000)

    val surfaceColor = currentTheme.surface
    val accentColor = currentTheme.primary
    val textPrimary = currentTheme.onBackground
    val textSecondary = currentTheme.onSurfaceVariant

    val p = profile ?: return

    // Calculate level progression parameters
    // In our repository, total xp increases level every 600 xp.
    // Let's adapt this dynamically or match the aesthetic.
    val levelXp = p.xp % 600
    val totalXpNeeded = 600
    val progress = (levelXp.toFloat() / totalXpNeeded).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Header welcome bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        // User avatar
                        AsyncImage(
                            model = p.avatarUrl,
                            contentDescription = "Profile Avatar",
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .border(2.dp, accentColor, CircleShape)
                                .background(Color.DarkGray)
                        )
                        Column {
                            Text(
                                text = "Welcome back",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    color = textSecondary,
                                    letterSpacing = 1.sp
                                )
                            )
                            Text(
                                text = p.fullName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    color = textPrimary,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            )
                        }
                    }

                    // Floating Notification Button
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(if (isDark) Color(0xFF111111) else Color(0xFFE5E5E5))
                            .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.Transparent, CircleShape)
                            .clickable { onNavigateToLumo() },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(text = "⚡", fontSize = 18.sp)
                    }
                }
            }

            // Level and XP Section
            item {
                GlassSurface(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.Bottom
                        ) {
                            Column {
                                Box(
                                    modifier = Modifier
                                        .background(accentColor.copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(100.dp))
                                        .padding(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "LEVEL ${p.level}",
                                        color = accentColor,
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Black)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Heritage Guardian",
                                    style = MaterialTheme.typography.bodyLarge.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = textPrimary
                                    )
                                )
                            }
                            Text(
                                text = "$levelXp / $totalXpNeeded XP",
                                style = MaterialTheme.typography.labelLarge.copy(
                                    color = textPrimary,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Progress Bar
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(10.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isDark) Color(0xFF222222) else Color(0xFFDDDDDD))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(accentColor)
                            )
                        }
                    }
                }
            }

            // Stats Section Grid
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    val statModifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(18.dp))
                        .background(if (isDark) Color.White.copy(alpha = 0.06f) else Color.Black.copy(alpha = 0.04f))
                        .border(
                            width = 1.dp,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    Color.White.copy(alpha = 0.15f),
                                    Color.White.copy(alpha = 0.03f)
                                )
                            ),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .padding(12.dp)

                    // Scans
                    Column(
                        modifier = statModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "📸", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "SCANS", style = MaterialTheme.typography.labelSmall.copy(color = textSecondary, fontWeight = FontWeight.Bold))
                        Text(text = "${p.scansCount}", style = MaterialTheme.typography.titleMedium.copy(color = textPrimary, fontWeight = FontWeight.Bold))
                    }

                    // Badges
                    Column(
                        modifier = statModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🏆", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "BADGES", style = MaterialTheme.typography.labelSmall.copy(color = textSecondary, fontWeight = FontWeight.Bold))
                        Text(text = "${p.badgesCount}", style = MaterialTheme.typography.titleMedium.copy(color = textPrimary, fontWeight = FontWeight.Bold))
                    }

                    // Streak
                    Column(
                        modifier = statModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "🔥", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "STREAK", style = MaterialTheme.typography.labelSmall.copy(color = textSecondary, fontWeight = FontWeight.Bold))
                        Text(text = "${p.streak}d", style = MaterialTheme.typography.titleMedium.copy(color = textPrimary, fontWeight = FontWeight.Bold))
                    }

                    // XP / Rank
                    Column(
                        modifier = statModifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(text = "💎", fontSize = 20.sp)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(text = "RANK", style = MaterialTheme.typography.labelSmall.copy(color = textSecondary, fontWeight = FontWeight.Bold))
                        Text(text = "#4", style = MaterialTheme.typography.titleMedium.copy(color = textPrimary, fontWeight = FontWeight.Bold))
                    }
                }
            }

            // Featured Card Section (Slideshow of Heritages of Sri Lanka)
            item {
                Text(
                    text = "Daily Heritage Discoveries",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            item {
                val slideshowItems = remember {
                    listOf(
                        Triple("Sigiriya Rock Fortress", "https://images.unsplash.com/photo-1588598126781-db26040a4cfc?w=1000", "5th Century AD rock fortress built by King Kashyapa. Famous for its mirror wall and lion gate."),
                        Triple("Temple of the Tooth", "https://images.unsplash.com/photo-1586861335167-e5223aadc9fe?w=1000", "Houses Kandy's sacred tooth relic of Gautama Buddha within the royal complex."),
                        Triple("Galle Fort Citadel", "https://images.unsplash.com/photo-1546708973-b339540b5162?w=1000", "Star-shaped coastal fortress town blending European architecture and South Asian traditions."),
                        Triple("Dambulla Cave Temple", "https://images.unsplash.com/photo-1608958416744-8846c071d2b0?w=1000", "Largest cave temple complex in Sri Lanka, boasting 150+ stunning Buddha statues."),
                        Triple("Sacred Anuradhapura", "https://images.unsplash.com/photo-1600100397608-f010e9df0782?w=1000", "Ancient capital featuring the giant Ruwanwelisaya stupa and the sacred Bodhi tree.")
                    )
                }
                var currentIndex by remember { mutableStateOf(0) }

                LaunchedEffect(Unit) {
                    while (true) {
                        delay(4000)
                        currentIndex = (currentIndex + 1) % slideshowItems.size
                    }
                }

                val activeItem = slideshowItems[currentIndex]

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(345.dp)
                        .clip(RoundedCornerShape(32.dp))
                        .background(Color.DarkGray)
                        .border(1.dp, if (isDark) Color(0x33FFFFFF) else Color.Transparent, RoundedCornerShape(32.dp))
                ) {
                    // Current Active Site Image
                    AsyncImage(
                        model = activeItem.second,
                        contentDescription = activeItem.first,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Gradient overlay for readability and liquid glass aesthetic
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.4f),
                                        Color.Black.copy(alpha = 0.95f)
                                    )
                                )
                            )
                    )

                    // Card Content
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        verticalArrangement = Arrangement.Bottom
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .background(Color(0xFF9AF04D), RoundedCornerShape(4.dp))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "UNESCO SITE",
                                        color = Color.Black,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                }
                                Text(
                                    text = "📍 Sri Lanka",
                                    color = Color.White.copy(alpha = 0.8f),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }

                            // Dot indicators
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                slideshowItems.forEachIndexed { idx, _ ->
                                    Box(
                                        modifier = Modifier
                                            .size(if (idx == currentIndex) 8.dp else 6.dp)
                                            .clip(CircleShape)
                                            .background(if (idx == currentIndex) Color(0xFF9AF04D) else Color.White.copy(alpha = 0.4f))
                                            .clickable { currentIndex = idx }
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = activeItem.first,
                            color = Color.White,
                            style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold)
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = activeItem.third,
                            color = Color.LightGray,
                            style = MaterialTheme.typography.bodyMedium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Button Action
                        Button(
                            onClick = onNavigateToAR,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.White,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(16.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                        ) {
                            Text(
                                text = "Explore AR Discovery ✨",
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Black)
                                    .copy(letterSpacing = 0.5.sp)
                            )
                        }
                    }
                }
            }

            // Recent activity header
            item {
                Text(
                    text = "Recent Activity",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = textPrimary,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.padding(top = 10.dp)
                )
            }

            if (scanHistory.isEmpty()) {
                item {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = if (isDark) Color(0xFF111111) else Color(0xFFF5F5F5)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(24.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "No recent scans. Go to Scan tab to start!",
                                color = textSecondary,
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
            } else {
                items(scanHistory.take(2)) { scan ->
                    GlassSurface(
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            AsyncImage(
                                model = scan.imageUrl,
                                contentDescription = scan.siteName,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(Color.DarkGray)
                            )
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = scan.siteName,
                                    style = MaterialTheme.typography.bodyMedium.copy(
                                        color = textPrimary,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                                Text(
                                    text = scan.province,
                                    style = MaterialTheme.typography.labelSmall.copy(color = textSecondary)
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "+${scan.xpEarned} XP",
                                    color = accentColor,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                Text(
                                    text = "Scanned",
                                    style = MaterialTheme.typography.labelSmall.copy(color = textSecondary)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
