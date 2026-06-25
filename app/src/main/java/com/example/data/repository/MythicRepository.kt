package com.example.data.repository

import android.content.Context
import android.util.Log
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.remote.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import java.util.UUID

class MythicRepository(context: Context) :
    ProfileRepository,
    XPRepository,
    BadgeRepository,
    QuestRepository,
    ScanHistoryRepository,
    SavedSitesRepository,
    UserPreferencesRepository {
    private val db = MythicDatabase.getDatabase(context)
    private val dao = db.mythicDao()

    // --- Reactive flows for UI ---
    override val profile: Flow<ProfileEntity?> = dao.getProfileFlow()
    override val badges: Flow<List<BadgeEntity>> = dao.getBadgesFlow()
    override val quests: Flow<List<QuestEntity>> = dao.getQuestsFlow()
    override val scanHistory: Flow<List<ScanHistoryEntity>> = dao.getScanHistoryFlow()
    override val savedSites: Flow<List<SavedSiteEntity>> = dao.getSavedSitesFlow()
    val lumoMessages: Flow<List<LumoConversationEntity>> = dao.getLumoConversationsFlow()
    override val preferences: Flow<UserPreferencesEntity?> = dao.getUserPreferencesFlow()

    // --- Local Logged In User State ---
    private val _currentUser = MutableStateFlow<SupabaseUser?>(null)
    val currentUser: StateFlow<SupabaseUser?> = _currentUser.asStateFlow()

    init {
        // Initialize simple guest/default user profile if none exists
        // so that the app is immediately populated and beautiful
        // without requiring a cloud signup.
        _currentUser.value = SupabaseUser(
            id = "default_user_123",
            email = "explorer@mythic.lk",
            userMetadata = mapOf("username" to "explorer_01", "full_name" to "Heritage Explorer")
        )
    }

    suspend fun checkSession() {
        // Simple session recovery emulated locally
        val existingProfile = dao.getProfile()
        if (existingProfile != null) {
            _currentUser.value = SupabaseUser(
                id = existingProfile.id,
                email = existingProfile.email,
                userMetadata = mapOf("username" to existingProfile.username, "full_name" to existingProfile.fullName)
            )
        } else {
            // Seed a default profile for first-time premium sandbox experience
            seedDefaultData()
        }
    }

    private suspend fun seedDefaultData() {
        val defaultId = "default_user_123"
        val profile = ProfileEntity(
            id = defaultId,
            username = "explorer_01",
            email = "explorer@mythic.lk",
            fullName = "Heritage Explorer",
            avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=explorer",
            level = 5,
            xp = 2750, // 4 * 600 + 350 XP = 2750 XP (which is Level 5, 350 / 600 XP)
            streak = 7,
            scansCount = 23,
            badgesCount = 12
        )
        dao.insertProfile(profile)

        // Seed default quests
        val quests = listOf(
            QuestEntity(
                id = "quest_scan_1",
                userId = defaultId,
                title = "Scan a Heritage Site",
                description = "Scan any site using your camera",
                rewardXp = 50,
                progress = 0,
                target = 1,
                type = "daily",
                completed = false
            ),
            QuestEntity(
                id = "quest_read_3",
                userId = defaultId,
                title = "Read 3 Articles",
                description = "Read 3 heritage articles",
                rewardXp = 30,
                progress = 1,
                target = 3,
                type = "daily",
                completed = false
            ),
            QuestEntity(
                id = "quest_like_5",
                userId = defaultId,
                title = "Like 5 Articles",
                description = "Like 5 articles in feed",
                rewardXp = 20,
                progress = 2,
                target = 5,
                type = "daily",
                completed = false
            ),
            QuestEntity(
                id = "quest_maintain_streak",
                userId = defaultId,
                title = "Maintain Streak",
                description = "Keep your streak alive",
                rewardXp = 100,
                progress = 1,
                target = 1,
                type = "weekly",
                completed = true
            )
        )
        dao.insertQuests(quests)

        // Seed some default badges
        val defaultBadges = listOf(
            BadgeEntity(
                id = "badge_explorer",
                userId = defaultId,
                code = "explorer",
                name = "Explorer Badge",
                description = "Unlock by exploring your first site",
                tier = "bronze",
                unlockedAt = System.currentTimeMillis() - 86400000 * 3,
                icon = "🏆"
            ),
            BadgeEntity(
                id = "badge_archaeologist",
                userId = defaultId,
                code = "archaeologist",
                name = "Heritage Guardian",
                description = "Successfully scan 5 historical sites",
                tier = "silver",
                unlockedAt = System.currentTimeMillis() - 86400000 * 2,
                icon = "🏛️"
            ),
            BadgeEntity(
                id = "badge_photographer",
                userId = defaultId,
                code = "streak_master",
                name = "Streak Master",
                description = "Maintain a 7-day scanning streak",
                tier = "gold",
                unlockedAt = System.currentTimeMillis() - 86400000,
                icon = "🔥"
            ),
            BadgeEntity(
                id = "badge_historian",
                userId = defaultId,
                code = "historian",
                name = "Knowledge Seeker",
                description = "Read all suggestions in feed",
                tier = "platinum",
                unlockedAt = System.currentTimeMillis(),
                icon = "📜"
            )
        )
        dao.insertBadges(defaultBadges)

        // Seed pre-existing scan history
        val defaultScans = listOf(
            ScanHistoryEntity(
                id = "scan_sigiriya_seed",
                userId = defaultId,
                siteName = "Sigiriya Rock Fortress",
                province = "Central Province",
                description = "An ancient rock fortress with ancient frescoes.",
                unescoStatus = "UNESCO World Heritage Site",
                era = "5th Century AD",
                facts = "Mirror Wall, Lion's Paw gate, Water gardens",
                xpEarned = 50,
                imageUrl = "https://images.unsplash.com/photo-1588598126781-db26040a4cfc?w=600",
                timestamp = System.currentTimeMillis() - 3600000 * 4
            ),
            ScanHistoryEntity(
                id = "scan_galle_seed",
                userId = defaultId,
                siteName = "Galle Fort",
                province = "Southern Province",
                description = "A historical fortified city built by the Portuguese.",
                unescoStatus = "UNESCO World Heritage Site",
                era = "16th Century AD",
                facts = "Dutch Reformed Church, Lighthouse, Bastions",
                xpEarned = 50,
                imageUrl = "https://images.unsplash.com/photo-1546708973-b339540b5162?w=600",
                timestamp = System.currentTimeMillis() - 3600000 * 24
            )
        )
        dao.insertScans(defaultScans)

        // Seed some conversation with LUMO
        dao.insertLumoMessage(
            LumoConversationEntity(
                id = "msg_welcome",
                userId = defaultId,
                messageId = UUID.randomUUID().toString(),
                role = "assistant",
                content = "Aayubowan! 🇱🇰 I am LUMO, your Sri Lankan heritage guide. Ask me anything about Sigiriya, Anuradhapura, or our glorious history!",
                timestamp = System.currentTimeMillis() - 600000
            )
        )
    }

    // --- AUTH OPERATIONS ---
    suspend fun signUp(username: String, email: String, password: String): String? = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClient.isConfigured) {
                val response = SupabaseClient.service.signUp(
                    SupabaseSignUpRequest(email, password, mapOf("username" to username, "full_name" to username))
                )
                if (response.isSuccessful && response.body() != null) {
                    val authBody = response.body()!!
                    val user = authBody.user
                    if (user != null) {
                        // User needs to confirm email first, so we do NOT set _currentUser.value yet
                        _currentUser.value = null
                        // Insert a provisional profile in local cache
                        dao.insertProfile(
                            ProfileEntity(
                                id = user.id,
                                username = username,
                                email = email,
                                fullName = username,
                                avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$username",
                                level = 1,
                                xp = 0,
                                streak = 1,
                                scansCount = 0,
                                badgesCount = 0
                            )
                        )
                        return@withContext "VERIFICATION_SENT"
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    if (errorBody.contains("already registered", ignoreCase = true)) {
                        return@withContext "This email is already registered."
                    }
                    return@withContext "Registration failed. Try a different email or password."
                }
            }
            
            // Local emulation fallback (emulates email verification prompt)
            val mockId = UUID.randomUUID().toString()
            // We insert into local cache, but don't set current user yet, requiring a fake login
            dao.insertProfile(
                ProfileEntity(
                    id = mockId,
                    username = username,
                    email = email,
                    fullName = username,
                    avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$username",
                    level = 1,
                    xp = 0,
                    streak = 1,
                    scansCount = 0,
                    badgesCount = 0
                )
            )
            return@withContext "VERIFICATION_SENT_MOCK"
        } catch (e: Exception) {
            Log.e("MythicRepository", "SignUp failed", e)
            return@withContext "An unexpected network error occurred: ${e.message}"
        }
    }

    suspend fun login(email: String, password: String): String? = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClient.isConfigured) {
                val response = SupabaseClient.service.login(SupabaseLoginRequest(email, password))
                if (response.isSuccessful && response.body()?.user != null) {
                    val user = response.body()!!.user!!
                    
                    // If email verification is enabled on Supabase, emailConfirmedAt will be null on unconfirmed users
                    if (user.emailConfirmedAt == null) {
                        return@withContext "EMAIL_NOT_CONFIRMED"
                    }
                    
                    _currentUser.value = user
                    
                    // Pull remote profile
                    val profileResponse = SupabaseClient.service.getProfile("id=eq.${user.id}")
                    if (profileResponse.isSuccessful && profileResponse.body()?.isNotEmpty() == true) {
                        val p = profileResponse.body()!![0]
                        dao.insertProfile(
                            ProfileEntity(
                                id = p.id,
                                username = p.username,
                                email = p.email,
                                fullName = p.fullName ?: p.username,
                                avatarUrl = p.avatarUrl ?: "https://api.dicebear.com/7.x/avataaars/svg?seed=${p.username}",
                                level = p.level,
                                xp = p.xp,
                                streak = p.streak,
                                scansCount = p.scansCount,
                                badgesCount = p.badgesCount
                            )
                        )
                    }
                    return@withContext null // Success
                } else {
                    val errorBody = response.errorBody()?.string() ?: ""
                    if (errorBody.contains("Email not confirmed", ignoreCase = true)) {
                        return@withContext "EMAIL_NOT_CONFIRMED"
                    }
                    return@withContext "Invalid email or password. Please try again."
                }
            }

            // Local fallback login
            val existing = dao.getProfile()
            if (existing != null && existing.email == email) {
                _currentUser.value = SupabaseUser(
                    existing.id,
                    existing.email,
                    emailConfirmedAt = "2026-06-24T00:00:00Z", // Emulate confirmed email
                    mapOf("username" to existing.username, "full_name" to existing.fullName)
                )
                return@withContext null // Success
            } else if (email.isNotEmpty() && password.isNotEmpty()) {
                // Emulate dynamic creation of user
                val mockId = UUID.randomUUID().toString()
                val name = email.split("@")[0]
                val user = SupabaseUser(
                    mockId, 
                    email, 
                    emailConfirmedAt = "2026-06-24T00:00:00Z", // Emulate confirmed email
                    mapOf("username" to name)
                )
                _currentUser.value = user
                dao.insertProfile(
                    ProfileEntity(
                        id = mockId,
                        username = name,
                        email = email,
                        fullName = name,
                        avatarUrl = "https://api.dicebear.com/7.x/avataaars/svg?seed=$name",
                        level = 1,
                        xp = 0,
                        streak = 1,
                        scansCount = 0,
                        badgesCount = 0
                    )
                )
                return@withContext null // Success
            }
            return@withContext "Incorrect email or password."
        } catch (e: Exception) {
            Log.e("MythicRepository", "Login failed", e)
            return@withContext "Network connection issue: ${e.message}"
        }
    }

    suspend fun recoverPassword(email: String): Boolean = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClient.isConfigured) {
                val response = SupabaseClient.service.recoverPassword(mapOf("email" to email))
                return@withContext response.isSuccessful
            }
            return@withContext true
        } catch (e: Exception) {
            Log.e("MythicRepository", "Recover password failed", e)
            false
        }
    }

    override suspend fun getProfile(): ProfileEntity? = withContext(Dispatchers.IO) {
        dao.getProfile()
    }

    override suspend fun insertProfile(profile: ProfileEntity) = withContext(Dispatchers.IO) {
        dao.insertProfile(profile)
    }

    override suspend fun updateStreak(userId: String) = withContext(Dispatchers.IO) {
        val currentProfile = dao.getProfile() ?: return@withContext
        val now = System.currentTimeMillis()
        val newStreak = currentProfile.streak + 1
        val updated = currentProfile.copy(streak = newStreak)
        dao.insertProfile(updated)

        if (SupabaseClient.isConfigured && _currentUser.value != null) {
            try {
                SupabaseClient.service.updateProfile(
                    "id=eq.${updated.id}",
                    mapOf("streak" to newStreak)
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase streak sync failed", e)
            }
        }
    }

    override suspend fun clearProfile() = withContext(Dispatchers.IO) {
        dao.clearProfile()
    }

    suspend fun logout() = withContext(Dispatchers.IO) {
        try {
            if (SupabaseClient.isConfigured) {
                SupabaseClient.service.logout()
            }
        } catch (e: Exception) {
            Log.e("MythicRepository", "Remote logout failed", e)
        }
        _currentUser.value = null
        clearProfile()
        clearBadges()
        clearQuests()
        clearScanHistory()
        clearSavedSites()
        dao.clearLumoConversations()
    }

    // --- GAME XP SYSTEM ---
    override suspend fun addXp(amount: Int) = withContext(Dispatchers.IO) {
        val currentProfile = dao.getProfile() ?: return@withContext
        val newXp = currentProfile.xp + amount
        // XP calculations: level changes every 600 XP (Level = newXp / 600 + 1)
        val newLevel = (newXp / 600) + 1
        
        val updated = currentProfile.copy(
            xp = newXp,
            level = newLevel
        )
        dao.insertProfile(updated)

        // Try to update Supabase profile
        if (SupabaseClient.isConfigured && _currentUser.value != null) {
            try {
                SupabaseClient.service.updateProfile(
                    "id=eq.${updated.id}",
                    mapOf("xp" to newXp, "level" to newLevel)
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase sync of XP failed", e)
            }
        }
    }

    suspend fun addScanCount() = withContext(Dispatchers.IO) {
        val currentProfile = dao.getProfile() ?: return@withContext
        val updated = currentProfile.copy(scansCount = currentProfile.scansCount + 1)
        dao.insertProfile(updated)
        
        // Try update Supabase profile
        if (SupabaseClient.isConfigured && _currentUser.value != null) {
            try {
                SupabaseClient.service.updateProfile(
                    "id=eq.${updated.id}",
                    mapOf("scans_count" to updated.scansCount)
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase sync of scans_count failed", e)
            }
        }
    }

    // --- BADGES ---
    override suspend fun unlockBadge(
        code: String,
        name: String,
        description: String,
        tier: String,
        icon: String?
    ) = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: "guest"
        val badge = BadgeEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            code = code,
            name = name,
            description = description,
            tier = tier,
            unlockedAt = System.currentTimeMillis(),
            icon = icon ?: "🏆"
        )
        dao.insertBadge(badge)

        if (SupabaseClient.isConfigured) {
            try {
                SupabaseClient.service.saveBadge(
                    SupabaseBadge(
                        id = badge.id,
                        userId = badge.userId,
                        code = badge.code,
                        name = badge.name,
                        description = badge.description,
                        tier = badge.tier,
                        icon = badge.icon
                    )
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase badge sync failed", e)
            }
        }
    }

    override suspend fun clearBadges() = withContext(Dispatchers.IO) {
        dao.clearBadges()
    }

    // --- SAVED SITES ---
    override suspend fun toggleSaveSite(siteName: String, province: String, imageUrl: String?) = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: "guest"
        if (dao.isSiteSaved(siteName)) {
            dao.deleteSavedSite(siteName)
            if (SupabaseClient.isConfigured) {
                try {
                    SupabaseClient.service.deleteSavedSite("user_id=eq.$userId", "site_name=eq.$siteName")
                } catch (e: Exception) {
                    Log.e("MythicRepository", "Supabase saved sites delete failed", e)
                }
            }
        } else {
            val savedSite = SavedSiteEntity(
                id = UUID.randomUUID().toString(),
                userId = userId,
                siteName = siteName,
                province = province,
                imageUrl = imageUrl,
                timestamp = System.currentTimeMillis()
            )
            dao.insertSavedSite(savedSite)
            if (SupabaseClient.isConfigured) {
                try {
                    SupabaseClient.service.saveSite(
                        SupabaseSavedSite(
                            id = savedSite.id,
                            userId = savedSite.userId,
                            siteName = savedSite.siteName,
                            province = savedSite.province,
                            imageUrl = savedSite.imageUrl
                        )
                    )
                } catch (e: Exception) {
                    Log.e("MythicRepository", "Supabase save site failed", e)
                }
            }
        }
    }

    override suspend fun isSiteSaved(siteName: String): Boolean = withContext(Dispatchers.IO) {
        dao.isSiteSaved(siteName)
    }

    override suspend fun clearSavedSites() = withContext(Dispatchers.IO) {
        dao.clearSavedSites()
    }

    // --- QUESTS PROGRESS ---
    override suspend fun incrementQuestProgress(type: String) = withContext(Dispatchers.IO) {
        val currentQuests = dao.getQuests()
        for (q in currentQuests) {
            // increment daily scan/read/like quests based on triggers
            if (!q.completed) {
                var triggered = false
                var inc = 0
                if (type == "scan" && q.title.contains("Scan", ignoreCase = true)) {
                    triggered = true
                    inc = 1
                } else if (type == "read" && q.title.contains("Read", ignoreCase = true)) {
                    triggered = true
                    inc = 1
                } else if (type == "like" && q.title.contains("Like", ignoreCase = true)) {
                    triggered = true
                    inc = 1
                }

                if (triggered) {
                    val newProgress = (q.progress + inc).coerceAtMost(q.target)
                    val completed = newProgress >= q.target
                    val updated = q.copy(progress = newProgress, completed = completed)
                    dao.updateQuest(updated)
                    if (completed) {
                        addXp(q.rewardXp)
                    }
                    
                    if (SupabaseClient.isConfigured) {
                        try {
                            SupabaseClient.service.updateQuest(
                                "id=eq.${q.id}",
                                mapOf("progress" to newProgress, "completed" to completed)
                            )
                        } catch (e: Exception) {
                            Log.e("MythicRepository", "Supabase quest update failed", e)
                        }
                    }
                }
            }
        }
    }

    override suspend fun clearQuests() = withContext(Dispatchers.IO) {
        dao.clearQuests()
    }

    // --- SCAN LANDMARK ---
    override suspend fun scanHeritage(siteName: String, imageUrl: String?): ScanHistoryEntity = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: "guest"
        
        // Define site metadata
        val province = when(siteName) {
            "Sigiriya Rock Fortress" -> "Central Province"
            "Temple of the Tooth" -> "Central Province"
            "Dambulla Cave Temple" -> "Central Province"
            "Galle Fort" -> "Southern Province"
            "Anuradhapura" -> "North Central Province"
            "Polonnaruwa Ancient City" -> "North Central Province"
            "Jaffna Fort" -> "Northern Province"
            else -> "Central Province"
        }

        val description = when(siteName) {
            "Sigiriya Rock Fortress" -> "An ancient rock fortress and castle complex built by King Kashyapa in the 5th century AD. It is a UNESCO World Heritage Site famous for its frescoes, water gardens, and advanced urban planning."
            "Temple of the Tooth" -> "A glorious golden-roofed Buddhist temple in Kandy which houses the sacred tooth relic of the Buddha."
            "Dambulla Cave Temple" -> "The largest and best-preserved cave temple complex in Sri Lanka, boasting magnificent Buddha statues and complex historical wall murals."
            "Galle Fort" -> "A historical fortified city built first by the Portuguese, then heavily fortified by the Dutch, standing as an archaeological treasure."
            "Anuradhapura" -> "The magnificent ancient capital of Sri Lanka, home to colossal stupas like Ruwanwelisaya and Jetavanaramaya."
            "Polonnaruwa Ancient City" -> "The second ancient capital of Sri Lanka, renowned for its incredible stone sculptures at Gal Vihara and ruins of grand palaces."
            "Jaffna Fort" -> "A beautiful historical star-shaped coastal fort built by the Portuguese in 1618, overlooking the scenic Jaffna lagoon."
            else -> "A majestic Sri Lankan heritage site filled with centuries of cultural wealth, sacred history, and architectural marvel."
        }

        val unescoStatus = when(siteName) {
            "Sigiriya Rock Fortress", "Temple of the Tooth", "Dambulla Cave Temple", "Galle Fort", "Anuradhapura", "Polonnaruwa Ancient City" -> "UNESCO World Heritage Site"
            else -> "Protected Cultural Monument"
        }

        val era = when(siteName) {
            "Sigiriya Rock Fortress" -> "5th Century AD"
            "Temple of the Tooth" -> "16th Century AD"
            "Dambulla Cave Temple" -> "1st Century BC"
            "Galle Fort" -> "16th Century AD"
            "Anuradhapura" -> "3rd Century BC"
            "Polonnaruwa Ancient City" -> "11th Century AD"
            "Jaffna Fort" -> "17th Century AD"
            else -> "Ancient Era"
        }

        val facts = when(siteName) {
            "Sigiriya Rock Fortress" -> "The mirror wall still contains ancient graffiti; The lion gate was a massive masonry lion; Advanced hydraulic systems feed the active garden fountains."
            "Temple of the Tooth" -> "Houses the sacred tooth relic of Gautama Buddha; Inside the Inner Sanctuary; Holds the famous annual Esala Perahera festival."
            "Dambulla Cave Temple" -> "Spread over five separate cavern structures; Houses 153 Buddha statues; Pre-historic human skeletal remains found in nearby caves."
            "Galle Fort" -> "A fusion of European architectural styles and South Asian traditions; Stands unharmed by the 2004 tsunami; Houses colonial-era villas."
            "Anuradhapura" -> "Ruwanwelisaya is a marvel of ancient engineering; The sacred Jaya Sri Maha Bodhi is the oldest human-planted tree; Complex water reservoirs."
            "Polonnaruwa Ancient City" -> "Features the majestic Gal Vihara rock-cut Buddha statues; Parakrama Samudra is an immense artificial lake; Polonnaruwa Vatadage is fully intact."
            "Jaffna Fort" -> "Portugal established key fortifications; Captured and modified into a star-fort by the Dutch; Star-shaped layout is highly visible."
            else -> "Houses rich archaeological discoveries; Serves as a vital cultural monument; Reflects outstanding engineering and ancient design."
        }

        val scan = ScanHistoryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            siteName = siteName,
            province = province,
            description = description,
            unescoStatus = unescoStatus,
            era = era,
            facts = facts,
            xpEarned = 50,
            imageUrl = imageUrl ?: "https://images.unsplash.com/photo-1588598126781-db26040a4cfc?w=600",
            timestamp = System.currentTimeMillis()
        )

        // Store locally
        dao.insertScan(scan)
        
        // Add game statistics and progress
        addXp(50)
        addScanCount()
        incrementQuestProgress("scan")

        // Try to update Supabase
        if (SupabaseClient.isConfigured) {
            try {
                SupabaseClient.service.saveScan(
                    SupabaseScan(
                        id = scan.id,
                        userId = scan.userId,
                        siteName = scan.siteName,
                        province = scan.province,
                        description = scan.description,
                        unescoStatus = scan.unescoStatus,
                        era = scan.era,
                        facts = scan.facts,
                        xpEarned = scan.xpEarned,
                        imageUrl = scan.imageUrl
                    )
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase scan save failed", e)
            }
        }

        return@withContext scan
    }

    override suspend fun saveCustomScan(
        siteName: String,
        province: String,
        description: String,
        unescoStatus: String,
        era: String,
        facts: String,
        imageUrl: String?
    ): ScanHistoryEntity = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: "guest"
        val scan = ScanHistoryEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            siteName = siteName,
            province = province,
            description = description,
            unescoStatus = unescoStatus,
            era = era,
            facts = facts,
            xpEarned = 50,
            imageUrl = imageUrl ?: "https://images.unsplash.com/photo-1588598126781-db26040a4cfc?w=600",
            timestamp = System.currentTimeMillis()
        )
        dao.insertScan(scan)
        addXp(50)
        addScanCount()
        incrementQuestProgress("scan")

        if (SupabaseClient.isConfigured) {
            try {
                SupabaseClient.service.saveScan(
                    SupabaseScan(
                        id = scan.id,
                        userId = scan.userId,
                        siteName = scan.siteName,
                        province = scan.province,
                        description = scan.description,
                        unescoStatus = scan.unescoStatus,
                        era = scan.era,
                        facts = scan.facts,
                        xpEarned = scan.xpEarned,
                        imageUrl = scan.imageUrl
                    )
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase custom scan save failed", e)
            }
        }
        return@withContext scan
    }

    override suspend fun clearScanHistory() = withContext(Dispatchers.IO) {
        dao.clearScanHistory()
    }

    // --- REPORTS ---
    suspend fun submitReport(report: SupabaseReport) = withContext(Dispatchers.IO) {
        if (SupabaseClient.isConfigured) {
            try {
                SupabaseClient.service.saveReport(report)
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase report save failed", e)
            }
        }
    }

    suspend fun getRecentReports(): List<SupabaseReport> = withContext(Dispatchers.IO) {
        if (SupabaseClient.isConfigured) {
            try {
                SupabaseClient.service.getReports().body() ?: emptyList()
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase reports fetch failed", e)
                emptyList()
            }
        } else {
            emptyList()
        }
    }

    // --- LUMO COMPANION CHAT ---
    suspend fun sendLumoMessage(userMessageContent: String): String = withContext(Dispatchers.IO) {
        val userId = _currentUser.value?.id ?: "guest"
        
        // Save user message
        val userMsg = LumoConversationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            messageId = UUID.randomUUID().toString(),
            role = "user",
            content = userMessageContent,
            timestamp = System.currentTimeMillis()
        )
        dao.insertLumoMessage(userMsg)

        if (SupabaseClient.isConfigured) {
            try {
                SupabaseClient.service.saveLumoMessage(
                    SupabaseLumoMessage(
                        id = userMsg.id,
                        userId = userMsg.userId,
                        messageId = userMsg.messageId,
                        role = userMsg.role,
                        content = userMsg.content
                    )
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase userMsg save failed", e)
            }
        }

        // Call Gemini or fallback locally
        val assistantResponse = if (GeminiClient.isConfigured) {
            try {
                val systemInstruction = "You are LUMO, an expert Sri Lankan heritage guide, archeologist, and cultural educator. Limit your responses to the historical, cultural, geographical, and architectural facts of Sri Lanka. Keep your tone highly conversational, inspiring, and rich in historical detail. Include emojis where helpful."
                val request = GeminiRequest(
                    contents = listOf(
                        GeminiContent(
                            role = "user",
                            parts = listOf(GeminiPart(text = userMessageContent))
                        )
                    ),
                    systemInstruction = GeminiContent(
                        role = "system",
                        parts = listOf(GeminiPart(text = systemInstruction))
                    )
                )
                val res = GeminiClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                if (res.isSuccessful && res.body() != null) {
                    res.body()!!.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                        ?: getLocalLumoResponse(userMessageContent)
                } else {
                    getLocalLumoResponse(userMessageContent)
                }
            } catch (e: Exception) {
                Log.e("MythicRepository", "Gemini API failed, falling back to local Lumo", e)
                getLocalLumoResponse(userMessageContent)
            }
        } else {
            getLocalLumoResponse(userMessageContent)
        }

        // Save assistant response
        val assistantMsg = LumoConversationEntity(
            id = UUID.randomUUID().toString(),
            userId = userId,
            messageId = UUID.randomUUID().toString(),
            role = "assistant",
            content = assistantResponse,
            timestamp = System.currentTimeMillis()
        )
        dao.insertLumoMessage(assistantMsg)

        if (SupabaseClient.isConfigured) {
            try {
                SupabaseClient.service.saveLumoMessage(
                    SupabaseLumoMessage(
                        id = assistantMsg.id,
                        userId = assistantMsg.userId,
                        messageId = assistantMsg.messageId,
                        role = assistantMsg.role,
                        content = assistantMsg.content
                    )
                )
            } catch (e: Exception) {
                Log.e("MythicRepository", "Supabase assistantMsg save failed", e)
            }
        }

        return@withContext assistantResponse
    }

    private fun getLocalLumoResponse(userQuery: String): String {
        val q = userQuery.lowercase()
        return when {
            q.contains("sigiriya") || q.contains("kashyapa") -> {
                "Sigiriya Rock Fortress is one of Sri Lanka's most iconic sites! 🏛️ Built in the 5th century AD by King Kashyapa, it served as a secure royal palace and a dramatic canvas for the famous Sigiriya frescoes (the beautiful cloud maidens). The site is an engineering masterpiece with working hydraulic gravity-fed fountains. Don't forget to climb through the massive Lion's Paw Gate!"
            }
            q.contains("tooth") || q.contains("kandy") || q.contains("temple") -> {
                "The Sri Dalada Maligawa (Temple of the Sacred Tooth Relic) in Kandy is a deeply revered Buddhist site. 🏰 It houses the sacred left canine tooth of Gautama Buddha, which was brought to Sri Lanka in the 4th century AD. It represents a symbol of royal authority—whoever holds the relic is believed to hold the right to rule the land. Every year, the city hosts the grand Esala Perahera festival, featuring glowing lanterns, traditional Kandyan dancers, and majestic adorned elephants!"
            }
            q.contains("galle") || q.contains("fort") -> {
                "Galle Fort in the Southern Province is a magnificent coastal bastion! 🌊 Initially built by the Portuguese in 1588, it was heavily fortified by the Dutch in the 17th century. It stands as a living museum of colonial architecture fused with South Asian traditions. Be sure to check out the Galle Lighthouse, the Dutch Reformed Church, and walk along the massive stone walls at sunset!"
            }
            q.contains("anuradhapura") || q.contains("ruwanweli") -> {
                "Anuradhapura was the first glorious capital of ancient Sri Lanka, founded in the 4th century BC! 🏛️ It houses some of the tallest brick stupas in the world, such as the colossal Ruwanwelisaya and Jetavanaramaya. It is also home to the sacred Jaya Sri Maha Bodhi, a sapling from the original Bodhi Tree under which the Buddha attained enlightenment, making it the oldest historical tree planted by humans in the world!"
            }
            q.contains("polonnaruwa") || q.contains("gal vihara") -> {
                "Polonnaruwa succeeded Anuradhapura as the medieval capital of Sri Lanka (11th - 13th century AD). 📜 It is famous for its outstanding stone architecture! The Gal Vihara temple features four massive Buddha statues carved directly out of a single granite rock face. It also features the beautiful round relic house called the Vatadage and the enormous Parakrama Samudra artificial lake, built by King Parakramabahu the Great."
            }
            q.contains("quiz") || q.contains("test") -> {
                "Let's test your knowledge! 🧠 Here is a quick heritage quiz:\n\n*Which ancient Sri Lankan king built the magnificent Sigiriya Rock Fortress in the 5th century AD?*\n\nA) King Devanampiyatissa\nB) King Kashyapa\nC) King Parakramabahu\n\nReply with the answer and I'll verify it!"
            }
            q.contains("b") || q.contains("kashyapa") -> {
                "Correct! 🎉 King Kashyapa built Sigiriya as his spectacular royal fortress in the sky to protect himself from his brother, Mugalan. You've earned a virtual Explorer Star! ⭐ Ask me about another heritage site to learn more!"
            }
            q.contains("recommend") || q.contains("visit") || q.contains("where") -> {
                "I highly recommend visiting the **Dambulla Cave Temple**! ⛰️ It consists of five majestic caves carved into a massive rock, housing 153 outstanding statues of the Buddha and covered in ancient ceiling paintings from over 2,000 years ago. It is incredibly well preserved and deeply spiritual."
            }
            q.contains("history") || q.contains("culture") || q.contains("sri lanka") -> {
                "Sri Lanka has a documented history spanning over 3,000 years, with some of the most advanced hydraulic civilizations of the ancient world! 🇱🇰 Our culture is shaped by Buddhism, introduced in the 3rd century BC, and enriched by multi-ethnic trade routes connecting East and West. We boast 8 UNESCO World Heritage Sites! Ask me about Sigiriya, Anuradhapura, Galle Fort, or Dambulla to dive deeper."
            }
            else -> {
                "That is a fascinating question! 📜 Sri Lankan history and archaeology are incredibly rich. Whether it's the colossal stupas of Anuradhapura, the engineering wonders of Sigiriya, or the coastal fortifications of Galle Fort, there is always a story to tell. Ask me specifically about Sigiriya, Temple of the Tooth, Galle Fort, or Anuradhapura to get a detailed historical explanation!"
            }
        }
    }

    suspend fun clearChat() = withContext(Dispatchers.IO) {
        dao.clearLumoConversations()
    }

    override suspend fun insertUserPreferences(prefs: UserPreferencesEntity) = withContext(Dispatchers.IO) {
        dao.insertUserPreferences(prefs)
    }
}
