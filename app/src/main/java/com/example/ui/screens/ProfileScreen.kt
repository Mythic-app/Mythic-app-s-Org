package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.viewmodel.MythicViewModel

@Composable
fun ProfileScreen(
    viewModel: MythicViewModel,
    onLogoutSuccess: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val savedSites by viewModel.savedSites.collectAsState()
    val scanHistory by viewModel.scanHistory.collectAsState()

    val currentTheme = MaterialTheme.colorScheme
    val isDark = currentTheme.background == Color(0xFF000000)

    val textPrimary = currentTheme.onBackground
    val textSecondary = currentTheme.onSurfaceVariant
    val accentColor = currentTheme.primary

    var activeTab by remember { mutableStateOf("Saves") } // "Saves", "History"
    var showSettingsDialog by remember { mutableStateOf(false) }

    val p = profile
    if (p == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                AsyncImage(
                    model = "https://res.cloudinary.com/dlr63wcmt/image/upload/v1782377619/Main_lmc9y6.png",
                    contentDescription = "MYTHIC Logo",
                    modifier = Modifier.size(100.dp).padding(bottom = 16.dp),
                    contentScale = ContentScale.Fit
                )
                CircularProgressIndicator(color = accentColor)
            }
        }
    } else {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(currentTheme.background)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = 120.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                // Large visual hero avatar card profile info
                item {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(if (isDark) Color(0xFF0D0D0D) else Color(0xFFF9F9F9))
                            .padding(top = 40.dp, bottom = 32.dp, start = 20.dp, end = 20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Profile Avatar
                        Box(
                            modifier = Modifier
                                .size(110.dp)
                                .clip(CircleShape)
                                .border(3.dp, accentColor, CircleShape)
                                .background(Color.Gray.copy(alpha = 0.2f))
                                .clickable { viewModel.randomizeAvatar() }
                        ) {
                            AsyncImage(
                                model = p.avatarUrl,
                                contentDescription = p.fullName,
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Text(
                            text = "🎲 Tap Avatar to Randomize!",
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = accentColor,
                                fontWeight = FontWeight.Bold
                            ),
                            modifier = Modifier.clickable { viewModel.randomizeAvatar() }
                        )

                        Spacer(modifier = Modifier.height(12.dp))

                        // Username and emails
                        Text(
                            text = p.fullName,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = textPrimary
                            )
                        )
                        Text(
                            text = "@${p.username} • ${p.email}",
                            style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
                        )

                        Spacer(modifier = Modifier.height(24.dp))

                        // Quick mini achievements stats columns row
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(20.dp))
                                .background(if (isDark) Color(0xFF151515) else Color(0xFFEEEEEE))
                                .padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceAround
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "LEVEL",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSecondary
                                )
                                Text(
                                    text = "${p.level}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = accentColor
                                )
                            }
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "TOTAL XP",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = textSecondary
                                )
                                Text(
                                    text = "${p.xp}",
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Black,
                                    color = textPrimary
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Row with Settings & About, and Logout Button
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Settings & About Button
                            Button(
                                onClick = { showSettingsDialog = true },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isDark) Color(0xFF222222) else Color(0xFFDDDDDD),
                                    contentColor = textPrimary
                                ),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier
                                    .weight(1.2f)
                                    .testTag("settings_button")
                            ) {
                                Text("Settings & About", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }

                            // Logout Button
                            Button(
                                onClick = { viewModel.logout(onLogoutSuccess) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color.Red.copy(alpha = 0.12f),
                                    contentColor = Color.Red
                                ),
                                shape = RoundedCornerShape(100.dp),
                                modifier = Modifier
                                    .weight(0.8f)
                                    .testTag("logout_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Logout,
                                    contentDescription = "Logout",
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Logout", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Social Links Section
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "FOLLOW US",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                color = textSecondary,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            // Instagram (Simulated)
                            IconButton(onClick = { /* Open Instagram */ }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Share,
                                    contentDescription = "Instagram",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                            // Facebook (Simulated)
                            IconButton(onClick = { /* Open Facebook */ }, modifier = Modifier.size(32.dp)) {
                                Icon(
                                    imageVector = Icons.Default.Public,
                                    contentDescription = "Facebook",
                                    tint = accentColor,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }

                // Sub tabs selecting saved vs scanned landmarks
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp)
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isDark) Color(0xFF111111) else Color(0xFFEEEEEE))
                    ) {
                        val subTabs = listOf("Saves", "History")
                        subTabs.forEach { tab ->
                            val isSelected = activeTab == tab
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isSelected) accentColor else Color.Transparent)
                                    .clickable { activeTab = tab }
                                    .padding(vertical = 10.dp)
                                    .testTag("profile_tab_${tab.lowercase()}"),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (tab == "Saves") "Saved Sites (${savedSites.size})" else "Scan History (${scanHistory.size})",
                                    color = if (isSelected) Color.Black else textPrimary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                // Grid list profiles items cards
                if (activeTab == "Saves") {
                    if (savedSites.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No saved sites yet! Add saves in Feed.", color = textSecondary)
                            }
                        }
                    } else {
                        items(savedSites) { site ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .border(
                                        1.dp,
                                        if (isDark) Color(0x22FFFFFF) else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF111111) else Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(20.dp)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    AsyncImage(
                                        model = site.imageUrl,
                                        contentDescription = site.siteName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier
                                            .size(60.dp)
                                            .clip(RoundedCornerShape(12.dp))
                                            .background(Color.DarkGray)
                                    )
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = site.siteName,
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = site.province,
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                    }
                                    IconButton(
                                        onClick = {
                                            viewModel.toggleSaveSite(site.siteName, site.province, site.imageUrl)
                                        }
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Remove Save",
                                            tint = Color.Red.copy(alpha = 0.8f)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    if (scanHistory.isEmpty()) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(40.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text("No scans recorded! Go to the Camera.", color = textSecondary)
                            }
                        }
                    } else {
                        items(scanHistory) { scan ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 20.dp)
                                    .border(
                                        1.dp,
                                        if (isDark) Color(0x22FFFFFF) else Color.Transparent,
                                        RoundedCornerShape(20.dp)
                                    ),
                                colors = CardDefaults.cardColors(
                                    containerColor = if (isDark) Color(0xFF111111) else Color(0xFFF5F5F5)
                                ),
                                shape = RoundedCornerShape(20.dp)
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
                                            fontWeight = FontWeight.Bold,
                                            color = textPrimary
                                        )
                                        Text(
                                            text = "Scanned in ${scan.province}",
                                            fontSize = 12.sp,
                                            color = textSecondary
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = "+${scan.xpEarned} XP • ${scan.era}",
                                            color = accentColor,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Settings & About Dialog with CodeRiders Branding and Theme Switcher
    if (showSettingsDialog) {
        AlertDialog(
            onDismissRequest = { showSettingsDialog = false },
            title = {
                Text("Settings & About MYTHIC", color = textPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Theme Switcher Row
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Dark Theme", color = textPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Toggle neon dark vs organic light", color = textSecondary, fontSize = 12.sp)
                        }
                        Switch(
                            checked = isDark,
                            onCheckedChange = { checked ->
                                viewModel.toggleTheme(checked)
                            },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = accentColor,
                                checkedTrackColor = accentColor.copy(alpha = 0.3f)
                            )
                        )
                    }

                    Divider(color = textSecondary.copy(alpha = 0.2f))

                    // About section
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        AsyncImage(
                            model = "https://res.cloudinary.com/dlr63wcmt/image/upload/v1782377619/Main_lmc9y6.png",
                            contentDescription = "MYTHIC Logo",
                            modifier = Modifier.size(80.dp).padding(bottom = 8.dp),
                            contentScale = ContentScale.Fit
                        )
                        Text("About MYTHIC", color = textPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            text = "MYTHIC is an advanced, online-first Sri Lankan heritage explorer. Our mission is to digitize, preserve, and celebrate ancient landmarks across Sri Lanka using AI-powered camera scanning and immersive companion technology.",
                            color = textSecondary,
                            fontSize = 13.sp,
                            textAlign = TextAlign.Center
                        )
                    }

                    Divider(color = textSecondary.copy(alpha = 0.2f))

                    // Branding section
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Made by CodeRiders",
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp
                        )
                        Text(
                            text = "Version 1.0.0 • Production Ready",
                            color = textSecondary,
                            fontSize = 11.sp
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = { showSettingsDialog = false },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = accentColor,
                        contentColor = Color.Black
                    )
                ) {
                    Text("Close", fontWeight = FontWeight.Bold)
                }
            },
            containerColor = if (isDark) Color(0xFF111111) else Color(0xFFFFFFFF),
            modifier = Modifier.border(
                1.dp,
                if (isDark) Color.White.copy(alpha = 0.15f) else Color.Black.copy(alpha = 0.1f),
                RoundedCornerShape(28.dp)
            )
        )
    }
}
