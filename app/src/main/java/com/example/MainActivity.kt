package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import coil.compose.AsyncImage
import com.example.ui.screens.*
import com.example.ui.theme.MythicTheme
import com.example.viewmodel.MythicViewModel
import com.example.ui.components.AppBackground
import com.example.ui.components.GlassCard
import com.example.ui.components.GlassSurface
import com.example.ui.screens.AIScreen
import com.example.ui.screens.SettingsScreen
import com.example.R

enum class Screen {
    SPLASH, AUTH, MAIN, CHAT, AR_EXPLORE, SETTINGS, ADMIN
}

enum class Tab {
    HOME, FEED, AI, SCAN, QUESTS, PROFILE, REPORT
}

class MainActivity : ComponentActivity() {
    private val viewModel: MythicViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MythicTheme {
                AppBackground {
                    var currentScreen by remember { mutableStateOf(Screen.SPLASH) }
                var currentTab by remember { mutableStateOf(Tab.HOME) }

                val currentUser by viewModel.currentUser.collectAsState()
                val scanResult by viewModel.scanResult.collectAsState()
                val showScanResultDialog by viewModel.showScanResultDialog.collectAsState()

                val currentTheme = MaterialTheme.colorScheme
                val isDark = currentTheme.background == Color(0xFF000000)

                // Navigation router
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = Color.Transparent,
                    bottomBar = {
                        if (currentScreen == Screen.MAIN) {
                            BottomNavBar(
                                currentTab = currentTab,
                                onTabSelected = { tab -> currentTab = tab },
                                isDark = isDark,
                                accentColor = currentTheme.primary,
                                textPrimary = currentTheme.onBackground
                            )
                        }
                    },
                    floatingActionButton = {
                        // Floating LUMO AI Companion trigger button
                        if (currentScreen == Screen.MAIN && currentTab != Tab.SCAN) {
                            FloatingLumoButton(
                                onClick = { currentScreen = Screen.CHAT },
                                accentColor = currentTheme.primary
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                bottom = if (currentScreen == Screen.MAIN) innerPadding.calculateBottomPadding() else 0.dp,
                                top = if (currentScreen == Screen.SPLASH) 0.dp else innerPadding.calculateTopPadding()
                            )
                    ) {
                        when (currentScreen) {
                            Screen.SPLASH -> {
                                SplashScreen(
                                    isDark = isDark,
                                    onGetStarted = {
                                        currentScreen = if (currentUser != null) Screen.MAIN else Screen.AUTH
                                    },
                                    onAlreadyHaveAccount = {
                                        currentScreen = Screen.AUTH
                                    }
                                )
                            }
                            Screen.AUTH -> {
                                AuthScreen(
                                    viewModel = viewModel,
                                    onAuthSuccess = { currentScreen = Screen.MAIN }
                                )
                            }
                            Screen.MAIN -> {
                                when (currentTab) {
                                    Tab.HOME -> HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToAR = { currentScreen = Screen.AR_EXPLORE },
                                        onNavigateToLumo = { currentScreen = Screen.CHAT }
                                    )
                                    Tab.FEED -> FeedScreen(viewModel = viewModel)
                                    Tab.AI -> AIScreen(viewModel = viewModel)
                                    Tab.SCAN -> ScanScreen(viewModel = viewModel)
                                    Tab.QUESTS -> QuestsScreen(viewModel = viewModel)
                                    Tab.PROFILE -> ProfileScreen(
                                        viewModel = viewModel,
                                        onLogoutSuccess = { currentScreen = Screen.AUTH }
                                    )
                                    Tab.REPORT -> ReportScreen(viewModel = viewModel)
                                }
                            }
                            Screen.CHAT -> {
                                ChatScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.MAIN }
                                )
                            }
                            Screen.AR_EXPLORE -> {
                                ARScreen(
                                    viewModel = viewModel,
                                    onDismiss = { currentScreen = Screen.MAIN }
                                )
                            }
                            Screen.SETTINGS -> {
                                SettingsScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.MAIN }
                                )
                            }
                            Screen.ADMIN -> {
                                AdminDashboardScreen(
                                    viewModel = viewModel,
                                    onBack = { currentScreen = Screen.MAIN }
                                )
                            }
                        }

                        // Scanning Result Popup Overlay Dialog
                        if (showScanResultDialog && scanResult != null) {
                            val scan = scanResult!!
                            Dialog(onDismissRequest = { viewModel.dismissScanResult() }) {
                                GlassCard(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        // Site Image
                                        AsyncImage(
                                            model = scan.imageUrl,
                                            contentDescription = scan.siteName,
                                            contentScale = ContentScale.Crop,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(150.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color.DarkGray)
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        // Badge Label
                                        Box(
                                            modifier = Modifier
                                                .background(Color(0xFF9AF04D).copy(alpha = 0.15f), RoundedCornerShape(100.dp))
                                                .border(1.dp, Color(0xFF9AF04D), RoundedCornerShape(100.dp))
                                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                        ) {
                                            Text(
                                                text = scan.unescoStatus.uppercase(),
                                                color = Color(0xFF9AF04D),
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Black
                                            )
                                        }

                                        Spacer(modifier = Modifier.height(8.dp))

                                        Text(
                                            text = scan.siteName,
                                            color = Color.White,
                                            style = MaterialTheme.typography.titleLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            textAlign = TextAlign.Center
                                        )

                                        Text(
                                            text = scan.province,
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Text(
                                            text = scan.description,
                                            color = Color.LightGray,
                                            style = MaterialTheme.typography.bodyMedium,
                                            textAlign = TextAlign.Center
                                        )

                                        Spacer(modifier = Modifier.height(12.dp))

                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text(text = "Era: ${scan.era}", color = Color.Gray, fontSize = 12.sp)
                                            Text(text = "Scanned successfully!", color = Color(0xFF9AF04D), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                        }

                                        Divider(color = Color.DarkGray, modifier = Modifier.padding(vertical = 12.dp))

                                        // XP Reward Label
                                        Text(
                                            text = "+${scan.xpEarned} XP Earned",
                                            color = Color(0xFF9AF04D),
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.Black
                                        )

                                        Spacer(modifier = Modifier.height(16.dp))

                                        Button(
                                            onClick = { viewModel.dismissScanResult() },
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(0xFF9AF04D),
                                                contentColor = Color.Black
                                            ),
                                            shape = RoundedCornerShape(16.dp),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(50.dp)
                                                .testTag("awesome_dismiss_button")
                                        ) {
                                            Text("Awesome!", fontWeight = FontWeight.Black)
                                        }
                                    }
                                }
                            }
                        }
                    }
                } // Scaffold close
            } // AppBackground close
        }
    }
}

