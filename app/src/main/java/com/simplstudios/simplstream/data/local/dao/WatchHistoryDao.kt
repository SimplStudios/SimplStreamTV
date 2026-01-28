package com.simplstudios.simplstream.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.simplstudios.simplstream.data.local.entity.WatchHistoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchHistoryDao {
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId 
        ORDER BY last_watched_at DESC
    """)
    fun getWatchHistoryForProfile(profileId: Long): Flow<List<WatchHistoryEntity>>
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId AND is_completed = 0
        ORDER BY last_watched_at DESC
        LIMIT :limit
    """)
    fun getContinueWatching(profileId: Long, limit: Int = 20): Flow<List<WatchHistoryEntity>>
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND media_type = :mediaType
    """)
    suspend fun getWatchHistory(
        profileId: Long, 
        contentId: Int, 
        mediaType: String
    ): WatchHistoryEntity?
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND media_type = :mediaType
    """)
    fun observeWatchHistory(
        profileId: Long, 
        contentId: Int, 
        mediaType: String
    ): Flow<WatchHistoryEntity?>
    
    @Query("""
        SELECT * FROM watch_history 
        WHERE profile_id = :profileId 
        AND content_id = :tvShowId 
        AND media_type = 'tv'
        ORDER BY season_number DESC, episode_number DESC
        LIMIT 1
    """)
    suspend fun getLastWatchedEpisode(profileId: Long, tvShowId: Int): WatchHistoryEntity?
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWatchHistory(watchHistory: WatchHistoryEntity): Long
    
    @Update
    suspend fun updateWatchHistory(watchHistory: WatchHistoryEntity)
    
    @Delete
    suspend fun deleteWatchHistory(watchHistory: WatchHistoryEntity)
    
    @Query("""
        DELETE FROM watch_history 
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND media_type = :mediaType
    """)
    suspend fun deleteWatchHistory(profileId: Long, contentId: Int, mediaType: String)
    
    @Query("DELETE FROM watch_history WHERE profile_id = :profileId")
    suspend fun clearWatchHistoryForProfile(profileId: Long)
    
    @Query("""
        UPDATE watch_history 
        SET watch_position = :position, 
            total_duration = :duration,
            is_completed = :isCompleted,
            last_watched_at = :timestamp
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND media_type = :mediaType
    """)
    suspend fun updateWatchProgress(
        profileId: Long,
        contentId: Int,
        mediaType: String,
        position: Long,
        duration: Long,
        isCompleted: Boolean,
        timestamp: Long = System.currentTimeMillis()
    )
}
