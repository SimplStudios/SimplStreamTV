package com.simplstudios.simplstream.domain.repository

import com.simplstudios.simplstream.domain.model.Profile
import kotlinx.coroutines.flow.Flow

interface ProfileRepository {
    
    fun getAllProfiles(): Flow<List<Profile>>
    
    suspend fun getAllProfilesOnce(): List<Profile>
    
    suspend fun getProfileById(id: Long): Profile?
    
    fun observeProfileById(id: Long): Flow<Profile?>
    
    suspend fun createProfile(
        name: String,
        avatarIndex: Int = 0,
        pin: String? = null,
        isKidsProfile: Boolean = false
    ): Long
    
    suspend fun updateProfile(profile: Profile)
    
    suspend fun updateProfileDetails(
        profileId: Long,
        name: String,
        avatarIndex: Int,
        newPin: String?,
        isKidsProfile: Boolean,
        clearPin: Boolean
    )
    
    suspend fun updateProfilePin(profileId: Long, newPin: String?)
    
    suspend fun deleteProfile(id: Long)
    
    suspend fun verifyPin(profileId: Long, pin: String): Boolean
    
    suspend fun hasPin(profileId: Long): Boolean
    
    suspend fun getProfileCount(): Int
    
    fun observeProfileCount(): Flow<Int>
}
