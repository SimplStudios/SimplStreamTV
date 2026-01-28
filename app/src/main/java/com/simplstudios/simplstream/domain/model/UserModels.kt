package com.simplstudios.simplstream.domain.model

/**
 * User profile model
 */
data class Profile(
    val id: Long,
    val name: String,
    val avatarIndex: Int,
    val hasPin: Boolean,
    val isKidsProfile: Boolean,
    val createdAt: Long
) {
    companion object {
        val AVATAR_COLORS = listOf(
            0xFF2563EB.toInt(), // SimplBlue
            0xFF7C3AED.toInt(), // Purple
            0xFFEF4444.toInt(), // Red
            0xFF10B981.toInt(), // Green
            0xFFF59E0B.toInt(), // Amber
            0xFFEC4899.toInt(), // Pink
            0xFF06B6D4.toInt(), // Cyan
            0xFF8B5CF6.toInt(), // Violet
        )
        
        fun getAvatarColor(index: Int): Int {
            return AVATAR_COLORS[index % AVATAR_COLORS.size]
        }
    }
    
    val avatarColor: Int get() = getAvatarColor(avatarIndex)
    val initial: String get() = name.firstOrNull()?.uppercaseChar()?.toString() ?: "?"
}

/**
 * Watch history item
 */
data class WatchHistory(
    val id: Long,
    val profileId: Long,
    val contentId: Int,
    val mediaType: MediaType,
    val title: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val seasonNumber: Int?,
    val episodeNumber: Int?,
    val episodeTitle: String?,
    val watchPosition: Long,
    val totalDuration: Long,
    val watchProgress: Float,
    val isCompleted: Boolean,
    val lastWatchedAt: Long
) {
    val progressPercent: Int get() = (watchProgress * 100).toInt()
    
    val episodeInfo: String? get() = if (mediaType == MediaType.TV && seasonNumber != null && episodeNumber != null) {
        "S${seasonNumber}:E${episodeNumber}"
    } else null
    
    val displayTitle: String get() = if (mediaType == MediaType.TV && episodeTitle != null) {
        "$title - $episodeTitle"
    } else {
        title
    }
    
    fun toContent(): Content = Content(
        id = contentId,
        title = title,
        overview = "",
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        voteAverage = 0f,
        releaseDate = null,
        mediaType = mediaType
    )
}

/**
 * Watchlist item
 */
data class WatchlistItem(
    val id: Long,
    val profileId: Long,
    val contentId: Int,
    val mediaType: MediaType,
    val title: String,
    val overview: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val voteAverage: Float,
    val releaseDate: String?,
    val addedAt: Long
) {
    val year: String? get() = releaseDate?.take(4)
    
    fun toContent(): Content = Content(
        id = contentId,
        title = title,
        overview = overview ?: "",
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        mediaType = mediaType
    )
}
