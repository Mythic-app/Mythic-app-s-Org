package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey val id: String,
    val username: String,
    val email: String,
    val fullName: String,
    val avatarUrl: String,
    val level: Int,
    val xp: Int,
    val streak: Int,
    val scansCount: Int,
    val badgesCount: Int
)

@Entity(tableName = "badges")
data class BadgeEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val code: String,
    val name: String,
    val description: String,
    val tier: String, // bronze, silver, gold, platinum
    val unlockedAt: Long,
    val icon: String?
)

@Entity(tableName = "quests")
data class QuestEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val title: String,
    val description: String,
    val rewardXp: Int,
    val progress: Int,
    val target: Int,
    val type: String, // daily, weekly, achievement
    val completed: Boolean
)

@Entity(tableName = "scan_history")
data class ScanHistoryEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val siteName: String,
    val province: String,
    val description: String,
    val unescoStatus: String,
    val era: String,
    val facts: String, // Comma-separated or JSON string
    val xpEarned: Int,
    val imageUrl: String?,
    val timestamp: Long
)

@Entity(tableName = "saved_sites")
data class SavedSiteEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val siteName: String,
    val province: String,
    val imageUrl: String?,
    val timestamp: Long
)

@Entity(tableName = "lumo_conversations")
data class LumoConversationEntity(
    @PrimaryKey val id: String,
    val userId: String,
    val messageId: String,
    val role: String, // user, assistant
    val content: String,
    val timestamp: Long
)

@Entity(tableName = "user_preferences")
data class UserPreferencesEntity(
    @PrimaryKey val userId: String,
    val darkMode: Boolean,
    val notificationsEnabled: Boolean,
    val language: String
)
