package com.simplstudios.simplstream.data.repository

import com.simplstudios.simplstream.data.local.dao.WatchHistoryDao
import com.simplstudios.simplstream.data.local.entity.WatchHistoryEntity
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.model.WatchHistory
import com.simplstudios.simplstream.domain.repository.WatchHistoryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchHistoryRepositoryImpl @Inject constructor(
    private val watchHistoryDao: WatchHistoryDao
) : WatchHistoryRepository {
    
    override fun getContinueWatching(profileId: Long, limit: Int): Flow<List<WatchHistory>> {
        return watchHistoryDao.getContinueWatching(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getWatchHistoryForProfile(profileId: Long): Flow<List<WatchHistory>> {
        return watchHistoryDao.getWatchHistoryForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun getWatchHistory(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): WatchHistory? {
        return watchHistoryDao.getWatchHistory(
            profileId = profileId,
            contentId = contentId,
            mediaType = mediaType.name.lowercase()
        )?.toDomain()
    }
    
    override fun observeWatchHistory(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): Flow<WatchHistory?> {
        return watchHistoryDao.observeWatchHistory(
            profileId = profileId,
            contentId = contentId,
            mediaType = mediaType.name.lowercase()
        ).map { it?.toDomain() }
    }
    
    override suspend fun getLastWatchedEpisode(
        profileId: Long,
        tvShowId: Int
    ): WatchHistory? {
        return watchHistoryDao.getLastWatchedEpisode(profileId, tvShowId)?.toDomain()
    }
    
    override suspend fun addOrUpdateWatchHistory(
        profileId: Long,
        content: Content,
        seasonNumber: Int?,
        episodeNumber: Int?,
        episodeTitle: String?,
        watchPosition: Long,
        totalDuration: Long,
        isCompleted: Boolean
    ) {
        val existing = watchHistoryDao.getWatchHistory(
            profileId = profileId,
            contentId = content.id,
            mediaType = content.mediaType.name.lowercase()
        )
        
        val entity = WatchHistoryEntity(
            id = existing?.id ?: 0,
            profileId = profileId,
            contentId = content.id,
            mediaType = content.mediaType.name.lowercase(),
            title = content.title,
            posterPath = content.posterUrl?.substringAfterLast("/"),
            backdropPath = content.backdropUrl?.substringAfterLast("/"),
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            watchPosition = watchPosition,
            totalDuration = totalDuration,
            isCompleted = isCompleted,
            lastWatchedAt = System.currentTimeMillis()
        )
        
        watchHistoryDao.insertWatchHistory(entity)
    }
    
    override suspend fun updateWatchProgress(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType,
        watchPosition: Long,
        totalDuration: Long,
        isCompleted: Boolean
    ) {
        watchHistoryDao.updateWatchProgress(
            profileId = profileId,
            contentId = contentId,
            mediaType = mediaType.name.lowercase(),
            position = watchPosition,
            duration = totalDuration,
            isCompleted = isCompleted
        )
    }
    
    override suspend fun removeFromHistory(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ) {
        watchHistoryDao.deleteWatchHistory(
            profileId = profileId,
            contentId = contentId,
            mediaType = mediaType.name.lowercase()
        )
    }
    
    override suspend fun clearHistory(profileId: Long) {
        watchHistoryDao.clearWatchHistoryForProfile(profileId)
    }
    
    private fun WatchHistoryEntity.toDomain(): WatchHistory {
        return WatchHistory(
            id = id,
            profileId = profileId,
            contentId = contentId,
            mediaType = if (mediaType == "movie") MediaType.MOVIE else MediaType.TV,
            title = title,
            posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500/$it" },
            backdropUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280/$it" },
            seasonNumber = seasonNumber,
            episodeNumber = episodeNumber,
            episodeTitle = episodeTitle,
            watchPosition = watchPosition,
            totalDuration = totalDuration,
            watchProgress = watchProgress,
            isCompleted = isCompleted,
            lastWatchedAt = lastWatchedAt
        )
    }
}
