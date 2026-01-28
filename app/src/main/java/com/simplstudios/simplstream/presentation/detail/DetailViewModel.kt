package com.simplstudios.simplstream.presentation.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplstudios.simplstream.data.preferences.SessionManager
import com.simplstudios.simplstream.domain.model.*
import com.simplstudios.simplstream.domain.repository.ContentRepository
import com.simplstudios.simplstream.domain.repository.WatchHistoryRepository
import com.simplstudios.simplstream.domain.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val watchlistRepository: WatchlistRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DetailUiState())
    val uiState: StateFlow<DetailUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<DetailEvent>()
    val events: SharedFlow<DetailEvent> = _events.asSharedFlow()
    
    private var contentId: Int = 0
    private var mediaType: MediaType = MediaType.MOVIE
    private var profileId: Long = SessionManager.NO_PROFILE
    
    init {
        viewModelScope.launch {
            sessionManager.currentProfileId.collect { id ->
                profileId = id
                if (contentId > 0) {
                    observeWatchlistStatus()
                    observeWatchHistory()
                }
            }
        }
    }
    
    fun loadContent(id: Int, type: MediaType) {
        contentId = id
        mediaType = type
        
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                when (type) {
                    MediaType.MOVIE -> loadMovieDetails(id)
                    MediaType.TV -> loadTvShowDetails(id)
                }
                
                observeWatchlistStatus()
                observeWatchHistory()
            } catch (e: Exception) {
                _uiState.update { 
                    it.copy(
                        isLoading = false, 
                        error = e.message ?: "Failed to load details"
                    ) 
                }
            }
        }
    }
    
    private suspend fun loadMovieDetails(movieId: Int) {
        val result = contentRepository.getMovieDetails(movieId)
        result.onSuccess { movie ->
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    movieDetail = movie,
                    tvShowDetail = null
                )
            }
        }.onFailure { e ->
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    error = e.message ?: "Failed to load movie"
                )
            }
        }
    }
    
    private suspend fun loadTvShowDetails(tvShowId: Int) {
        val result = contentRepository.getTvShowDetails(tvShowId)
        result.onSuccess { tvShow ->
            _uiState.update { 
                it.copy(
                    isLoading = false,
                    tvShowDetail = tvShow,
                    movieDetail = null,
                    selectedSeasonNumber = tvShow.seasons.firstOrNull()?.seasonNumber ?: 1
                )
            }
            
            // Load first season's episodes
            if (tvShow.seasons.isNotEmpty()) {
                loadSeasonEpisodes(tvShowId, tvShow.seasons.first().seasonNumber)
            }
        }.onFailure { e ->
            _uiState.update { 
                it.copy(
                    isLoading = false, 
                    error = e.message ?: "Failed to load TV show"
                )
            }
        }
    }
    
    fun selectSeason(seasonNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(selectedSeasonNumber = seasonNumber) }
            loadSeasonEpisodes(contentId, seasonNumber)
        }
    }
    
    private suspend fun loadSeasonEpisodes(tvShowId: Int, seasonNumber: Int) {
        _uiState.update { it.copy(isLoadingEpisodes = true) }
        
        val result = contentRepository.getSeasonDetails(tvShowId, seasonNumber)
        result.onSuccess { season ->
            _uiState.update { 
                it.copy(
                    isLoadingEpisodes = false,
                    currentSeasonDetail = season
                )
            }
        }.onFailure {
            _uiState.update { it.copy(isLoadingEpisodes = false) }
        }
    }
    
    private fun observeWatchlistStatus() {
        if (profileId == SessionManager.NO_PROFILE) return
        
        viewModelScope.launch {
            watchlistRepository.observeIsInWatchlist(profileId, contentId, mediaType)
                .collect { isInWatchlist ->
                    _uiState.update { it.copy(isInWatchlist = isInWatchlist) }
                }
        }
    }
    
    private fun observeWatchHistory() {
        if (profileId == SessionManager.NO_PROFILE) return
        
        viewModelScope.launch {
            watchHistoryRepository.observeWatchHistory(profileId, contentId, mediaType)
                .collect { history ->
                    _uiState.update { it.copy(watchHistory = history) }
                }
        }
    }
    
    fun toggleWatchlist() {
        if (profileId == SessionManager.NO_PROFILE) return
        
        viewModelScope.launch {
            val content = _uiState.value.movieDetail?.toContent() 
                ?: _uiState.value.tvShowDetail?.toContent()
                ?: return@launch
            
            val added = watchlistRepository.toggleWatchlist(profileId, content)
            _events.emit(
                if (added) DetailEvent.AddedToWatchlist 
                else DetailEvent.RemovedFromWatchlist
            )
        }
    }
    
    fun playMovie() {
        val movie = _uiState.value.movieDetail ?: return
        val sources = contentRepository.getVideoSources(
            contentId = movie.id,
            mediaType = MediaType.MOVIE,
            imdbId = movie.imdbId
        )
        
        viewModelScope.launch {
            _events.emit(
                DetailEvent.PlayContent(
                    contentId = movie.id,
                    title = movie.title,
                    mediaType = MediaType.MOVIE,
                    sources = sources,
                    resumePosition = _uiState.value.watchHistory?.watchPosition ?: 0
                )
            )
        }
    }
    
    fun playEpisode(episode: Episode) {
        val tvShow = _uiState.value.tvShowDetail ?: return
        val sources = contentRepository.getVideoSources(
            contentId = tvShow.id,
            mediaType = MediaType.TV,
            seasonNumber = episode.seasonNumber,
            episodeNumber = episode.episodeNumber,
            imdbId = tvShow.imdbId
        )
        
        viewModelScope.launch {
            _events.emit(
                DetailEvent.PlayContent(
                    contentId = tvShow.id,
                    title = "${tvShow.name} - ${episode.episodeCode}",
                    mediaType = MediaType.TV,
                    sources = sources,
                    seasonNumber = episode.seasonNumber,
                    episodeNumber = episode.episodeNumber,
                    episodeName = episode.name,
                    resumePosition = 0 // Could check episode-specific history
                )
            )
        }
    }
    
    fun resumeWatching() {
        val history = _uiState.value.watchHistory
        
        if (mediaType == MediaType.MOVIE) {
            playMovie()
        } else if (history != null && history.seasonNumber != null && history.episodeNumber != null) {
            // Resume specific episode
            val seasonDetail = _uiState.value.currentSeasonDetail
            if (seasonDetail?.seasonNumber == history.seasonNumber) {
                val episode = seasonDetail.episodes.find { it.episodeNumber == history.episodeNumber }
                if (episode != null) {
                    playEpisode(episode)
                    return
                }
            }
            
            // Load the correct season and play
            viewModelScope.launch {
                val result = contentRepository.getSeasonDetails(contentId, history.seasonNumber)
                result.onSuccess { season ->
                    val episode = season.episodes.find { it.episodeNumber == history.episodeNumber }
                    if (episode != null) {
                        playEpisode(episode)
                    }
                }
            }
        }
    }
}

