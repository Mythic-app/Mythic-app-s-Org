package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.BuildConfig
import com.example.data.local.*
import com.example.data.remote.GeminiClient
import com.example.data.remote.GeminiContent
import com.example.data.remote.GeminiPart
import com.example.data.remote.GeminiRequest
import com.example.data.remote.HeritageArticle
import com.example.data.repository.MythicRepository
import com.example.ui.theme.MythicThemeState
import com.example.ui.theme.ThemeMode
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.delay
import com.example.data.remote.GeminiInlineData
import com.example.data.remote.SupabaseReport
import org.json.JSONObject

class MythicViewModel(application: Application) : AndroidViewModel(application) {
    val repository = MythicRepository(application)

    // --- State Streams from Repository ---
    val currentUser = repository.currentUser
    val profile = repository.profile.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )
    val badges = repository.badges.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val quests = repository.quests.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val scanHistory = repository.scanHistory.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val savedSites = repository.savedSites.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )
    val lumoMessages = repository.lumoMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Realtime Feed Articles ---
    private val _feedArticles = MutableStateFlow<List<HeritageArticle>>(emptyList())
    val feedArticles = _feedArticles.asStateFlow()

    private val _isGeneratingArticle = MutableStateFlow(false)
    val isGeneratingArticle = _isGeneratingArticle.asStateFlow()

    private val _generationError = MutableStateFlow<String?>(null)
    val generationError = _generationError.asStateFlow()

    // --- UI Controls ---
    private val _isLumoTyping = MutableStateFlow(false)
    val isLumoTyping = _isLumoTyping.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanHistoryEntity?>(null)
    val scanResult = _scanResult.asStateFlow()

    private val _showScanResultDialog = MutableStateFlow(false)
    val showScanResultDialog = _showScanResultDialog.asStateFlow()

    private val _activeFeedTab = MutableStateFlow("All")
    val activeFeedTab = _activeFeedTab.asStateFlow()

    private val _activeQuestTab = MutableStateFlow("Daily")
    val activeQuestTab = _activeQuestTab.asStateFlow()

    private val _isLoggingIn = MutableStateFlow(false)
    val isLoggingIn = _isLoggingIn.asStateFlow()

    private val _authError = MutableStateFlow<String?>(null)
    val authError = _authError.asStateFlow()

    private val _verificationSentEmail = MutableStateFlow<String?>(null)
    val verificationSentEmail = _verificationSentEmail.asStateFlow()

    init {
        // Preseed articles with local high-quality drawables for offline loading and dynamic URL fallbacks
        _feedArticles.value = listOf(
            HeritageArticle(
                id = "art_sigiriya",
                siteName = "Sigiriya Rock Fortress",
                province = "Central Province",
                description = "Sigiriya is an ancient rock fortress and palace built by King Kashyapa in the 5th century AD. Rising 200 meters above the forest, it features stunning frescoes, a mirror wall with historic graffiti, and the monumental Lion Gate.",
                imageUrl = "img_sigiriya",
                category = "Ancient Cities",
                unescoStatus = "UNESCO World Heritage Site",
                era = "5th Century AD",
                facts = "Mirror Wall; Lion's Paw gate; Water gardens",
                initialLikes = 125
            ),
            HeritageArticle(
                id = "art_kandy",
                siteName = "Temple of the Tooth",
                province = "Central Province",
                description = "The Sri Dalada Maligawa in Kandy is a golden-roofed Buddhist temple complex housing the sacred tooth relic of Gautama Buddha. Located in the royal palace complex of the former Kingdom of Kandy, it remains a supreme spiritual sanctuary.",
                imageUrl = "img_kandy",
                category = "Religious",
                unescoStatus = "UNESCO World Heritage Site",
                era = "16th Century AD",
                facts = "Houses the sacred tooth relic; Located in the historic Kandy Royal Palace; Features stunning Kandyan architecture",
                initialLikes = 142
            ),
            HeritageArticle(
                id = "art_galle",
                siteName = "Galle Fort Citadel",
                province = "Southern Province",
                description = "Galle Fort is a historic fortified old town built by the Portuguese in 1588 and extensively fortified by the Dutch in the 17th century. Blending European architectural styles and South Asian traditions, it stands on a beautiful coastal peninsula.",
                imageUrl = "img_galle",
                category = "Architecture",
                unescoStatus = "UNESCO World Heritage Site",
                era = "16th - 17th Century",
                facts = "Multi-cultural architectural style; Iconic Galle Lighthouse stands on the ramparts; Still is a living community",
                initialLikes = 98
            ),
            HeritageArticle(
                id = "art_anuradhapura",
                siteName = "Sacred Anuradhapura",
                province = "North Central Province",
                description = "Anuradhapura is one of the ancient capitals of Sri Lanka, famous for its exceptionally preserved ruins of an ancient Sinhalese civilization. It is home to massive ancient stupas like Ruwanwelisaya and the sacred Jaya Sri Maha Bodhi tree.",
                imageUrl = "img_anuradhapura",
                category = "Ancient Cities",
                unescoStatus = "UNESCO World Heritage Site",
                era = "3rd Century BC",
                facts = "Home to the sacred Jaya Sri Maha Bodhi tree; Massive dome of Ruwanwelisaya; Exceptionally advanced ancient water tanks",
                initialLikes = 110
            ),
            HeritageArticle(
                id = "art_dambulla",
                siteName = "Dambulla Cave Temple",
                province = "Central Province",
                description = "Dambulla is the largest and best-preserved cave temple complex in Sri Lanka. Five major caves house over 150 stunning statues of Buddha, Sri Lankan kings, and gods, beneath exquisite, vibrant rock ceiling paintings.",
                imageUrl = "https://images.unsplash.com/photo-1608958416744-8846c071d2b0?w=800",
                category = "Religious",
                unescoStatus = "UNESCO World Heritage Site",
                era = "1st Century BC",
                facts = "Largest and best-preserved cave temple; Features over 150 statues; Exceptional rock wall murals",
                initialLikes = 85
            ),
            HeritageArticle(
                id = "art_polonnaruwa",
                siteName = "Ancient Polonnaruwa",
                province = "North Central Province",
                description = "The second oldest kingdom of Sri Lanka, Polonnaruwa features the monumental ruins of the garden city built in the 12th century. Iconic monuments include the Gal Vihara rock temple, the Royal Palace, and the Quadrangle.",
                imageUrl = "https://images.unsplash.com/photo-1578593139888-39620e59c164?w=800",
                category = "Ancient Cities",
                unescoStatus = "UNESCO World Heritage Site",
                era = "12th Century AD",
                facts = "Iconic rock-cut Buddha statues of Gal Vihara; Unique circular relic house (Vatadage); Masterful ancient Parakrama Samudra reservoir",
                initialLikes = 92
            ),
            HeritageArticle(
                id = "art_jaffna",
                siteName = "Jaffna Fort",
                province = "Northern Province",
                description = "Built by the Portuguese in 1618 and later expanded by the Dutch, the Jaffna Fort stands as an impressive star-shaped limestone fortress overlooking the Jaffna lagoon. It represents a vital monument of the northern peninsula's rich history.",
                imageUrl = "https://images.unsplash.com/photo-1620121692029-d088224ddc74?w=800",
                category = "Architecture",
                unescoStatus = "Protected Cultural Monument",
                era = "17th Century",
                facts = "Built entirely using coral and limestone; Star-shaped Dutch fortress engineering; Overlooks the Jaffna Lagoon",
                initialLikes = 74
            )
        )

        viewModelScope.launch {
            repository.checkSession()
            // Observe preferences to apply theme
            repository.preferences.collect { prefs ->
                if (prefs != null) {
                    MythicThemeState.themeMode = if (prefs.darkMode) ThemeMode.DARK else ThemeMode.LIGHT
                }
            }
        }
    }

    // --- AUTH ACTIONS ---
    fun signUp(username: String, email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _authError.value = null
            _verificationSentEmail.value = null
            val error = repository.signUp(username, email, password)
            _isLoggingIn.value = false
            if (error == "VERIFICATION_SENT" || error == "VERIFICATION_SENT_MOCK") {
                _verificationSentEmail.value = email
            } else if (error == null) {
                onSuccess()
            } else {
                _authError.value = error
            }
        }
    }

    fun login(email: String, password: String, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _authError.value = null
            val error = repository.login(email, password)
            _isLoggingIn.value = false
            if (error == null) {
                onSuccess()
            } else if (error == "EMAIL_NOT_CONFIRMED") {
                _authError.value = "Please confirm your email before logging in. We have sent a verification email to your address."
            } else {
                _authError.value = error
            }
        }
    }

    fun recoverPassword(email: String, onSent: () -> Unit) {
        viewModelScope.launch {
            _isLoggingIn.value = true
            _authError.value = null
            val success = repository.recoverPassword(email)
            _isLoggingIn.value = false
            if (success) {
                onSent()
            } else {
                _authError.value = "Failed to send recovery email. Please check your internet connection."
            }
        }
    }

    fun clearVerificationSent() {
        _verificationSentEmail.value = null
    }

    fun logout(onSuccess: () -> Unit) {
        viewModelScope.launch {
            repository.logout()
            onSuccess()
        }
    }

    // --- THEME SWITCHER ---
    fun toggleTheme(darkMode: Boolean) {
        viewModelScope.launch {
            val mode = if (darkMode) ThemeMode.DARK else ThemeMode.LIGHT
            MythicThemeState.themeMode = mode
            val userId = currentUser.value?.id ?: "guest"
            repository.insertUserPreferences(
                UserPreferencesEntity(userId, darkMode, true, "en")
            )
        }
    }

    // --- SAVE / UNSAVE ---
    fun toggleSaveSite(siteName: String, province: String, imageUrl: String?) {
        viewModelScope.launch {
            repository.toggleSaveSite(siteName, province, imageUrl)
        }
    }

    // --- TRIGGER SCAN ---
    private val _isScanningWithVision = MutableStateFlow(false)
    val isScanningWithVision = _isScanningWithVision.asStateFlow()

    private val _visionScanError = MutableStateFlow<String?>(null)
    val visionScanError = _visionScanError.asStateFlow()

    fun scanWithGeminiVision(base64Image: String, mimeType: String, defaultSiteName: String, fallbackImageUrl: String) {
        viewModelScope.launch {
            _isScanningWithVision.value = true
            _visionScanError.value = null
            
            val isGeminiConfigured = GeminiClient.isConfigured
            if (isGeminiConfigured) {
                try {
                    val prompt = """
                        Analyze this image. It is a photograph of a historical, ancient, or cultural heritage monument or site in Sri Lanka. 
                        Identify the site name, which province of Sri Lanka it is in, a brief 2-3 sentence historical profile description, and its UNESCO status (either 'UNESCO World Heritage Site' or 'Protected Cultural Monument'), its historical era (e.g. 5th Century AD, 12th Century AD, etc.), and 3 interesting facts.
                        
                        You MUST respond with a single raw JSON object in this exact format:
                        {
                          "siteName": "Name of the site",
                          "province": "Province name",
                          "description": "Historical description",
                          "unescoStatus": "UNESCO World Heritage Site or Protected Cultural Monument",
                          "era": "Era name",
                          "facts": "Fact 1; Fact 2; Fact 3"
                        }
                        
                        Do NOT wrap inside markdown code blocks, output only the raw JSON.
                    """.trimIndent()
                    
                    val request = GeminiRequest(
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(
                                    GeminiPart(text = prompt),
                                    GeminiPart(inlineData = GeminiInlineData(mimeType = mimeType, data = base64Image))
                                )
                            )
                        )
                    )
                    
                    val response = GeminiClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                    if (response.isSuccessful && response.body() != null) {
                        val text = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                        if (!text.isNullOrBlank()) {
                            val jsonStart = text.indexOf("{")
                            val jsonEnd = text.lastIndexOf("}")
                            if (jsonStart != -1 && jsonEnd != -1) {
                                val jsonStr = text.substring(jsonStart, jsonEnd + 1)
                                val json = JSONObject(jsonStr)
                                val siteName = json.optString("siteName", defaultSiteName)
                                val province = json.optString("province", "Central Province")
                                val desc = json.optString("description", "A historical marvel")
                                val unesco = json.optString("unescoStatus", "Protected Cultural Monument")
                                val era = json.optString("era", "Ancient Era")
                                val facts = json.optString("facts", "Rich historical heritage")
                                
                                val scan = repository.saveCustomScan(
                                    siteName = siteName,
                                    province = province,
                                    description = desc,
                                    unescoStatus = unesco,
                                    era = era,
                                    facts = facts,
                                    imageUrl = fallbackImageUrl
                                )
                                _scanResult.value = scan
                                _showScanResultDialog.value = true
                                _isScanningWithVision.value = false
                                return@launch
                            }
                        }
                    }
                    _visionScanError.value = "Unable to analyze image. Please try again with a clearer picture."
                } catch (e: Exception) {
                    _visionScanError.value = "Error during analysis: ${e.message}"
                }
            } else {
                _visionScanError.value = "Gemini API is not configured. Please add your GEMINI_API_KEY to the Secrets panel."
            }
            
            // Fallback to offline scan simulation on error or if Gemini is not configured
            delay(2000)
            triggerScan(defaultSiteName, fallbackImageUrl)
            _isScanningWithVision.value = false
        }
    }

    fun triggerScan(siteName: String, imageUrl: String?) {
        viewModelScope.launch {
            val result = repository.scanHeritage(siteName, imageUrl)
            _scanResult.value = result
            _showScanResultDialog.value = true
        }
    }

    fun dismissScanResult() {
        _showScanResultDialog.value = false
        _scanResult.value = null
    }

    // --- LUMO MESSAGING ---
    fun sendLumoMessage(content: String) {
        if (content.isBlank()) return
        viewModelScope.launch {
            _isLumoTyping.value = true
            repository.sendLumoMessage(content)
            _isLumoTyping.value = false
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
        }
    }

    // --- SETTERS ---
    fun setFeedTab(tab: String) {
        _activeFeedTab.value = tab
    }

    fun setQuestTab(tab: String) {
        _activeQuestTab.value = tab
    }

    fun triggerArticleRead() {
        viewModelScope.launch {
            repository.incrementQuestProgress("read")
        }
    }

    fun triggerArticleLike() {
        viewModelScope.launch {
            repository.incrementQuestProgress("like")
        }
    }

    private val _recentReports = MutableStateFlow<List<SupabaseReport>>(emptyList())
    val recentReports: StateFlow<List<SupabaseReport>> = _recentReports

    // --- REPORTING ---
    fun submitReport(report: SupabaseReport) {
        viewModelScope.launch {
            repository.submitReport(report)
            fetchRecentReports()
        }
    }

    fun fetchRecentReports() {
        viewModelScope.launch {
            _recentReports.value = repository.getRecentReports()
        }
    }

    // --- REAL-TIME AI ARTICLE GENERATION ---
    fun generateRealtimeArticle(query: String) {
        if (query.isBlank()) return
        viewModelScope.launch {
            _isGeneratingArticle.value = true
            _generationError.value = null
            
            val isGeminiConfigured = GeminiClient.isConfigured
            var newArticle: HeritageArticle? = null
            
            if (isGeminiConfigured) {
                try {
                    val prompt = """
                        Generate a detailed, authentic historical profile for a Sri Lankan ancient heritage site, monument, temple, fort, or historical topic matching search term: "$query".
                        Your response MUST be a single raw JSON object with the exact following fields:
                        {
                          "siteName": "Name of the site",
                          "province": "Which Sri Lankan province (e.g., Central, Southern, North Central, Northern, Western, Sabaragamuwa, Uva, North Western, Eastern)",
                          "description": "A rich 3-4 sentence historical and archaeological description",
                          "category": "One of: 'Architecture', 'Ancient Cities', 'Religious'",
                          "unescoStatus": "UNESCO World Heritage Site or Protected Cultural Monument",
                          "era": "The historical era or century (e.g., 3rd Century BC, 5th Century AD, 12th Century AD, etc.)",
                          "facts": "Three highly interesting, verified historical facts about the site, separated strictly by semicolons (;) without numbering"
                        }
                        Important: Do NOT wrap the JSON inside markdown code blocks (e.g., ```json ... ```) or any other formatting. Output ONLY the raw JSON string.
                    """.trimIndent()
                    
                    val request = GeminiRequest(
                        contents = listOf(
                            GeminiContent(
                                role = "user",
                                parts = listOf(GeminiPart(text = prompt))
                            )
                        )
                    )
                    
                    val response = GeminiClient.service.generateContent(BuildConfig.GEMINI_API_KEY, request)
                    val responseBody = response.body()
                    val responseText = responseBody?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                    
                    if (!responseText.isNullOrBlank()) {
                        var cleanJson = responseText.trim()
                        if (cleanJson.startsWith("```")) {
                            cleanJson = cleanJson.removePrefix("```json").removePrefix("```").removeSuffix("```").trim()
                        }
                        
                        val json = JSONObject(cleanJson)
                        val siteName = json.getString("siteName")
                        val province = json.getString("province")
                        val description = json.getString("description")
                        val category = json.getString("category")
                        val unescoStatus = json.optString("unescoStatus", "Protected Cultural Monument")
                        val era = json.optString("era", "Ancient Era")
                        val facts = json.optString("facts", "Rich historical significance; Ancient Sri Lankan design; Cultural landmark")
                        
                        val artId = "art_dynamic_" + System.currentTimeMillis()
                        val imageUrl = "https://images.unsplash.com/featured/?srilanka,heritage,${siteName.replace(" ", "")}"
                        
                        newArticle = HeritageArticle(
                            id = artId,
                            siteName = siteName,
                            province = province,
                            description = description,
                            imageUrl = imageUrl,
                            category = if (category in listOf("Architecture", "Ancient Cities", "Religious")) category else "Ancient Cities",
                            unescoStatus = unescoStatus,
                            era = era,
                            facts = facts,
                            initialLikes = (30..150).random()
                        )
                    }
                } catch (e: Exception) {
                    // Fallback to local offline template on error
                }
            }
            
            if (newArticle == null) {
                newArticle = getOfflineGeneratedArticle(query)
            }
            
            // Prepend new article to the top of the feedArticles list!
            _feedArticles.value = listOf(newArticle!!) + _feedArticles.value
            _isGeneratingArticle.value = false
            
            // Increment quest progress for generation/reading!
            repository.incrementQuestProgress("read")
        }
    }

    private fun getOfflineGeneratedArticle(query: String): HeritageArticle {
        val q = query.lowercase().trim()
        return when {
            q.contains("mihintale") -> HeritageArticle(
                id = "art_mihintale",
                siteName = "Mihintale Sanctuary",
                province = "North Central Province",
                description = "Mihintale is a mountain peak near Anuradhapura, believed to be the site of a historic meeting between the Buddhist monk Mahinda and King Devanampiyatissa, which inaugurated the presence of Buddhism in Sri Lanka. It is a revered pilgrimage site with ancient stupas and rock carvings.",
                imageUrl = "https://images.unsplash.com/featured/?srilanka,mihintale",
                category = "Religious",
                unescoStatus = "Protected Cultural Sanctuary",
                era = "3rd Century BC",
                facts = "Considered the cradle of Buddhism in Sri Lanka; Houses the Aradhana Gala rock; Home to one of the world's oldest medical baths",
                initialLikes = 87
            )
            q.contains("pidurangala") -> HeritageArticle(
                id = "art_pidurangala",
                siteName = "Pidurangala Rock",
                province = "Central Province",
                description = "Pidurangala is a massive rock formation situated a few kilometers north of Sigiriya. It was home to a Buddhist monastery dating back to the 5th century AD, which was heavily supported by King Kashyapa during his reign. It offers a panoramic 360-degree view of Sigiriya Rock.",
                imageUrl = "https://images.unsplash.com/featured/?srilanka,pidurangala",
                category = "Architecture",
                unescoStatus = "Protected Monument",
                era = "5th Century AD",
                facts = "Used as an ancient Buddhist monastery; Houses a massive reclining Buddha statue carved under a cave; Provides the best vantage point for Sigiriya",
                initialLikes = 104
            )
            q.contains("ritigala") -> HeritageArticle(
                id = "art_ritigala",
                siteName = "Ritigala Forest Monastery",
                province = "North Central Province",
                description = "Ritigala is an ancient Buddhist monastery and mountain range in northern Sri Lanka. The ruins are nestled deep in a strictly protected natural reserve and showcase exceptional stone masonry, ancient paths, and monastic bathing pools with no religious carvings, indicating a unique aesthetic.",
                imageUrl = "https://images.unsplash.com/featured/?srilanka,ritigala",
                category = "Ancient Cities",
                unescoStatus = "Strict Nature Reserve & Protected Monument",
                era = "1st Century BC",
                facts = "Exceptional double-platform stone bridges; Features unique medicinal herb forests; Mentioned in the epic Ramayana legend",
                initialLikes = 93
            )
            q.contains("yapahuwa") -> HeritageArticle(
                id = "art_yapahuwa",
                siteName = "Yapahuwa Rock Fortress",
                province = "North West Province",
                description = "Yapahuwa was one of the ephemeral medieval capitals of Sri Lanka in the late 13th century. Built on a massive 90-meter granite rock, it served as a palace-fortress housing the Sacred Tooth Relic. It is famous for its stunning, steep ornamental stone stairway with intricate carvings.",
                imageUrl = "https://images.unsplash.com/featured/?srilanka,yapahuwa",
                category = "Architecture",
                unescoStatus = "Protected Cultural Citadel",
                era = "13th Century AD",
                facts = "Home to the iconic Chinese-style Yapahuwa Lion carvings; Protected the Tooth Relic during a South Indian invasion; Boasts a steep rock-cut ornamental staircase",
                initialLikes = 79
            )
            else -> {
                val capitalized = query.split(" ").joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                HeritageArticle(
                    id = "art_dynamic_gen_" + System.currentTimeMillis(),
                    siteName = capitalized,
                    province = "Sri Lanka",
                    description = "A magnificent historical site in Sri Lanka filled with centuries of deep cultural heritage, ancient craftsmanship, and sacred archaeological discoveries. It stands as a testament to the island's glorious engineering and spiritual longevity.",
                    imageUrl = "https://images.unsplash.com/featured/?srilanka,heritage,history",
                    category = "Ancient Cities",
                    unescoStatus = "Protected Cultural Monument",
                    era = "Ancient Era",
                    facts = "Rich archaeological discoveries; Showcases exceptional historic architectural engineering; Embedded deep in the island's local folklore",
                    initialLikes = (25..95).random()
                )
            }
        }
    }
}
