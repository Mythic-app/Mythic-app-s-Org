package com.example.data.remote

import com.example.BuildConfig
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.*
import java.util.concurrent.TimeUnit

interface SupabaseApiService {
    
    // --- AUTH ---
    @POST("auth/v1/signup")
    suspend fun signUp(
        @Body request: SupabaseSignUpRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/token?grant_type=password")
    suspend fun login(
        @Body request: SupabaseLoginRequest
    ): Response<SupabaseAuthResponse>

    @POST("auth/v1/logout")
    suspend fun logout(): Response<Unit>

    @POST("auth/v1/recover")
    suspend fun recoverPassword(
        @Body body: Map<String, String>
    ): Response<Unit>

    @GET("auth/v1/user")
    suspend fun getUserDetails(
        @Header("Authorization") bearerToken: String
    ): Response<SupabaseUser>

    // --- PROFILES ---
    @GET("rest/v1/profiles")
    suspend fun getProfile(
        @Query("id") query: String
    ): Response<List<SupabaseProfile>>

    @PATCH("rest/v1/profiles")
    suspend fun updateProfile(
        @Query("id") query: String,
        @Body profile: Map<String, Any>
    ): Response<Unit>

    // --- BADGES ---
    @GET("rest/v1/badges")
    suspend fun getBadges(
        @Query("user_id") query: String
    ): Response<List<SupabaseBadge>>

    @POST("rest/v1/badges")
    suspend fun saveBadge(
        @Body badge: SupabaseBadge
    ): Response<Unit>

    // --- QUESTS ---
    @GET("rest/v1/quests")
    suspend fun getQuests(
        @Query("user_id") query: String
    ): Response<List<SupabaseQuest>>

    @POST("rest/v1/quests")
    suspend fun saveQuests(
        @Body quests: List<SupabaseQuest>
    ): Response<Unit>

    @PATCH("rest/v1/quests")
    suspend fun updateQuest(
        @Query("id") query: String,
        @Body updates: Map<String, Any>
    ): Response<Unit>

    // --- SCAN HISTORY ---
    @GET("rest/v1/scan_history")
    suspend fun getScanHistory(
        @Query("user_id") query: String
    ): Response<List<SupabaseScan>>

    @POST("rest/v1/scan_history")
    suspend fun saveScan(
        @Body scan: SupabaseScan
    ): Response<Unit>

    // --- SAVED SITES ---
    @GET("rest/v1/saved_sites")
    suspend fun getSavedSites(
        @Query("user_id") query: String
    ): Response<List<SupabaseSavedSite>>

    @POST("rest/v1/saved_sites")
    suspend fun saveSite(
        @Body site: SupabaseSavedSite
    ): Response<Unit>

    @DELETE("rest/v1/saved_sites")
    suspend fun deleteSavedSite(
        @Query("user_id") query: String,
        @Query("site_name") siteNameQuery: String
    ): Response<Unit>

    // --- LUMO ---
    @GET("rest/v1/lumo_conversations")
    suspend fun getLumoConversations(
        @Query("user_id") query: String
    ): Response<List<SupabaseLumoMessage>>

    @POST("rest/v1/lumo_conversations")
    suspend fun saveLumoMessage(
        @Body message: SupabaseLumoMessage
    ): Response<Unit>

    @POST("rest/v1/reports")
    suspend fun saveReport(
        @Body report: SupabaseReport
    ): Response<Unit>

    @GET("rest/v1/reports")
    suspend fun getReports(): Response<List<SupabaseReport>>
}

object SupabaseClient {
    private val supabaseUrl = BuildConfig.SUPABASE_URL.trim().removeSuffix("/")
    private val supabaseKey = BuildConfig.SUPABASE_KEY.trim()

    val isConfigured: Boolean
        get() = supabaseUrl.isNotEmpty() && 
                supabaseUrl != "https://your-project.supabase.co" && 
                supabaseKey.isNotEmpty() && 
                supabaseKey != "your-supabase-anon-key"

    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient: OkHttpClient by lazy {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()
            
            // Build absolute URL if needed or modify headers
            val builder = originalRequest.newBuilder()
                .header("apikey", supabaseKey)
                .header("Authorization", "Bearer $supabaseKey")
                .header("Content-Type", "application/json")
                .header("Prefer", "return=representation")

            chain.proceed(builder.build())
        }

        OkHttpClient.Builder()
            .addInterceptor(authInterceptor)
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    val service: SupabaseApiService by lazy {
        val baseUrl = if (isConfigured) "$supabaseUrl/" else "https://placeholder-supabase.co/"
        Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(SupabaseApiService::class.java)
    }
}
