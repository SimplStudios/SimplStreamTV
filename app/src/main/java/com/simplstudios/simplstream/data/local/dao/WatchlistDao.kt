package com.simplstudios.simplstream.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.simplstudios.simplstream.data.local.entity.WatchlistEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WatchlistDao {
    
    @Query("""
        SELECT * FROM watchlist 
        WHERE profile_id = :profileId 
        ORDER BY added_at DESC
    """)
    fun getWatchlistForProfile(profileId: Long): Flow<List<WatchlistEntity>>
    
    @Query("""
        SELECT * FROM watchlist 
        WHERE profile_id = :profileId 
        ORDER BY added_at DESC
        LIMIT :limit
    """)
    fun getWatchlistForProfileLimited(profileId: Long, limit: Int): Flow<List<WatchlistEntity>>
    
    @Query("""
        SELECT * FROM watchlist 
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND media_type = :mediaType
    """)
    suspend fun getWatchlistItem(
        profileId: Long, 
        contentId: Int, 
        mediaType: String
    ): WatchlistEntity?
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM watchlist 
            WHERE profile_id = :profileId 
            AND content_id = :contentId 
            AND media_type = :mediaType
        )
    """)
    suspend fun isInWatchlist(
        profileId: Long, 
        contentId: Int, 
        mediaType: String
    ): Boolean
    
    @Query("""
        SELECT EXISTS(
            SELECT 1 FROM watchlist 
            WHERE profile_id = :profileId 
            AND content_id = :contentId 
            AND media_type = :mediaType
        )
    """)
    fun observeIsInWatchlist(
        profileId: Long, 
        contentId: Int, 
        mediaType: String
    ): Flow<Boolean>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addToWatchlist(item: WatchlistEntity): Long
    
    @Delete
    suspend fun removeFromWatchlist(item: WatchlistEntity)
    
    @Query("""
        DELETE FROM watchlist 
        WHERE profile_id = :profileId 
        AND content_id = :contentId 
        AND media_type = :mediaType
    """)
    suspend fun removeFromWatchlist(profileId: Long, contentId: Int, mediaType: String)
    
    @Query("DELETE FROM watchlist WHERE profile_id = :profileId")
    suspend fun clearWatchlistForProfile(profileId: Long)
    
    @Query("SELECT COUNT(*) FROM watchlist WHERE profile_id = :profileId")
    suspend fun getWatchlistCount(profileId: Long): Int
    
    @Query("SELECT COUNT(*) FROM watchlist WHERE profile_id = :profileId")
    fun observeWatchlistCount(profileId: Long): Flow<Int>
}
