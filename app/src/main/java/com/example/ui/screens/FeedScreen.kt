package com.example.ui.screens

import android.net.Uri
import com.example.ui.components.VideoPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.*
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.pager.VerticalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import coil.compose.AsyncImage
import com.example.data.remote.HeritageArticle
import com.example.data.remote.HeritageQuiz
import com.example.ui.components.HeritageQuizDialog
import com.example.viewmodel.MythicViewModel
import com.example.viewmodel.HeritageReel
import com.example.viewmodel.ReelComment
import com.example.ui.components.GlassCard
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: MythicViewModel
) {
    var activeTabMode by remember { mutableStateOf("Reels") } // "Reels", "Articles", "Community"
    
    val savedSites by viewModel.savedSites.collectAsState()
    val activeFeedTab by viewModel.activeFeedTab.collectAsState()
    val articles by viewModel.feedArticles.collectAsState()
    
    val currentQuiz by viewModel.currentQuiz.collectAsState()
    val isGeneratingQuiz by viewModel.isGeneratingQuiz.collectAsState()

    if (currentQuiz != null) {
        HeritageQuizDialog(
            quiz = currentQuiz!!,
            onDismiss = { viewModel.clearQuiz() }
        )
    }

    val currentTheme = MaterialTheme.colorScheme
    val isDark = currentTheme.background == Color(0xFF000000)

    val textPrimary = currentTheme.onBackground
    val textSecondary = currentTheme.onSurfaceVariant
    val accentColor = currentTheme.primary

    val filteredArticles = remember(activeFeedTab, articles) {
        if (activeFeedTab == "All") {
            articles
        } else {
            articles.filter { it.category == activeFeedTab }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(if (activeTabMode == "Reels") Color.Black else currentTheme.background)
    ) {
        when (activeTabMode) {
            "Reels" -> {
                // Immersive TikTok Video Feed
                ReelsView(viewModel = viewModel)
            }
            "Articles" -> {
                // Standard Article Feed
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                ) {
                    Spacer(modifier = Modifier.height(60.dp)) // space for toggle

                    // Screen Title
                    Text(
                        text = "Heritage Feed",
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontWeight = FontWeight.Black,
                            color = textPrimary
                        ),
                        modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 12.dp, bottom = 4.dp)
                    )

                    // Real-Time Active Heritage Explorer Search (AI Getter)
                    var searchQuery by remember { mutableStateOf("") }
                    val isGeneratingArticle by viewModel.isGeneratingArticle.collectAsState()
                    val generationError by viewModel.generationError.collectAsState()

                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isDark) Color(0xFF111111) else Color(0xFFF5F5F5)
                    ),
                    shape = RoundedCornerShape(24.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "AI REAL-TIME EXPLORER",
                            color = accentColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "Type any Sri Lankan monument or historical place to actively fetch facts and real-time photos.",
                            color = textSecondary,
                            fontSize = 12.sp,
                            style = MaterialTheme.typography.bodySmall
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("e.g. Ritigala, Mihintale, Yapahuwa...", fontSize = 14.sp) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(52.dp)
                                    .testTag("ai_search_input"),
                                shape = RoundedCornerShape(12.dp),
                                singleLine = true,
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = accentColor,
                                    unfocusedBorderColor = Color.Gray.copy(alpha = 0.3f),
                                    focusedLabelColor = accentColor,
                                    focusedTextColor = textPrimary,
                                    unfocusedTextColor = textPrimary
                                )
                            )
                            
                            if (isGeneratingArticle) {
                                Box(
                                    modifier = Modifier
                                        .size(52.dp)
                                        .background(accentColor.copy(alpha = 0.1f), RoundedCornerShape(12.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator(color = accentColor, strokeWidth = 3.dp, modifier = Modifier.size(24.dp))
                                }
                            } else {
                                Button(
                                    onClick = {
                                        if (searchQuery.isNotBlank()) {
                                            viewModel.generateRealtimeArticle(searchQuery)
                                            searchQuery = ""
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = accentColor, contentColor = Color.Black),
                                    modifier = Modifier
                                        .height(52.dp)
                                        .testTag("ai_generate_button"),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Explore", fontWeight = FontWeight.Black)
                                }
                            }
                        }
                        if (generationError != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(text = generationError!!, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }

                // Category Chips Selection Row
                val tabs = listOf("All", "Architecture", "Ancient Cities", "Religious")
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 20.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp)
                ) {
                    items(tabs) { tab ->
                        val isSelected = tab == activeFeedTab
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(100.dp))
                                .background(if (isSelected) accentColor else (if (isDark) Color(0xFF111111) else Color(0xFFEEEEEE)))
                                .border(
                                    1.dp,
                                    if (isSelected) Color.Transparent else (if (isDark) Color(0x33FFFFFF) else Color.Transparent),
                                    RoundedCornerShape(100.dp)
                                )
                                .clickable { viewModel.setFeedTab(tab) }
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(
                                text = tab,
                                color = if (isSelected) Color.Black else textPrimary,
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }

                // Article Cards
                LazyColumn(
                    contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 120.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(filteredArticles) { article ->
                        var isLiked by remember { mutableStateOf(false) }
                        var likeCount by remember { mutableStateOf(article.initialLikes) }
                        val isSaved = savedSites.any { it.siteName == article.siteName }

                        var isExpanded by remember { mutableStateOf(false) }

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .border(
                                    1.dp,
                                    if (isDark) Color(0x22FFFFFF) else Color.Transparent,
                                    RoundedCornerShape(24.dp)
                                )
                                .clickable {
                                    isExpanded = !isExpanded
                                    viewModel.triggerArticleRead()
                                },
                            colors = CardDefaults.cardColors(
                                containerColor = if (isDark) Color(0xFF111111) else Color(0xFFF5F5F5)
                            ),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column {
                                // Large Image with custom local resolver
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(200.dp)
                                        .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                                ) {
                                    val context = LocalContext.current
                                    val drawableId = remember(article.imageUrl) {
                                        if (article.imageUrl.startsWith("http")) {
                                            null
                                        } else {
                                            context.resources.getIdentifier(article.imageUrl, "drawable", context.packageName).let { id ->
                                                if (id != 0) id else null
                                            }
                                        }
                                    }

                                    AsyncImage(
                                        model = drawableId ?: article.imageUrl,
                                        contentDescription = article.siteName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.fillMaxSize()
                                    )

                                    // Category Label
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(12.dp)
                                            .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(8.dp))
                                            .padding(horizontal = 8.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = article.category.uppercase(),
                                            color = accentColor,
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }

                                // Texts
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text(
                                        text = article.province.uppercase(),
                                        style = MaterialTheme.typography.labelSmall.copy(
                                            color = accentColor,
                                            fontWeight = FontWeight.Bold
                                        )
                                    )
                                    Text(
                                        text = article.siteName,
                                        style = MaterialTheme.typography.titleMedium.copy(
                                            color = textPrimary,
                                            fontWeight = FontWeight.ExtraBold
                                        )
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = article.description,
                                        style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary),
                                        maxLines = if (isExpanded) 12 else 2,
                                        overflow = TextOverflow.Ellipsis
                                    )

                                    // Historical info expand block
                                    AnimatedVisibility(
                                        visible = isExpanded,
                                        enter = expandVertically(),
                                        exit = shrinkVertically()
                                    ) {
                                        Column(modifier = Modifier.padding(top = 12.dp)) {
                                            Divider(color = Color.Gray.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 8.dp))
                                            Text(
                                                text = "UNESCO STATUS: ${article.unescoStatus}",
                                                style = MaterialTheme.typography.labelLarge.copy(color = textPrimary, fontWeight = FontWeight.Bold)
                                            )
                                            Text(
                                                text = "ERA: ${article.era}",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
                                            )
                                            Spacer(modifier = Modifier.height(6.dp))
                                            Text(
                                                text = "Interesting Facts:",
                                                style = MaterialTheme.typography.bodyMedium.copy(color = textPrimary, fontWeight = FontWeight.Bold)
                                            )
                                            article.facts.split(";").forEach { fact ->
                                                if (fact.trim().isNotEmpty()) {
                                                    Text(
                                                        text = "• ${fact.trim()}",
                                                        style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
                                                    )
                                                }
                                            }
                                            
                                            Spacer(modifier = Modifier.height(16.dp))
                                            
                                            Button(
                                                onClick = { viewModel.generateQuiz(article.siteName) },
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = Color(0xFF9AF04D).copy(alpha = 0.2f),
                                                    contentColor = Color(0xFF9AF04D)
                                                ),
                                                shape = RoundedCornerShape(12.dp),
                                                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF9AF04D)),
                                                enabled = !isGeneratingQuiz
                                            ) {
                                                if (isGeneratingQuiz) {
                                                    CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color(0xFF9AF04D), strokeWidth = 2.dp)
                                                } else {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(Icons.Default.School, contentDescription = null, modifier = Modifier.size(18.dp))
                                                        Spacer(modifier = Modifier.width(8.dp))
                                                        Text("Take LUMO AI Quiz", fontWeight = FontWeight.Black)
                                                    }
                                                }
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(16.dp))

                                    // Social Actions
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                                            // Like
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.clickable {
                                                    isLiked = !isLiked
                                                    likeCount = if (isLiked) likeCount + 1 else likeCount - 1
                                                    viewModel.triggerArticleLike()
                                                }
                                            ) {
                                                Icon(
                                                    imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                    contentDescription = "Like",
                                                    tint = if (isLiked) Color.Red else textSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(text = "$likeCount", color = textSecondary, fontSize = 14.sp)
                                            }

                                            // Share (simulated)
                                            Row(
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                modifier = Modifier.clickable { /* Share triggers */ }
                                            ) {
                                                Icon(
                                                    imageVector = Icons.Default.Share,
                                                    contentDescription = "Share",
                                                    tint = textSecondary,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                                Text(text = "Share", color = textSecondary, fontSize = 14.sp)
                                            }
                                        }

                                        // Save
                                        IconButton(
                                            onClick = {
                                                viewModel.toggleSaveSite(article.siteName, article.province, article.imageUrl)
                                            },
                                            modifier = Modifier.size(24.dp)
                                        ) {
                                            Icon(
                                                imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                                                contentDescription = "Save",
                                                tint = if (isSaved) accentColor else textSecondary
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
        "Community" -> {
                CommunityView(viewModel = viewModel)
            }
        }

        // Overlaid Mode Switch (Articles vs TikTok Reels) at the top of FeedScreen
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
                .align(Alignment.TopCenter),
            horizontalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .background(
                        if (activeTabMode == "Reels") Color.Black.copy(alpha = 0.6f) else (if (isDark) Color(0xFF111111) else Color(0xFFEEEEEE)),
                        RoundedCornerShape(100.dp)
                    )
                    .border(1.dp, if (activeTabMode == "Reels") Color.White.copy(alpha = 0.2f) else Color.Transparent, RoundedCornerShape(100.dp))
                    .padding(4.dp)
            ) {
                Row {
                    val activeColor = accentColor
                    val inactiveColor = Color.Transparent
                    
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (activeTabMode == "Reels") activeColor else inactiveColor)
                            .clickable { activeTabMode = "Reels" }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🎬 Reels",
                            color = if (activeTabMode == "Reels") Color.Black else (if (activeTabMode == "Reels") Color.White else textPrimary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (activeTabMode == "Articles") activeColor else inactiveColor)
                            .clickable { activeTabMode = "Articles" }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "📰 Articles",
                            color = if (activeTabMode == "Articles") Color.Black else (if (activeTabMode == "Reels") Color.White else textPrimary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (activeTabMode == "Community") activeColor else inactiveColor)
                            .clickable { activeTabMode = "Community" }
                            .padding(horizontal = 20.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = "🏘️ Community",
                            color = if (activeTabMode == "Community") Color.Black else (if (activeTabMode == "Reels") Color.White else textPrimary),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityView(viewModel: MythicViewModel) {
    var postText by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 60.dp)
            .padding(16.dp)
    ) {
        Text(
            text = "Community Hub",
            style = MaterialTheme.typography.displayLarge.copy(
                fontWeight = FontWeight.Black,
                color = if (MaterialTheme.colorScheme.background == Color.Black) Color.White else Color.Black
            ),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // New Post Input
        OutlinedTextField(
            value = postText,
            onValueChange = { postText = it },
            placeholder = { Text("Share a heritage discovery...", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            trailingIcon = {
                IconButton(onClick = {
                    if (postText.isNotBlank()) {
                        postText = ""
                    }
                }) {
                    Icon(Icons.Default.Send, contentDescription = "Post", tint = Color(0xFF9AF04D))
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            item {
                Text(
                    text = "Trending Conversations",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Gray,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(5) { index ->
                CommunityPostItem(index)
            }
        }
    }
}

@Composable
fun CommunityPostItem(index: Int) {
    val isDark = MaterialTheme.colorScheme.background == Color.Black
    GlassCard(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Gray)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(text = "Heritage Explorer $index", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text(text = "${index + 1}h ago", color = Color.Gray, fontSize = 10.sp)
                }
                Spacer(modifier = Modifier.weight(1f))
                Icon(Icons.Default.MoreVert, contentDescription = null, tint = Color.Gray)
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Text(
                text = "Exploring the ancient ruins of Polonnaruwa today. The stone carvings are incredible! 🏛️ #SriLanka #Heritage",
                color = Color.LightGray,
                fontSize = 13.sp
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.FavoriteBorder, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${(10..50).random()}", color = Color.Gray, fontSize = 12.sp)
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.ChatBubbleOutline, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "${(1..10).random()}", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun ReelsView(viewModel: MythicViewModel) {
    val reels by viewModel.reels.collectAsState()
    val isGeneratingReel by viewModel.isGeneratingReel.collectAsState()
    val reelGenerationError by viewModel.reelGenerationError.collectAsState()
    
    val pagerState = rememberPagerState(pageCount = { reels.size })
    val coroutineScope = rememberCoroutineScope()
    
    var showCreateDialog by remember { mutableStateOf(false) }
    var userPrompt by remember { mutableStateOf("") }

    // Trigger pre-fetching of next reel for continuous scrolling
    LaunchedEffect(pagerState.currentPage) {
        if (reels.isNotEmpty() && pagerState.currentPage >= reels.size - 2) {
            viewModel.generateNextReel()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        if (reels.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
            }
        } else {
            VerticalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize()
            ) { page ->
                val reel = reels[page]
                ReelPageItem(
                    reel = reel,
                    viewModel = viewModel,
                    isActive = pagerState.currentPage == page
                )
            }
        }

        // --- Elegant "🤖 AI Upload" button at Top-Right overlay ---
        Column(
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(top = 90.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = { showCreateDialog = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.85f),
                    contentColor = Color.Black
                ),
                shape = RoundedCornerShape(100.dp),
                contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                modifier = Modifier.testTag("ai_reel_upload_trigger")
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🤖", fontSize = 16.sp)
                    Text(
                        text = "AI Upload",
                        fontWeight = FontWeight.Black,
                        fontSize = 12.sp,
                        letterSpacing = 0.5.sp
                    )
                }
            }
            
            if (isGeneratingReel) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.7f), RoundedCornerShape(100.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        CircularProgressIndicator(
                            color = MaterialTheme.colorScheme.primary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            text = "AI Uploading...",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // --- AI Reel Upload/Generator Dialog ---
        if (showCreateDialog) {
            AlertDialog(
                onDismissRequest = { if (!isGeneratingReel) showCreateDialog = false },
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("🤖", fontSize = 24.sp)
                        Text(
                            text = "AI Reel Generator",
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black,
                                color = Color.White
                            )
                        )
                    }
                },
                text = {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Describe any Sri Lankan site, landmark, or historical mystery. Our Gemini AI will draft the script, match visuals, and upload a playable video reel directly to the feed!",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                        
                        OutlinedTextField(
                            value = userPrompt,
                            onValueChange = { userPrompt = it },
                            placeholder = { Text("e.g. Polonnaruwa Vatadage, Ravana Caves...", color = Color.Gray, fontSize = 14.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("ai_reel_input_field"),
                            shape = RoundedCornerShape(12.dp),
                            singleLine = true,
                            enabled = !isGeneratingReel,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = MaterialTheme.colorScheme.primary,
                                unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                                focusedTextColor = Color.White,
                                unfocusedTextColor = Color.White
                            )
                        )

                        if (isGeneratingReel) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                LinearProgressIndicator(
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.fillMaxWidth()
                                )
                                Text(
                                    text = "Gemini is building your script & linking dynamic video stream...",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        if (reelGenerationError != null) {
                            Text(
                                text = reelGenerationError!!,
                                color = Color.Red,
                                fontSize = 12.sp
                            )
                        }
                    }
                },
                confirmButton = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Surprise Me button
                        TextButton(
                            onClick = {
                                viewModel.generateNewReel(null) {
                                    coroutineScope.launch {
                                        pagerState.animateScrollToPage(0)
                                    }
                                    showCreateDialog = false
                                    userPrompt = ""
                                }
                            },
                            enabled = !isGeneratingReel,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("🎲 Surprise Me", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                        }

                        // Generate Button
                        Button(
                            onClick = {
                                if (userPrompt.isNotBlank()) {
                                    viewModel.generateNewReel(userPrompt) {
                                        coroutineScope.launch {
                                            pagerState.animateScrollToPage(0)
                                        }
                                        showCreateDialog = false
                                        userPrompt = ""
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.Black
                            ),
                            shape = RoundedCornerShape(100.dp),
                            enabled = !isGeneratingReel && userPrompt.isNotBlank(),
                            modifier = Modifier.testTag("ai_reel_submit")
                        ) {
                            Text("Upload", fontWeight = FontWeight.Black)
                        }
                    }
                },
                dismissButton = {
                    if (!isGeneratingReel) {
                        TextButton(
                            onClick = { showCreateDialog = false }
                        ) {
                            Text("Cancel", color = Color.Gray)
                        }
                    }
                },
                containerColor = Color(0xFF181818),
                textContentColor = Color.White
            )
        }
    }
}

@Composable
fun ReelPageItem(
    reel: HeritageReel,
    viewModel: MythicViewModel,
    isActive: Boolean
) {
    val context = LocalContext.current
    var isVideoReady by remember { mutableStateOf(false) }
    var isMuted by remember { mutableStateOf(false) }
    
    var showComments by remember { mutableStateOf(false) }
    var showShareSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        // 1. Image Thumbnail (Placeholder until video is ready)
        if (!isVideoReady) {
            val drawableId = remember(reel.imageUrl) {
                if (reel.imageUrl.startsWith("http")) {
                    null
                } else {
                    context.resources.getIdentifier(reel.imageUrl, "drawable", context.packageName).let { id ->
                        if (id != 0) id else null
                    }
                }
            }

            AsyncImage(
                model = drawableId ?: reel.imageUrl,
                contentDescription = reel.siteName,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        }

        // 2. Advanced Video Player Overlay
        VideoPlayer(
            videoUrl = reel.videoUrl,
            modifier = Modifier
                .fillMaxSize()
                .clickable { isMuted = !isMuted },
            isActive = isActive,
            isMuted = isMuted,
            onVideoReady = { isVideoReady = true }
        )

        // Dark Gradient overlays for layout contrast
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color.Black.copy(alpha = 0.4f),
                            Color.Transparent,
                            Color.Black.copy(alpha = 0.8f)
                        )
                    )
                )
        )

        // Loading indicator
        if (isActive && !isVideoReady) {
            CircularProgressIndicator(
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(36.dp),
                strokeWidth = 3.dp
            )
        }

        // Dynamic Mute indicator badge
        var showMuteBadge by remember { mutableStateOf(false) }
        LaunchedEffect(isMuted) {
            showMuteBadge = true
            kotlinx.coroutines.delay(1000)
            showMuteBadge = false
        }
        if (showMuteBadge) {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(100.dp))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = if (isMuted) "🔇 Muted" else "🔊 Unmuted",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
            }
        }

        // 3. Bottom-Left Information & Captions Pane
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(start = 16.dp, bottom = 120.dp, end = 80.dp) // padded bottom for bottom navigation
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = reel.author,
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 15.sp
                )
                Box(
                    modifier = Modifier
                        .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(4.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "HERITAGE EXPERT",
                        color = Color.Black,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = "📍 ${reel.siteName} • ${reel.province}",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = reel.description,
                color = Color.White.copy(alpha = 0.9f),
                fontSize = 13.sp,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(modifier = Modifier.height(12.dp))

            // Spinning cassette/disc animation
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val infiniteTransition = rememberInfiniteTransition(label = "disc")
                val rotationAngle by infiniteTransition.animateFloat(
                    initialValue = 0f,
                    targetValue = 360f,
                    animationSpec = infiniteRepeatable(
                        animation = tween(4000, easing = LinearEasing),
                        repeatMode = RepeatMode.Restart
                    ),
                    label = "disc_rot"
                )
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(rotationAngle)
                        .background(Color.DarkGray, CircleShape)
                        .border(1.5.dp, MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🎵", fontSize = 10.sp)
                }
                Text(
                    text = "Heritage Ambient Sound - Original Mix",
                    color = Color.White.copy(alpha = 0.6f),
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.widthIn(max = 180.dp)
                )
            }
        }

        // 4. Interactive Right Side Floating Action Buttons Panel (TikTok style)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 12.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Profile uploader with a plus sign
            Box(
                modifier = Modifier.size(50.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                ) {
                    AsyncImage(
                        model = reel.authorAvatarUrl,
                        contentDescription = "Author Avatar",
                        modifier = Modifier.fillMaxSize()
                    )
                }
                Box(
                    modifier = Modifier
                        .offset(y = 4.dp)
                        .size(16.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "+", color = Color.Black, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Hearts Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                val heartScale = remember { Animatable(1f) }
                val coroutineScope = rememberCoroutineScope()
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .clickable {
                            viewModel.toggleLikeReel(reel.id)
                            coroutineScope.launch {
                                heartScale.animateTo(1.4f, spring(dampingRatio = Spring.DampingRatioHighBouncy))
                                heartScale.animateTo(1f)
                            }
                        }
                        .testTag("reel_like_${reel.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (reel.isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Like Reel",
                        tint = if (reel.isLiked) Color.Red else Color.White,
                        modifier = Modifier
                            .size(24.dp)
                            .align(Alignment.Center)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reel.likes}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Comments Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .clickable { showComments = true }
                        .testTag("reel_comment_trigger_${reel.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "💬", fontSize = 20.sp)
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reel.comments.size}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            // Shares Action
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(45.dp)
                        .background(Color.Black.copy(alpha = 0.4f), CircleShape)
                        .clickable {
                            viewModel.shareReel(reel.id)
                            showShareSheet = true
                        }
                        .testTag("reel_share_trigger_${reel.id}"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = "Share Reel",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "${reel.shares}",
                    color = Color.White,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // 5. Comments Sheet PopUp Drawer
        if (showComments) {
            ReelCommentsSheet(
                reel = reel,
                viewModel = viewModel,
                onDismiss = { showComments = false }
            )
        }

        // 6. Share Sheet PopUp Drawer
        if (showShareSheet) {
            ReelShareSheet(
                reel = reel,
                onDismiss = { showShareSheet = false }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelCommentsSheet(
    reel: HeritageReel,
    viewModel: MythicViewModel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = Color(0xFF141414),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight(0.65f)
                .fillMaxWidth()
                .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding())
        ) {
            // Header
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp, horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "${reel.comments.size} Comments",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color.White,
                        fontWeight = FontWeight.ExtraBold
                    )
                )
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Comments list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                items(reel.comments) { comment ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .border(1.dp, Color.White.copy(alpha = 0.2f), CircleShape)
                        ) {
                            AsyncImage(
                                model = comment.avatarUrl,
                                contentDescription = "Commenter Avatar",
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = comment.username,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = comment.timestamp,
                                    fontSize = 10.sp,
                                    color = Color.Gray
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = comment.text,
                                fontSize = 13.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }

            Divider(color = Color.White.copy(alpha = 0.1f))

            // Realtime Type and Post Comment Row
            var newCommentText by remember { mutableStateOf("") }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = newCommentText,
                    onValueChange = { newCommentText = it },
                    placeholder = { Text("Add supportive comment...", color = Color.Gray, fontSize = 13.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reel_comment_input"),
                    shape = RoundedCornerShape(100.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.2f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White,
                        cursorColor = MaterialTheme.colorScheme.primary
                    ),
                    singleLine = true
                )

                IconButton(
                    onClick = {
                        if (newCommentText.isNotBlank()) {
                            viewModel.addCommentToReel(reel.id, newCommentText)
                            newCommentText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .background(MaterialTheme.colorScheme.primary, CircleShape)
                        .testTag("reel_comment_send_btn")
                ) {
                    Icon(
                        imageVector = Icons.Default.Send,
                        contentDescription = "Send Comment",
                        tint = Color.Black,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReelShareSheet(
    reel: HeritageReel,
    onDismiss: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = rememberModalBottomSheetState(),
        containerColor = Color(0xFF141414),
        contentColor = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = "Reshare Heritage Clip",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ShareOptionItem("🔗", "Copy Link")
                ShareOptionItem("💬", "Guardian Chat")
                ShareOptionItem("💚", "WhatsApp")
                ShareOptionItem("👑", "Order Feed")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
            ) {
                Text("Close", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun ShareOptionItem(icon: String, label: String) {
    var isShared by remember { mutableStateOf(false) }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { isShared = true }
    ) {
        Box(
            modifier = Modifier
                .size(50.dp)
                .background(Color.DarkGray, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(text = icon, fontSize = 24.sp)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(text = if (isShared) "Shared!" else label, color = if (isShared) MaterialTheme.colorScheme.primary else Color.LightGray, fontSize = 11.sp)
    }
}
