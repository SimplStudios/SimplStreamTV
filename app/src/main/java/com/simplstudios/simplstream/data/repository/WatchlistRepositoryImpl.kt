package com.simplstudios.simplstream.data.repository

import com.simplstudios.simplstream.data.local.dao.WatchlistDao
import com.simplstudios.simplstream.data.local.entity.WatchlistEntity
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.model.WatchlistItem
import com.simplstudios.simplstream.domain.repository.WatchlistRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WatchlistRepositoryImpl @Inject constructor(
    private val watchlistDao: WatchlistDao
) : WatchlistRepository {
    
    override fun getWatchlist(profileId: Long): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistForProfile(profileId).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getWatchlistLimited(profileId: Long, limit: Int): Flow<List<WatchlistItem>> {
        return watchlistDao.getWatchlistForProfileLimited(profileId, limit).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override suspend fun isInWatchlist(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): Boolean {
        return watchlistDao.isInWatchlist(
            profileId = profileId,
            contentId = contentId,
            mediaType = mediaType.name.lowercase()
        )
    }
    
    override fun observeIsInWatchlist(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ): Flow<Boolean> {
        return watchlistDao.observeIsInWatchlist(
            profileId = profileId,
            contentId = contentId,
            mediaType = mediaType.name.lowercase()
        )
    }
    
    override suspend fun addToWatchlist(profileId: Long, content: Content) {
        val entity = WatchlistEntity(
            profileId = profileId,
            contentId = content.id,
            mediaType = content.mediaType.name.lowercase(),
            title = content.title,
            overview = content.overview,
            posterPath = content.posterUrl?.substringAfterLast("/"),
            backdropPath = content.backdropUrl?.substringAfterLast("/"),
            voteAverage = content.voteAverage,
            releaseDate = content.releaseDate
        )
        watchlistDao.addToWatchlist(entity)
    }
    
    override suspend fun removeFromWatchlist(
        profileId: Long,
        contentId: Int,
        mediaType: MediaType
    ) {
        watchlistDao.removeFromWatchlist(
            profileId = profileId,
            contentId = contentId,
            mediaType = mediaType.name.lowercase()
        )
    }
    
    override suspend fun toggleWatchlist(profileId: Long, content: Content): Boolean {
        val isInWatchlist = isInWatchlist(profileId, content.id, content.mediaType)
        if (isInWatchlist) {
            removeFromWatchlist(profileId, content.id, content.mediaType)
        } else {
            addToWatchlist(profileId, content)
        }
        return !isInWatchlist
    }
    
    override suspend fun clearWatchlist(profileId: Long) {
        watchlistDao.clearWatchlistForProfile(profileId)
    }
    
    override suspend fun getWatchlistCount(profileId: Long): Int {
        return watchlistDao.getWatchlistCount(profileId)
    }
    
    override fun observeWatchlistCount(profileId: Long): Flow<Int> {
        return watchlistDao.observeWatchlistCount(profileId)
    }
    
    private fun WatchlistEntity.toDomain(): WatchlistItem {
        return WatchlistItem(
            id = id,
            profileId = profileId,
            contentId = contentId,
            mediaType = if (mediaType == "movie") MediaType.MOVIE else MediaType.TV,
            title = title,
            overview = overview,
            posterUrl = posterPath?.let { "https://image.tmdb.org/t/p/w500/$it" },
            backdropUrl = backdropPath?.let { "https://image.tmdb.org/t/p/w1280/$it" },
            voteAverage = voteAverage,
            releaseDate = releaseDate,
            addedAt = addedAt
        )
    }
}
