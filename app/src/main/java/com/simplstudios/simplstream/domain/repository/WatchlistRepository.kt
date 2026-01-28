package com.simplstudios.simplstream.domain.repository

import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    
    fun getWatchlist(profileId: Long): Flow<List<WatchlistItem>>
    
    fun getWatchlistLimited(profileId: Long, limit: Int): Flow<List<WatchlistItem>>
    
    suspend fun isInWatchlist(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): Boolean
    
    fun observeIsInWatchlist(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): Flow<Boolean>
    
    suspend fun addToWatchlist(profileId: Long, content: Content)
    
    suspend fun removeFromWatchlist(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    )
    
    /**
     * Toggle watchlist status and return new status (true = added, false = removed)
     */
    suspend fun toggleWatchlist(profileId: Long, content: Content): Boolean
    
    suspend fun clearWatchlist(profileId: Long)
    
    suspend fun getWatchlistCount(profileId: Long): Int
    
    fun observeWatchlistCount(profileId: Long): Flow<Int>
}
