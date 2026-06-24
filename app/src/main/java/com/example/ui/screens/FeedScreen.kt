package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.data.remote.HeritageArticle
import com.example.viewmodel.MythicViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: MythicViewModel
) {
    val savedSites by viewModel.savedSites.collectAsState()
    val activeFeedTab by viewModel.activeFeedTab.collectAsState()
    val articles by viewModel.feedArticles.collectAsState()

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
            .background(currentTheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Screen Title
            Text(
                text = "Heritage Feed",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                ),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 4.dp)
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
                contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 100.dp),
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
}
