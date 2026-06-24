package com.example.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import com.example.data.local.BadgeEntity
import com.example.data.local.QuestEntity
import com.example.viewmodel.MythicViewModel

@Composable
fun QuestsScreen(
    viewModel: MythicViewModel
) {
    val quests by viewModel.quests.collectAsState()
    val badges by viewModel.badges.collectAsState()

    val currentTheme = MaterialTheme.colorScheme
    val isDark = currentTheme.background == Color(0xFF000000)

    val textPrimary = currentTheme.onBackground
    val textSecondary = currentTheme.onSurfaceVariant
    val accentColor = currentTheme.primary

    var activeTab by remember { mutableStateOf("Daily") } // "Daily", "Badges"

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(currentTheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 88.dp)
        ) {
            // Screen Title
            Text(
                text = "Quests",
                style = MaterialTheme.typography.displayLarge.copy(
                    fontWeight = FontWeight.Black,
                    color = textPrimary
                ),
                modifier = Modifier.padding(start = 20.dp, end = 20.dp, top = 24.dp, bottom = 12.dp)
            )

            // Tabs Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 20.dp, end = 20.dp, bottom = 16.dp)
                    .clip(RoundedCornerShape(100.dp))
                    .background(if (isDark) Color(0xFF111111) else Color(0xFFEEEEEE))
                    .border(
                        1.dp,
                        if (isDark) Color(0x33FFFFFF) else Color.Transparent,
                        RoundedCornerShape(100.dp)
                    )
            ) {
                val tabs = listOf("Daily", "Badges")
                tabs.forEach { tab ->
                    val isSelected = activeTab == tab
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(100.dp))
                            .background(if (isSelected) accentColor else Color.Transparent)
                            .clickable { activeTab = tab }
                            .padding(vertical = 12.dp)
                            .testTag("quest_tab_${tab.lowercase()}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (tab == "Daily") "Active Quests" else "Unlocked Badges",
                            color = if (isSelected) Color.Black else textPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Tabs Content
            if (activeTab == "Daily") {
                QuestsListTab(quests, isDark, textPrimary, textSecondary, accentColor)
            } else {
                BadgesGridTab(badges, isDark, textPrimary, textSecondary, accentColor)
            }
        }
    }
}

@Composable
fun QuestsListTab(
    quests: List<QuestEntity>,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color
) {
    if (quests.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "No quests available today!", color = textSecondary)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(quests) { quest ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = quest.title,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            )
                            Box(
                                modifier = Modifier
                                    .background(accentColor.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "+${quest.rewardXp} XP",
                                    color = accentColor,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = quest.description,
                            style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
                        )
                        Spacer(modifier = Modifier.height(16.dp))

                        // Progress bar calculations
                        val progressPct = quest.progress.toFloat() / quest.target.toFloat()
                        val animatedProgress by animateFloatAsState(targetValue = progressPct)

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Progress bar slider
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .height(8.dp)
                                    .clip(RoundedCornerShape(100.dp))
                                    .background(if (isDark) Color(0xFF222222) else Color(0xFFE0E0E0))
                            ) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .fillMaxWidth(fraction = animatedProgress.coerceIn(0f, 1f))
                                        .background(if (quest.completed) Color(0xFF9AF04D) else accentColor)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(
                                text = "${quest.progress}/${quest.target}",
                                color = if (quest.completed) Color(0xFF9AF04D) else textPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        if (quest.completed) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "✓ Quest Completed Successfully",
                                color = Color(0xFF9AF04D),
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

@Composable
fun BadgesGridTab(
    badges: List<BadgeEntity>,
    isDark: Boolean,
    textPrimary: Color,
    textSecondary: Color,
    accentColor: Color
) {
    if (badges.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text(text = "Scan landmarks and solve quizzes to unlock badges!", color = textSecondary)
        }
    } else {
        LazyColumn(
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(badges) { badge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
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
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Badge circular emblem
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(RoundedCornerShape(100.dp))
                                .background(accentColor.copy(alpha = 0.15f))
                                .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(100.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = badge.icon ?: "🏆", fontSize = 28.sp)
                        }

                        // Badge metadata
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = badge.name,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = textPrimary
                                )
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = badge.description,
                                style = MaterialTheme.typography.bodyMedium.copy(color = textSecondary)
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            val dateFormat = remember { SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()) }
                            val dateStr = remember(badge.unlockedAt) { dateFormat.format(Date(badge.unlockedAt)) }
                            Text(
                                text = "Unlocked on $dateStr",
                                color = accentColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}
