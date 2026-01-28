package com.simplstudios.simplstream.domain.repository

import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.model.WatchHistory
import kotlinx.coroutines.flow.Flow

interface WatchHistoryRepository {
    
    fun getContinueWatching(profileId: Long, limit: Int = 20): Flow<List<WatchHistory>>
    
    fun getWatchHistoryForProfile(profileId: Long): Flow<List<WatchHistory>>
    
    suspend fun getWatchHistory(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): WatchHistory?
    
    fun observeWatchHistory(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): Flow<WatchHistory?>
    
    suspend fun getLastWatchedEpisode(
        profileId: Long,
        tvShowId: Int
    ): WatchHistory?
    
    suspend fun addOrUpdateWatchHistory(
        profileId: Long,
        content: Content,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        episodeTitle: String? = null,
        watchPosition: Long,
        totalDuration: Long,
        isCompleted: Boolean
    )
    
    suspend fun updateWatchProgress(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType,
        watchPosition: Long,
        totalDuration: Long,
        isCompleted: Boolean
    )
    
    suspend fun removeFromHistory(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    )
    
    suspend fun clearHistory(profileId: Long)
}