// Splash & Get Started Screen
@Composable
fun SplashScreen(
    isDark: Boolean,
    onGetStarted: () -> Unit,
    onAlreadyHaveAccount: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // Hero background image
        Image(
            painter = painterResource(id = R.drawable.sigiriya_splash_1782327284271),
            contentDescription = "Sigiriya Hero",
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Gradient overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.5f),
                            Color.Black.copy(alpha = 0.95f)
                        )
                    )
                )
        )

        // Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .padding(bottom = 40.dp),
            verticalArrangement = Arrangement.Bottom,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "MYTHIC",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF9AF04D),
                    letterSpacing = 4.sp
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Explore. Learn. Preserve.",
                color = Color.LightGray,
                style = MaterialTheme.typography.bodyLarge.copy(letterSpacing = 2.sp)
            )

            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "Discover the timeless\nheritage of Sri Lanka",
                color = Color.White,
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = onGetStarted,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF9AF04D),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("get_started_button")
            ) {
                Text(
                    text = "Get Started",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "I already have an account",
                color = Color.Gray,
                modifier = Modifier
                    .clickable { onAlreadyHaveAccount() }
                    .padding(8.dp)
                    .testTag("already_have_account_link"),
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Made by CodeRiders",
                color = Color.White.copy(alpha = 0.4f),
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 1.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }
    }
}

// Bottom Navigation Bar matching mockup perfectly
@Composable
fun BottomNavBar(
    currentTab: Tab,
    onTabSelected: (Tab) -> Unit,
    isDark: Boolean,
    accentColor: Color,
    textPrimary: Color
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (isDark) Color(0xFF0A0A0A) else Color(0xFFF5F5F5))
    ) {
        Divider(
            color = if (isDark) Color.DarkGray.copy(alpha = 0.5f) else Color.LightGray.copy(alpha = 0.5f),
            thickness = 1.dp
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
        val navItem = @Composable { tab: Tab, icon: String, label: String ->
            val isSelected = currentTab == tab
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(2.dp), // Smaller spacing
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(4.dp) // Smaller padding
                    .testTag("nav_tab_${label.lowercase()}")
            ) {
                Text(
                    text = icon,
                    fontSize = 16.sp, // Smaller font
                    color = if (isSelected) accentColor else Color.Gray
                )
                Text(
                    text = label,
                    fontSize = 8.sp, // Smaller font
                    fontWeight = FontWeight.Bold,
                    color = if (isSelected) accentColor else Color.Gray
                )
            }
        }

        navItem(Tab.HOME, "🏠", "Home")
        navItem(Tab.FEED, "📰", "Feed")
        navItem(Tab.AI, "⚡", "AI")

        // Glowing Scan button centered and highlighted
        Box(
            modifier = Modifier
                .offset(y = (-12).dp) // Adjusted offset
                .clickable { onTabSelected(Tab.SCAN) }
                .testTag("nav_tab_scan"),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(50.dp) // Smaller size
                        .clip(CircleShape)
                        .background(accentColor)
                        .border(4.dp, if (isDark) Color.Black else Color.White, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "📷", fontSize = 20.sp, color = Color.Black)
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Scan",
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (currentTab == Tab.SCAN) accentColor else Color.Gray
                )
            }
        }

        navItem(Tab.QUESTS, "🎯", "Quests")
        navItem(Tab.PROFILE, "👤", "Profile")
        navItem(Tab.REPORT, "⚠️", "Report")
    }
}
}

// Floating LUMO Companion Chat trigger button
@Composable
fun FloatingLumoButton(
    onClick: () -> Unit,
    accentColor: Color
) {
    // Pulsing indicator animation
    val infiniteTransition = rememberInfiniteTransition()
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        )
    )

    Box(
        modifier = Modifier
            .padding(bottom = 80.dp, end = 4.dp)
            .size(56.dp)
            .clip(CircleShape)
            .background(Color(0xFF111111))
            .border(1.5.dp, Color(0xFF9AF04D).copy(alpha = 0.5f), CircleShape)
            .clickable { onClick() }
            .testTag("floating_lumo_button"),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(Color(0xFF9AF04D))
                .align(Alignment.TopEnd)
                .offset(x = (-4).dp, y = 4.dp)
        )
        Text(text = "⚡", fontSize = 24.sp)
    }
}
}
