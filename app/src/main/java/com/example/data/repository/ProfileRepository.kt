package com.example.data.repository

import com.example.data.local.ProfileEntity
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    val profile: Flow<ProfileEntity?>
    suspend fun getProfile(): ProfileEntity?
    suspend fun insertProfile(profile: ProfileEntity)
    suspend fun updateStreak(userId: String)
    suspend fun clearProfile()
}
