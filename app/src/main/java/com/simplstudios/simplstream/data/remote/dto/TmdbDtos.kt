package com.simplstudios.simplstream.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Generic paged response from TMDB
 */
@JsonClass(generateAdapter = true)
data class TmdbPagedResponse<T>(
    @Json(name = "page") val page: Int,
    @Json(name = "results") val results: List<T>,
    @Json(name = "total_pages") val totalPages: Int,
    @Json(name = "total_results") val totalResults: Int
)

/**
 * Content DTO (can be Movie or TV - used in trending/search multi)
 */
@JsonClass(generateAdapter = true)
data class ContentDto(
    @Json(name = "id") val id: Int,
    @Json(name = "media_type") val mediaType: String? = null, // "movie" or "tv"
    @Json(name = "title") val title: String? = null, // Movies
    @Json(name = "name") val name: String? = null,   // TV Shows
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Float = 0f,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "release_date") val releaseDate: String? = null, // Movies
    @Json(name = "first_air_date") val firstAirDate: String? = null, // TV
    @Json(name = "genre_ids") val genreIds: List<Int>? = null
) {
    val displayTitle: String get() = title ?: name ?: ""
    val displayDate: String? get() = releaseDate ?: firstAirDate
    val isMovie: Boolean get() = mediaType == "movie" || title != null
}

/**
 * Movie DTO
 */
@JsonClass(generateAdapter = true)
data class MovieDto(
    @Json(name = "id") val id: Int,
    @Json(name = "title") val title: String,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Float = 0f,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null,
    @Json(name = "adult") val adult: Boolean = false
)

/**
 * Movie Detail DTO (full info)
 */
@JsonClass(generateAdapter = true)
data class MovieDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "title") val title: String,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Float = 0f,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "release_date") val releaseDate: String? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "genres") val genres: List<GenreDto>? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "credits") val credits: CreditsDto? = null,
    @Json(name = "recommendations") val recommendations: TmdbPagedResponse<MovieDto>? = null,
    @Json(name = "videos") val videos: VideosDto? = null
)

/**
 * TV Show DTO
 */
@JsonClass(generateAdapter = true)
data class TvShowDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Float = 0f,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "genre_ids") val genreIds: List<Int>? = null
)

/**
 * TV Show Detail DTO (full info)
 */
@JsonClass(generateAdapter = true)
data class TvShowDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "backdrop_path") val backdropPath: String? = null,
    @Json(name = "vote_average") val voteAverage: Float = 0f,
    @Json(name = "vote_count") val voteCount: Int = 0,
    @Json(name = "first_air_date") val firstAirDate: String? = null,
    @Json(name = "last_air_date") val lastAirDate: String? = null,
    @Json(name = "number_of_seasons") val numberOfSeasons: Int = 0,
    @Json(name = "number_of_episodes") val numberOfEpisodes: Int = 0,
    @Json(name = "episode_run_time") val episodeRunTime: List<Int>? = null,
    @Json(name = "genres") val genres: List<GenreDto>? = null,
    @Json(name = "status") val status: String? = null,
    @Json(name = "tagline") val tagline: String? = null,
    @Json(name = "seasons") val seasons: List<SeasonDto>? = null,
    @Json(name = "credits") val credits: CreditsDto? = null,
    @Json(name = "recommendations") val recommendations: TmdbPagedResponse<TvShowDto>? = null,
    @Json(name = "videos") val videos: VideosDto? = null,
    @Json(name = "external_ids") val externalIds: ExternalIdsDto? = null
)

/**
 * Season DTO
 */
@JsonClass(generateAdapter = true)
data class SeasonDto(
    @Json(name = "id") val id: Int,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "episode_count") val episodeCount: Int = 0
)

/**
 * Season Detail DTO (with episodes)
 */
@JsonClass(generateAdapter = true)
data class SeasonDetailDto(
    @Json(name = "id") val id: Int,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "poster_path") val posterPath: String? = null,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "episodes") val episodes: List<EpisodeDto>? = null
)

/**
 * Episode DTO
 */
@JsonClass(generateAdapter = true)
data class EpisodeDto(
    @Json(name = "id") val id: Int,
    @Json(name = "episode_number") val episodeNumber: Int,
    @Json(name = "season_number") val seasonNumber: Int,
    @Json(name = "name") val name: String? = null,
    @Json(name = "overview") val overview: String? = null,
    @Json(name = "still_path") val stillPath: String? = null,
    @Json(name = "air_date") val airDate: String? = null,
    @Json(name = "runtime") val runtime: Int? = null,
    @Json(name = "vote_average") val voteAverage: Float = 0f
)

/**
 * Genre DTO
 */
@JsonClass(generateAdapter = true)
data class GenreDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String
)

@JsonClass(generateAdapter = true)
data class GenreListDto(
    @Json(name = "genres") val genres: List<GenreDto>
)

/**
 * Credits DTO
 */
@JsonClass(generateAdapter = true)
data class CreditsDto(
    @Json(name = "cast") val cast: List<CastDto>? = null,
    @Json(name = "crew") val crew: List<CrewDto>? = null
)

@JsonClass(generateAdapter = true)
data class CastDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "character") val character: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null,
    @Json(name = "order") val order: Int = 0
)

@JsonClass(generateAdapter = true)
data class CrewDto(
    @Json(name = "id") val id: Int,
    @Json(name = "name") val name: String,
    @Json(name = "job") val job: String? = null,
    @Json(name = "department") val department: String? = null,
    @Json(name = "profile_path") val profilePath: String? = null
)

/**
 * Videos DTO
 */
@JsonClass(generateAdapter = true)
data class VideosDto(
    @Json(name = "results") val results: List<VideoDto>? = null
)

@JsonClass(generateAdapter = true)
data class VideoDto(
    @Json(name = "id") val id: String,
    @Json(name = "key") val key: String, // YouTube key
    @Json(name = "name") val name: String,
    @Json(name = "site") val site: String, // "YouTube"
    @Json(name = "type") val type: String  // "Trailer", "Teaser", etc.
)

/**
 * External IDs DTO
 */
@JsonClass(generateAdapter = true)
data class ExternalIdsDto(
    @Json(name = "imdb_id") val imdbId: String? = null,
    @Json(name = "tvdb_id") val tvdbId: Int? = null
)
