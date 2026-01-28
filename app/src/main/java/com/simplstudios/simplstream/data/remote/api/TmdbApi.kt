package com.simplstudios.simplstream.data.remote.api

import com.simplstudios.simplstream.data.remote.dto.*
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * TMDB API Interface
 * All movie/TV metadata comes from here
 */
interface TmdbApi {

    // ==================== TRENDING ====================
    
    @GET("trending/all/week")
    suspend fun getTrending(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<ContentDto>

    @GET("trending/movie/week")
    suspend fun getTrendingMovies(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MovieDto>

    @GET("trending/tv/week")
    suspend fun getTrendingTv(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShowDto>

    // ==================== MOVIES ====================
    
    @GET("movie/popular")
    suspend fun getPopularMovies(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MovieDto>

    @GET("movie/top_rated")
    suspend fun getTopRatedMovies(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MovieDto>

    @GET("movie/now_playing")
    suspend fun getNowPlayingMovies(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MovieDto>

    @GET("movie/upcoming")
    suspend fun getUpcomingMovies(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MovieDto>

    @GET("movie/{movie_id}")
    suspend fun getMovieDetails(
        @Path("movie_id") movieId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,recommendations,videos"
    ): MovieDetailDto

    // ==================== TV SHOWS ====================
    
    @GET("tv/popular")
    suspend fun getPopularTvShows(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShowDto>

    @GET("tv/top_rated")
    suspend fun getTopRatedTvShows(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShowDto>

    @GET("tv/on_the_air")
    suspend fun getOnTheAirTvShows(
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShowDto>

    @GET("tv/{tv_id}")
    suspend fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("append_to_response") appendToResponse: String = "credits,recommendations,videos"
    ): TvShowDetailDto

    @GET("tv/{tv_id}/season/{season_number}")
    suspend fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int
    ): SeasonDetailDto

    // ==================== SEARCH ====================
    
    @GET("search/multi")
    suspend fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<ContentDto>

    @GET("search/movie")
    suspend fun searchMovies(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MovieDto>

    @GET("search/tv")
    suspend fun searchTvShows(
        @Query("query") query: String,
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShowDto>

    // ==================== DISCOVER ====================
    
    @GET("discover/movie")
    suspend fun discoverMovies(
        @Query("with_genres") genreId: Int? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<MovieDto>

    @GET("discover/tv")
    suspend fun discoverTvShows(
        @Query("with_genres") genreId: Int? = null,
        @Query("sort_by") sortBy: String = "popularity.desc",
        @Query("page") page: Int = 1
    ): TmdbPagedResponse<TvShowDto>

    // ==================== GENRES ====================
    
    @GET("genre/movie/list")
    suspend fun getMovieGenres(): GenreListDto

    @GET("genre/tv/list")
    suspend fun getTvGenres(): GenreListDto
}
