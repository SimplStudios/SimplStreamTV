package com.simplstudios.simplstream.data.repository

import com.simplstudios.simplstream.data.local.dao.ProfileDao
import com.simplstudios.simplstream.data.local.entity.ProfileEntity
import com.simplstudios.simplstream.domain.model.Profile
import com.simplstudios.simplstream.domain.repository.ProfileRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.security.MessageDigest
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val profileDao: ProfileDao
) : ProfileRepository {
    
    override fun getAllProfiles(): Flow<List<Profile>> {
        return profileDao.getAllProfiles().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getAllProfilesOnce(): List<Profile> {
        return profileDao.getAllProfilesOnce().map { it.toDomain() }
    }
    
    override suspend fun getProfileById(id: Long): Profile? {
        return profileDao.getProfileById(id)?.toDomain()
    }
    
    override fun observeProfileById(id: Long): Flow<Profile?> {
        return profileDao.observeProfileById(id).map { it?.toDomain() }
    }
    
    override suspend fun createProfile(
        name: String,
        avatarIndex: Int,
        pin: String?,
        isKidsProfile: Boolean
    ): Long {
        val entity = ProfileEntity(
            name = name,
            avatarIndex = avatarIndex,
            pinHash = pin?.let { hashPin(it) },
            isKidsProfile = isKidsProfile
        )
        return profileDao.insertProfile(entity)
    }
    
    override suspend fun updateProfile(profile: Profile) {
        val existingEntity = profileDao.getProfileById(profile.id) ?: return
        val updatedEntity = existingEntity.copy(
            name = profile.name,
            avatarIndex = profile.avatarIndex,
            isKidsProfile = profile.isKidsProfile,
            updatedAt = System.currentTimeMillis()
        )
        profileDao.updateProfile(updatedEntity)
    }
    
    override suspend fun updateProfileDetails(
        profileId: Long,
        name: String,
        avatarIndex: Int,
        newPin: String?,
        isKidsProfile: Boolean,
        clearPin: Boolean
    ) {
        val existingEntity = profileDao.getProfileById(profileId) ?: return
        
        // Determine new PIN hash
        val newPinHash = when {
            clearPin -> null // User wants to remove PIN
            newPin != null -> hashPin(newPin) // User set a new PIN
            else -> existingEntity.pinHash // Keep existing PIN
        }
        
        val updatedEntity = existingEntity.copy(
            name = name,
            avatarIndex = avatarIndex,
            pinHash = newPinHash,
            isKidsProfile = isKidsProfile,
            updatedAt = System.currentTimeMillis()
        )
        profileDao.updateProfile(updatedEntity)
    }
    
    override suspend fun updateProfilePin(profileId: Long, newPin: String?) {
        val existingEntity = profileDao.getProfileById(profileId) ?: return
        val updatedEntity = existingEntity.copy(
            pinHash = newPin?.let { hashPin(it) },
            updatedAt = System.currentTimeMillis()
        )
        profileDao.updateProfile(updatedEntity)
    }
    
    override suspend fun deleteProfile(id: Long) {
        profileDao.deleteProfileById(id)
    }
    
    override suspend fun verifyPin(profileId: Long, pin: String): Boolean {
        val entity = profileDao.getProfileById(profileId) ?: return false
        return entity.pinHash == hashPin(pin)
    }
    
    override suspend fun hasPin(profileId: Long): Boolean {
        val entity = profileDao.getProfileById(profileId) ?: return false
        return entity.pinHash != null
    }
    
    override suspend fun getProfileCount(): Int {
        return profileDao.getProfileCount()
    }
    
    override fun observeProfileCount(): Flow<Int> {
        return profileDao.observeProfileCount()
    }
    
    private fun hashPin(pin: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(pin.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
    
    private fun ProfileEntity.toDomain(): Profile {
        return Profile(
            id = id,
            name = name,
            avatarIndex = avatarIndex,
            hasPin = pinHash != null,
            isKidsProfile = isKidsProfile,
            createdAt = createdAt
        )
    }
}
