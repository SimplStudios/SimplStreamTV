package com.simplstudios.simplstream.domain.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Media type enum
 */
enum class MediaType {
    MOVIE, TV
}

/**
 * Generic paginated content wrapper
 */
data class PagedContent<T>(
    val page: Int,
    val totalPages: Int,
    val totalResults: Int,
    val items: List<T>
) {
    val hasMore: Boolean get() = page < totalPages
}

/**
 * Base content model (movie or TV show card)
 */
data class Content(
    val id: Int,
    val title: String,
    val overview: String,
    val posterUrl: String?,
    val backdropUrl: String?,
    val voteAverage: Float,
    val releaseDate: String?,
    val mediaType: MediaType
) {
    val year: String? get() = releaseDate?.take(4)
    val ratingDisplay: String get() = "%.1f".format(voteAverage)
}

/**
 * Genre model
 */
data class Genre(
    val id: Int,
    val name: String
)

/**
 * Cast member model
 */
data class CastMember(
    val id: Int,
    val name: String,
    val character: String?,
    val profileUrl: String?
)

/**
 * Crew member model
 */
data class CrewMember(
    val id: Int,
    val name: String,
    val job: String,
    val profileUrl: String?
)

/**
 * Movie detail model
 */
data class MovieDetail(
    val id: Int,
    val imdbId: String?,
    val title: String,
    val overview: String,
    val tagline: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val voteAverage: Float,
    val voteCount: Int,
    val releaseDate: String?,
    val runtime: Int?,
    val status: String?,
    val genres: List<Genre>,
    val cast: List<CastMember>,
    val crew: List<CrewMember>,
    val recommendations: List<Content>,
    val trailerKey: String?
) {
    val year: String? get() = releaseDate?.take(4)
    val ratingDisplay: String get() = "%.1f".format(voteAverage)
    val runtimeDisplay: String? get() = runtime?.let { 
        "${it / 60}h ${it % 60}m" 
    }
    val genreDisplay: String get() = genres.take(3).joinToString(" • ") { it.name }
    val directors: List<CrewMember> get() = crew.filter { it.job == "Director" }
    
    fun toContent(): Content = Content(
        id = id,
        title = title,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        voteAverage = voteAverage,
        releaseDate = releaseDate,
        mediaType = MediaType.MOVIE
    )
}

/**
 * TV Show detail model
 */
data class TvShowDetail(
    val id: Int,
    val imdbId: String?,
    val name: String,
    val overview: String,
    val tagline: String?,
    val posterUrl: String?,
    val backdropUrl: String?,
    val voteAverage: Float,
    val voteCount: Int,
    val firstAirDate: String?,
    val lastAirDate: String?,
    val numberOfSeasons: Int,
    val numberOfEpisodes: Int,
    val episodeRunTime: Int?,
    val status: String?,
    val genres: List<Genre>,
    val seasons: List<Season>,
    val cast: List<CastMember>,
    val crew: List<CrewMember>,
    val recommendations: List<Content>,
    val trailerKey: String?
) {
    val year: String? get() = firstAirDate?.take(4)
    val ratingDisplay: String get() = "%.1f".format(voteAverage)
    val genreDisplay: String get() = genres.take(3).joinToString(" • ") { it.name }
    val seasonCountDisplay: String get() = "$numberOfSeasons Season${if (numberOfSeasons > 1) "s" else ""}"
    val creators: List<CrewMember> get() = crew.filter { it.job == "Creator" }
    
    fun toContent(): Content = Content(
        id = id,
        title = name,
        overview = overview,
        posterUrl = posterUrl,
        backdropUrl = backdropUrl,
        voteAverage = voteAverage,
        releaseDate = firstAirDate,
        mediaType = MediaType.TV
    )
}

/**
 * Season model
 */
data class Season(
    val id: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String?,
    val posterUrl: String?,
    val airDate: String?,
    val episodeCount: Int
) {
    val year: String? get() = airDate?.take(4)
}

/**
 * Season detail with episodes
 */
data class SeasonDetail(
    val id: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String?,
    val posterUrl: String?,
    val airDate: String?,
    val episodes: List<Episode>
)

/**
 * Episode model
 */
data class Episode(
    val id: Int,
    val episodeNumber: Int,
    val seasonNumber: Int,
    val name: String,
    val overview: String?,
    val stillUrl: String?,
    val airDate: String?,
    val runtime: Int?,
    val voteAverage: Float
) {
    val runtimeDisplay: String? get() = runtime?.let { "${it}m" }
    val episodeCode: String get() = "S${seasonNumber.toString().padStart(2, '0')}E${episodeNumber.toString().padStart(2, '0')}"
}

/**
 * Video server identifiers (backend)
 */
enum class VideoServerId {
    MOVIES111,   // Server Alpha
    VIDNEST,     // Server Dot (least ads)
    VIDSRC,      // Server Canteen
    VIDLINK      // Server Omega
}

/**
 * Video source for playback
 */
@Parcelize
data class VideoSource(
    val id: VideoServerId,
    val name: String,          // Backend identifier
    val displayName: String,   // User-facing name
    val url: String,
    val priority: Int,
    val isDefault: Boolean = false
) : Parcelable {
    companion object {
        // User-facing display names
        fun getDisplayName(id: VideoServerId): String = when (id) {
            VideoServerId.MOVIES111 -> "Server Alpha"
            VideoServerId.VIDNEST -> "Server Dot"
            VideoServerId.VIDSRC -> "Server Canteen"
            VideoServerId.VIDLINK -> "Server Omega"
        }
        
        // Server descriptions
        fun getDescription(id: VideoServerId): String = when (id) {
            VideoServerId.MOVIES111 -> ""
            VideoServerId.VIDNEST -> "(least ads)"
            VideoServerId.VIDSRC -> ""
            VideoServerId.VIDLINK -> ""
        }
    }
}
