package com.example.data.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class SupabaseSignUpRequest(
    val email: String,
    val password: String,
    val data: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseLoginRequest(
    val email: String,
    val password: String
)

@JsonClass(generateAdapter = true)
data class SupabaseAuthResponse(
    @Json(name = "access_token") val accessToken: String?,
    @Json(name = "user") val user: SupabaseUser?
)

@JsonClass(generateAdapter = true)
data class SupabaseUser(
    val id: String,
    val email: String?,
    @Json(name = "email_confirmed_at") val emailConfirmedAt: String? = null,
    @Json(name = "user_metadata") val userMetadata: Map<String, String>? = null
)

@JsonClass(generateAdapter = true)
data class SupabaseProfile(
    val id: String,
    val username: String,
    val email: String,
    @Json(name = "full_name") val fullName: String?,
    @Json(name = "avatar_url") val avatarUrl: String?,
    val level: Int = 1,
    val xp: Int = 0,
    val streak: Int = 0,
    @Json(name = "scans_count") val scansCount: Int = 0,
    @Json(name = "badges_count") val badgesCount: Int = 0
)

@JsonClass(generateAdapter = true)
data class SupabaseBadge(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    val code: String,
    val name: String,
    val description: String,
    val tier: String,
    val icon: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseQuest(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    val title: String,
    val description: String,
    @Json(name = "reward_xp") val rewardXp: Int,
    val progress: Int,
    val target: Int,
    val type: String,
    val completed: Boolean
)

@JsonClass(generateAdapter = true)
data class SupabaseScan(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    @Json(name = "site_name") val siteName: String,
    val province: String,
    val description: String,
    @Json(name = "unesco_status") val unescoStatus: String,
    val era: String,
    val facts: String,
    @Json(name = "xp_earned") val xpEarned: Int,
    @Json(name = "image_url") val imageUrl: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseSavedSite(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    @Json(name = "site_name") val siteName: String,
    val province: String,
    @Json(name = "image_url") val imageUrl: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseLumoMessage(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    @Json(name = "message_id") val messageId: String,
    val role: String,
    val content: String
)

@JsonClass(generateAdapter = true)
data class SupabaseReport(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    @Json(name = "content_id") val contentId: String,
    val reason: String,
    val priority: Float = 0.5f,
    val status: String = "pending",
    @Json(name = "created_at") val createdAt: String?
)

@JsonClass(generateAdapter = true)
data class SupabaseContentUpload(
    val id: String?,
    @Json(name = "user_id") val userId: String,
    @Json(name = "file_url") val fileUrl: String,
    val status: String = "pending",
    @Json(name = "created_at") val createdAt: String?
)
