package com.simplstudios.simplstream.domain.repository

import com.simplstudios.simplstream.domain.model.*
import kotlinx.coroutines.flow.Flow

interface ContentRepository {
    
    // Trending
    suspend fun getTrending(
        mediaType: String = "all",
        timeWindow: String = "week",
        page: Int = 1
    ): Result<PagedContent<Content>>
    
    // Movies
    suspend fun getPopularMovies(page: Int = 1): Result<PagedContent<Content>>
    suspend fun getTopRatedMovies(page: Int = 1): Result<PagedContent<Content>>
    suspend fun getNowPlayingMovies(page: Int = 1): Result<PagedContent<Content>>
    suspend fun getUpcomingMovies(page: Int = 1): Result<PagedContent<Content>>
    suspend fun getMovieDetails(movieId: Int): Result<MovieDetail>
    
    // TV Shows
    suspend fun getPopularTvShows(page: Int = 1): Result<PagedContent<Content>>
    suspend fun getTopRatedTvShows(page: Int = 1): Result<PagedContent<Content>>
    suspend fun getOnTheAirTvShows(page: Int = 1): Result<PagedContent<Content>>
    suspend fun getTvShowDetails(tvShowId: Int): Result<TvShowDetail>
    suspend fun getSeasonDetails(tvShowId: Int, seasonNumber: Int): Result<SeasonDetail>
    
    // Search
    suspend fun searchMulti(query: String, page: Int = 1): Result<PagedContent<Content>>
    suspend fun searchMovies(query: String, page: Int = 1): Result<PagedContent<Content>>
    suspend fun searchTvShows(query: String, page: Int = 1): Result<PagedContent<Content>>
    
    // Discover
    suspend fun discoverMovies(
        genreId: Int? = null,
        sortBy: String = "popularity.desc",
        page: Int = 1
    ): Result<PagedContent<Content>>
    
    suspend fun discoverTvShows(
        genreId: Int? = null,
        sortBy: String = "popularity.desc",
        page: Int = 1
    ): Result<PagedContent<Content>>
    
    // Genres
    suspend fun getMovieGenres(): Result<List<Genre>>
    suspend fun getTvGenres(): Result<List<Genre>>
    
    // Video Sources
    fun getVideoSources(
        contentId: Int,
        mediaType: MediaType,
        seasonNumber: Int? = null,
        episodeNumber: Int? = null,
        imdbId: String? = null,
        defaultServerId: VideoServerId? = null
    ): List<VideoSource>
}
