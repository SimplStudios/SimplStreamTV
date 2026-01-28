package com.simplstudios.simplstream.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.simplstudios.simplstream.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    
    @Query("SELECT * FROM profiles ORDER BY created_at ASC")
    fun getAllProfiles(): Flow<List<ProfileEntity>>
    
    @Query("SELECT * FROM profiles ORDER BY created_at ASC")
    suspend fun getAllProfilesOnce(): List<ProfileEntity>
    
    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getProfileById(id: Long): ProfileEntity?
    
    @Query("SELECT * FROM profiles WHERE id = :id")
    fun observeProfileById(id: Long): Flow<ProfileEntity?>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertProfile(profile: ProfileEntity): Long
    
    @Update
    suspend fun updateProfile(profile: ProfileEntity)
    
    @Delete
    suspend fun deleteProfile(profile: ProfileEntity)
    
    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteProfileById(id: Long)
    
    @Query("SELECT COUNT(*) FROM profiles")
    suspend fun getProfileCount(): Int
    
    @Query("SELECT COUNT(*) FROM profiles")
    fun observeProfileCount(): Flow<Int>
}
