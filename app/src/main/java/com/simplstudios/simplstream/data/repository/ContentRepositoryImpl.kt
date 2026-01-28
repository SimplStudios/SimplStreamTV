package com.simplstudios.simplstream.data.repository

import com.simplstudios.simplstream.BuildConfig
import com.simplstudios.simplstream.data.remote.api.TmdbApi
import com.simplstudios.simplstream.data.remote.dto.*
import com.simplstudios.simplstream.domain.model.*
import com.simplstudios.simplstream.domain.repository.ContentRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ContentRepositoryImpl @Inject constructor(
    private val tmdbApi: TmdbApi
) : ContentRepository {
    
    companion object {
        private const val IMAGE_BASE_URL = "https://image.tmdb.org/t/p/"
        private const val POSTER_SIZE = "w500"
        private const val BACKDROP_SIZE = "w1280"
        private const val PROFILE_SIZE = "w185"
    }
    
    // ============ Trending ============
    
    override suspend fun getTrending(
        mediaType: String,
        timeWindow: String,
        page: Int
    ): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getTrending(page)
        PagedContent(
            page = response.page,
            totalPages = response.totalPages,
            totalResults = response.totalResults,
            items = response.results.map { it.toDomain() }
        )
    }
    
    // ============ Movies ============
    
    override suspend fun getPopularMovies(page: Int): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getPopularMovies(page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun getTopRatedMovies(page: Int): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getTopRatedMovies(page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun getNowPlayingMovies(page: Int): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getNowPlayingMovies(page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun getUpcomingMovies(page: Int): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getUpcomingMovies(page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun getMovieDetails(movieId: Int): Result<MovieDetail> = safeApiCall {
        val response = tmdbApi.getMovieDetails(movieId)
        response.toDomain()
    }
    
    // ============ TV Shows ============
    
    override suspend fun getPopularTvShows(page: Int): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getPopularTvShows(page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun getTopRatedTvShows(page: Int): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getTopRatedTvShows(page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun getOnTheAirTvShows(page: Int): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.getOnTheAirTvShows(page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun getTvShowDetails(tvShowId: Int): Result<TvShowDetail> = safeApiCall {
        val response = tmdbApi.getTvShowDetails(tvShowId)
        response.toDomain()
    }
    
    override suspend fun getSeasonDetails(
        tvShowId: Int,
        seasonNumber: Int
    ): Result<SeasonDetail> = safeApiCall {
        val response = tmdbApi.getSeasonDetails(tvShowId, seasonNumber)
        response.toDomain()
    }
    
    // ============ Search ============
    
    override suspend fun searchMulti(
        query: String,
        page: Int
    ): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.searchMulti(query, page)
        PagedContent(
            page = response.page,
            totalPages = response.totalPages,
            totalResults = response.totalResults,
            items = response.results
                .filter { it.mediaType == "movie" || it.mediaType == "tv" }
                .map { it.toDomain() }
        )
    }
    
    override suspend fun searchMovies(
        query: String,
        page: Int
    ): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.searchMovies(query, page)
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun searchTvShows(
        query: String,
        page: Int
    ): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.searchTvShows(query, page)
        response.toPagedContent { it.toDomain() }
    }
    
    // ============ Discover ============
    
    override suspend fun discoverMovies(
        genreId: Int?,
        sortBy: String,
        page: Int
    ): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.discoverMovies(
            genreId = genreId,
            sortBy = sortBy,
            page = page
        )
        response.toPagedContent { it.toDomain() }
    }
    
    override suspend fun discoverTvShows(
        genreId: Int?,
        sortBy: String,
        page: Int
    ): Result<PagedContent<Content>> = safeApiCall {
        val response = tmdbApi.discoverTvShows(
            genreId = genreId,
            sortBy = sortBy,
            page = page
        )
        response.toPagedContent { it.toDomain() }
    }
    
    // ============ Genres ============
    
    override suspend fun getMovieGenres(): Result<List<Genre>> = safeApiCall {
        val response = tmdbApi.getMovieGenres()
        response.genres.map { Genre(it.id, it.name) }
    }
    
    override suspend fun getTvGenres(): Result<List<Genre>> = safeApiCall {
        val response = tmdbApi.getTvGenres()
        response.genres.map { Genre(it.id, it.name) }
    }
    
    // ============ Video Sources ============
    
    override fun getVideoSources(
        contentId: Int,
        mediaType: MediaType,
        seasonNumber: Int?,
        episodeNumber: Int?,
        imdbId: String?,
        defaultServerId: VideoServerId?
    ): List<VideoSource> {
        val sources = mutableListOf<VideoSource>()
        
        when (mediaType) {
            MediaType.MOVIE -> {
                // Server Alpha (111Movies)
                sources.add(VideoSource(
                    id = VideoServerId.MOVIES111,
                    name = "111Movies",
                    displayName = VideoSource.getDisplayName(VideoServerId.MOVIES111),
                    url = "${BuildConfig.MOVIES111_BASE_URL}/movie/$contentId",
                    priority = if (defaultServerId == VideoServerId.MOVIES111) 0 else 1,
                    isDefault = defaultServerId == VideoServerId.MOVIES111
                ))
                
                // Server Dot (Vidnest) - least ads
                sources.add(VideoSource(
                    id = VideoServerId.VIDNEST,
                    name = "Vidnest",
                    displayName = VideoSource.getDisplayName(VideoServerId.VIDNEST),
                    url = "${BuildConfig.VIDNEST_BASE_URL}/movie/$contentId",
                    priority = if (defaultServerId == VideoServerId.VIDNEST) 0 else 2,
                    isDefault = defaultServerId == VideoServerId.VIDNEST
                ))
                
                // Server Omega (Vidlink)
                sources.add(VideoSource(
                    id = VideoServerId.VIDLINK,
                    name = "Vidlink",
                    displayName = VideoSource.getDisplayName(VideoServerId.VIDLINK),
                    url = "${BuildConfig.VIDLINK_BASE_URL}/movie/$contentId",
                    priority = if (defaultServerId == VideoServerId.VIDLINK) 0 else 3,
                    isDefault = defaultServerId == VideoServerId.VIDLINK
                ))
            }
            MediaType.TV -> {
                val season = seasonNumber ?: 1
                val episode = episodeNumber ?: 1
                
                // Server Alpha (111Movies)
                sources.add(VideoSource(
                    id = VideoServerId.MOVIES111,
                    name = "111Movies",
                    displayName = VideoSource.getDisplayName(VideoServerId.MOVIES111),
                    url = "${BuildConfig.MOVIES111_BASE_URL}/tv/$contentId/$season/$episode",
                    priority = if (defaultServerId == VideoServerId.MOVIES111) 0 else 1,
                    isDefault = defaultServerId == VideoServerId.MOVIES111
                ))
                
                // Server Dot (Vidnest) - least ads
                sources.add(VideoSource(
                    id = VideoServerId.VIDNEST,
                    name = "Vidnest",
                    displayName = VideoSource.getDisplayName(VideoServerId.VIDNEST),
                    url = "${BuildConfig.VIDNEST_BASE_URL}/tv/$contentId/$season/$episode",
                    priority = if (defaultServerId == VideoServerId.VIDNEST) 0 else 2,
                    isDefault = defaultServerId == VideoServerId.VIDNEST
                ))
                
                // Server Omega (Vidlink)
                sources.add(VideoSource(
                    id = VideoServerId.VIDLINK,
                    name = "Vidlink",
                    displayName = VideoSource.getDisplayName(VideoServerId.VIDLINK),
                    url = "${BuildConfig.VIDLINK_BASE_URL}/tv/$contentId/$season/$episode",
                    priority = if (defaultServerId == VideoServerId.VIDLINK) 0 else 3,
                    isDefault = defaultServerId == VideoServerId.VIDLINK
                ))
            }
        }
        
        return sources.sortedBy { it.priority }
    }
    
    // ============ Helpers ============
    
    private suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> {
        return withContext(Dispatchers.IO) {
            try {
                Result.success(call())
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    private fun <T, R> TmdbPagedResponse<T>.toPagedContent(mapper: (T) -> R): PagedContent<R> {
        return PagedContent(
            page = page,
            totalPages = totalPages,
            totalResults = totalResults,
            items = results.map(mapper)
        )
    }
    
    private fun buildImageUrl(path: String?, size: String): String? {
        return path?.let { "$IMAGE_BASE_URL$size$it" }
    }
    
    // ============ Domain Mappers ============
    
    private fun ContentDto.toDomain(): Content {
        return Content(
            id = id,
            title = displayTitle,
            overview = overview ?: "",
            posterUrl = buildImageUrl(posterPath, POSTER_SIZE),
            backdropUrl = buildImageUrl(backdropPath, BACKDROP_SIZE),
            voteAverage = voteAverage,
            releaseDate = displayDate,
            mediaType = if (isMovie) MediaType.MOVIE else MediaType.TV
        )
    }
    
    private fun MovieDto.toDomain(): Content {
        return Content(
            id = id,
            title = title,
            overview = overview ?: "",
            posterUrl = buildImageUrl(posterPath, POSTER_SIZE),
            backdropUrl = buildImageUrl(backdropPath, BACKDROP_SIZE),
            voteAverage = voteAverage,
            releaseDate = releaseDate,
            mediaType = MediaType.MOVIE
        )
    }
    
    private fun TvShowDto.toDomain(): Content {
        return Content(
            id = id,
            title = name,
            overview = overview ?: "",
            posterUrl = buildImageUrl(posterPath, POSTER_SIZE),
            backdropUrl = buildImageUrl(backdropPath, BACKDROP_SIZE),
            voteAverage = voteAverage,
            releaseDate = firstAirDate,
            mediaType = MediaType.TV
        )
    }
    
    private fun MovieDetailDto.toDomain(): MovieDetail {
        return MovieDetail(
            id = id,
            imdbId = imdbId,
            title = title,
            overview = overview ?: "",
            tagline = tagline,
            posterUrl = buildImageUrl(posterPath, POSTER_SIZE),
            backdropUrl = buildImageUrl(backdropPath, BACKDROP_SIZE),
            voteAverage = voteAverage,
            voteCount = voteCount,
            releaseDate = releaseDate,
            runtime = runtime,
            status = status,
            genres = genres?.map { Genre(it.id, it.name) } ?: emptyList(),
            cast = credits?.cast?.take(20)?.map { it.toDomain() } ?: emptyList(),
            crew = credits?.crew?.filter { 
                it.job == "Director" || it.job == "Writer" || it.job == "Screenplay" 
            }?.map { it.toDomain() } ?: emptyList(),
            recommendations = recommendations?.results?.take(12)?.map { it.toDomain() } ?: emptyList(),
            trailerKey = videos?.results
                ?.filter { it.site == "YouTube" && (it.type == "Trailer" || it.type == "Teaser") }
                ?.firstOrNull()?.key
        )
    }
    
    private fun TvShowDetailDto.toDomain(): TvShowDetail {
        return TvShowDetail(
            id = id,
            imdbId = externalIds?.imdbId,
            name = name,
            overview = overview ?: "",
            tagline = tagline,
            posterUrl = buildImageUrl(posterPath, POSTER_SIZE),
            backdropUrl = buildImageUrl(backdropPath, BACKDROP_SIZE),
            voteAverage = voteAverage,
            voteCount = voteCount,
            firstAirDate = firstAirDate,
            lastAirDate = lastAirDate,
            numberOfSeasons = numberOfSeasons,
            numberOfEpisodes = numberOfEpisodes,
            episodeRunTime = episodeRunTime?.firstOrNull(),
            status = status,
            genres = genres?.map { Genre(it.id, it.name) } ?: emptyList(),
            seasons = seasons
                ?.filter { it.seasonNumber > 0 } // Exclude specials
                ?.map { it.toDomain() } 
                ?: emptyList(),
            cast = credits?.cast?.take(20)?.map { it.toDomain() } ?: emptyList(),
            crew = credits?.crew?.filter { 
                it.job == "Creator" || it.job == "Executive Producer" 
            }?.distinctBy { it.id }?.map { it.toDomain() } ?: emptyList(),
            recommendations = recommendations?.results?.take(12)?.map { it.toDomain() } ?: emptyList(),
            trailerKey = videos?.results
                ?.filter { it.site == "YouTube" && (it.type == "Trailer" || it.type == "Teaser") }
                ?.firstOrNull()?.key
        )
    }
    
    private fun SeasonDto.toDomain(): Season {
        return Season(
            id = id,
            seasonNumber = seasonNumber,
            name = name ?: "Season $seasonNumber",
            overview = overview,
            posterUrl = buildImageUrl(posterPath, POSTER_SIZE),
            airDate = airDate,
            episodeCount = episodeCount
        )
    }
    
    private fun SeasonDetailDto.toDomain(): SeasonDetail {
        return SeasonDetail(
            id = id,
            seasonNumber = seasonNumber,
            name = name ?: "Season $seasonNumber",
            overview = overview,
            posterUrl = buildImageUrl(posterPath, POSTER_SIZE),
            airDate = airDate,
            episodes = episodes?.map { it.toDomain() } ?: emptyList()
        )
    }
    
    private fun EpisodeDto.toDomain(): Episode {
        return Episode(
            id = id,
            episodeNumber = episodeNumber,
            seasonNumber = seasonNumber,
            name = name ?: "Episode $episodeNumber",
            overview = overview,
            stillUrl = buildImageUrl(stillPath, BACKDROP_SIZE),
            airDate = airDate,
            runtime = runtime,
            voteAverage = voteAverage
        )
    }
    
    private fun CastDto.toDomain(): CastMember {
        return CastMember(
            id = id,
            name = name,
            character = character,
            profileUrl = buildImageUrl(profilePath, PROFILE_SIZE)
        )
    }
    
    private fun CrewDto.toDomain(): CrewMember {
        return CrewMember(
            id = id,
            name = name,
            job = job ?: "",
            profileUrl = buildImageUrl(profilePath, PROFILE_SIZE)
        )
    }
}