data class DetailUiState(
    val isLoading: Boolean = true,
    val isLoadingEpisodes: Boolean = false,
    val error: String? = null,
    val movieDetail: MovieDetail? = null,
    val tvShowDetail: TvShowDetail? = null,
    val selectedSeasonNumber: Int = 1,
    val currentSeasonDetail: SeasonDetail? = null,
    val isInWatchlist: Boolean = false,
    val watchHistory: WatchHistory? = null
) {
    val isMovie: Boolean get() = movieDetail != null
    val isTvShow: Boolean get() = tvShowDetail != null
    
    val title: String get() = movieDetail?.title ?: tvShowDetail?.name ?: ""
    val overview: String get() = movieDetail?.overview ?: tvShowDetail?.overview ?: ""
    val backdropUrl: String? get() = movieDetail?.backdropUrl ?: tvShowDetail?.backdropUrl
    val posterUrl: String? get() = movieDetail?.posterUrl ?: tvShowDetail?.posterUrl
    val rating: String get() = movieDetail?.ratingDisplay ?: tvShowDetail?.ratingDisplay ?: ""
    val year: String? get() = movieDetail?.year ?: tvShowDetail?.year
    val genres: String get() = movieDetail?.genreDisplay ?: tvShowDetail?.genreDisplay ?: ""
    
    val canResume: Boolean get() = watchHistory != null && !watchHistory.isCompleted
    val resumeProgress: Float get() = watchHistory?.watchProgress ?: 0f
}

sealed class DetailEvent {
    data class PlayContent(
        val contentId: Int,
        val title: String,
        val mediaType: MediaType,
        val sources: List<VideoSource>,
        val seasonNumber: Int? = null,
        val episodeNumber: Int? = null,
        val episodeName: String? = null,
        val resumePosition: Long = 0
    ) : DetailEvent()
    
    data object AddedToWatchlist : DetailEvent()
    data object RemovedFromWatchlist : DetailEvent()
}
