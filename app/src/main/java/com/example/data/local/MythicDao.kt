package com.example.data.local

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface MythicDao {
    
    // --- Profile Queries ---
    @Query("SELECT * FROM profiles LIMIT 1")
    fun getProfileFlow(): Flow<ProfileEntity?>

    @Query("SELECT * FROM profiles LIMIT 1")
    suspend fun getProfile(): ProfileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity)

    @Query("DELETE FROM profiles")
    suspend fun clearProfile()

    // --- Badges Queries ---
    @Query("SELECT * FROM badges ORDER BY unlockedAt DESC")
    fun getBadgesFlow(): Flow<List<BadgeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadges(badges: List<BadgeEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBadge(badge: BadgeEntity)

    @Query("DELETE FROM badges")
    suspend fun clearBadges()

    // --- Quests Queries ---
    @Query("SELECT * FROM quests ORDER BY id ASC")
    fun getQuestsFlow(): Flow<List<QuestEntity>>

    @Query("SELECT * FROM quests ORDER BY id ASC")
    suspend fun getQuests(): List<QuestEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<QuestEntity>)

    @Update
    suspend fun updateQuest(quest: QuestEntity)

    @Query("DELETE FROM quests")
    suspend fun clearQuests()

    // --- Scan History Queries ---
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getScanHistoryFlow(): Flow<List<ScanHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScan(scan: ScanHistoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertScans(scans: List<ScanHistoryEntity>)

    @Query("DELETE FROM scan_history")
    suspend fun clearScanHistory()

    // --- Saved Sites Queries ---
    @Query("SELECT * FROM saved_sites ORDER BY timestamp DESC")
    fun getSavedSitesFlow(): Flow<List<SavedSiteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSavedSite(site: SavedSiteEntity)

    @Query("DELETE FROM saved_sites WHERE siteName = :siteName")
    suspend fun deleteSavedSite(siteName: String)

    @Query("SELECT EXISTS(SELECT 1 FROM saved_sites WHERE siteName = :siteName)")
    suspend fun isSiteSaved(siteName: String): Boolean

    @Query("DELETE FROM saved_sites")
    suspend fun clearSavedSites()

    // --- Lumo Conversations Queries ---
    @Query("SELECT * FROM lumo_conversations ORDER BY timestamp ASC")
    fun getLumoConversationsFlow(): Flow<List<LumoConversationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLumoMessage(message: LumoConversationEntity)

    @Query("DELETE FROM lumo_conversations")
    suspend fun clearLumoConversations()

    // --- User Preferences Queries ---
    @Query("SELECT * FROM user_preferences LIMIT 1")
    fun getUserPreferencesFlow(): Flow<UserPreferencesEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserPreferences(prefs: UserPreferencesEntity)
}
