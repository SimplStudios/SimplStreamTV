package com.simplstudios.simplstream.presentation.player

import android.os.Parcelable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplstudios.simplstream.data.preferences.SessionManager
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.model.VideoServerId
import com.simplstudios.simplstream.domain.model.VideoSource
import com.simplstudios.simplstream.domain.repository.ContentRepository
import com.simplstudios.simplstream.domain.repository.WatchHistoryRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.parcelize.Parcelize
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val contentRepository: ContentRepository,
    private val watchHistoryRepository: WatchHistoryRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PlayerUiState())
    val uiState: StateFlow<PlayerUiState> = _uiState.asStateFlow()
    
    private val _events = MutableSharedFlow<PlayerEvent>()
    val events: SharedFlow<PlayerEvent> = _events.asSharedFlow()
    
    private var profileId: Long = SessionManager.NO_PROFILE
    private var playbackArgs: PlaybackArgs? = null
    
    init {
        viewModelScope.launch {
            sessionManager.currentProfileId.collect { id ->
                profileId = id
            }
        }
        
        // Observe default server changes
        viewModelScope.launch {
            sessionManager.defaultServer.collect { serverId ->
                _uiState.update { it.copy(defaultServerId = serverId) }
            }
        }
    }
    
    fun initialize(args: PlaybackArgs) {
        playbackArgs = args
        
        // Find default server index if set
        viewModelScope.launch {
            val defaultServerId = sessionManager.getDefaultServerSync()
            val defaultIndex = if (defaultServerId != null) {
                args.sources.indexOfFirst { it.id == defaultServerId }.takeIf { it >= 0 } ?: 0
            } else {
                0
            }
            
            _uiState.update {
                it.copy(
                    contentId = args.contentId,
                    title = args.title,
                    mediaType = args.mediaType,
                    seasonNumber = args.seasonNumber,
                    episodeNumber = args.episodeNumber,
                    episodeName = args.episodeName,
                    videoSources = args.sources,
                    currentSourceIndex = defaultIndex,
                    currentUrl = args.sources.getOrNull(defaultIndex)?.url ?: "",
                    resumePosition = args.resumePosition,
                    defaultServerId = defaultServerId
                )
            }
        }
    }
    
    fun switchSource(index: Int) {
        val sources = _uiState.value.videoSources
        if (index in sources.indices) {
            _uiState.update {
                it.copy(
                    currentSourceIndex = index,
                    currentUrl = sources[index].url,
                    isLoading = true,
                    error = null
                )
            }
        }
    }
    
    /**
     * Switch to a specific server by VideoSource
     */
    fun switchToServer(source: VideoSource) {
        val index = _uiState.value.videoSources.indexOfFirst { it.id == source.id }
        if (index >= 0) {
            switchSource(index)
        }
    }
    
    /**
     * Set or toggle default server
     */
    fun setDefaultServer(serverId: VideoServerId) {
        viewModelScope.launch {
            val currentDefault = sessionManager.getDefaultServerSync()
            if (currentDefault == serverId) {
                // Toggle off - remove default
                sessionManager.setDefaultServer(null)
            } else {
                // Set new default
                sessionManager.setDefaultServer(serverId)
            }
        }
    }
    
    fun tryNextSource() {
        val currentIndex = _uiState.value.currentSourceIndex
        val nextIndex = currentIndex + 1
        
        if (nextIndex < _uiState.value.videoSources.size) {
            switchSource(nextIndex)
        } else {
            _uiState.update { it.copy(error = "All sources failed to load") }
        }
    }
    
    fun onSourceLoadStarted() {
        _uiState.update { it.copy(isLoading = true, error = null) }
    }
    
    fun onSourceLoaded() {
        _uiState.update { it.copy(isLoading = false, isPlaying = true) }
    }
    
    fun onSourceError(message: String) {
        _uiState.update { it.copy(isLoading = false, error = message) }
        // Auto-try next source
        tryNextSource()
    }
    
    fun updateProgress(position: Long, duration: Long) {
        if (duration <= 0) return
        
        val progress = position.toFloat() / duration.toFloat()
        val isCompleted = progress >= 0.9f // Mark as completed if watched 90%
        
        _uiState.update { 
            it.copy(
                currentPosition = position,
                totalDuration = duration,
                progress = progress,
                isCompleted = isCompleted
            )
        }
        
        // Save progress periodically (every 10 seconds worth of position change)
        saveProgress(position, duration, isCompleted)
    }
    
    private var lastSavedPosition: Long = 0
    
    private fun saveProgress(position: Long, duration: Long, isCompleted: Boolean) {
        // Only save every 10 seconds
        if (kotlin.math.abs(position - lastSavedPosition) < 10_000 && !isCompleted) return
        lastSavedPosition = position
        
        val args = playbackArgs ?: return
        if (profileId == SessionManager.NO_PROFILE) return
        
        viewModelScope.launch {
            val content = Content(
                id = args.contentId,
                title = args.title.substringBefore(" - "), // Remove episode suffix
                overview = "",
                posterUrl = null,
                backdropUrl = null,
                voteAverage = 0f,
                releaseDate = null,
                mediaType = args.mediaType
            )
            
            watchHistoryRepository.addOrUpdateWatchHistory(
                profileId = profileId,
                content = content,
                seasonNumber = args.seasonNumber,
                episodeNumber = args.episodeNumber,
                episodeTitle = args.episodeName,
                watchPosition = position,
                totalDuration = duration,
                isCompleted = isCompleted
            )
        }
    }
    
    fun togglePlayPause() {
        _uiState.update { it.copy(isPlaying = !it.isPlaying) }
    }
    
    fun setPlaying(playing: Boolean) {
        _uiState.update { it.copy(isPlaying = playing) }
    }
    
    fun onPlaybackEnded() {
        // Mark as completed
        _uiState.update { it.copy(isCompleted = true, isPlaying = false) }
        val state = _uiState.value
        if (state.totalDuration > 0) {
            saveProgress(state.totalDuration, state.totalDuration, true)
        }
    }
    
    fun showControls() {
        _uiState.update { it.copy(showControls = true) }
    }
    
    fun hideControls() {
        _uiState.update { it.copy(showControls = false) }
    }
    
    fun toggleControls() {
        _uiState.update { it.copy(showControls = !it.showControls) }
    }
    
    fun exit() {
        // Final save before exit
        val state = _uiState.value
        if (state.totalDuration > 0) {
            saveProgress(state.currentPosition, state.totalDuration, state.isCompleted)
        }
        
        viewModelScope.launch {
            _events.emit(PlayerEvent.Exit)
        }
    }
}

data class PlayerUiState(
    val contentId: Int = 0,
    val title: String = "",
    val mediaType: MediaType = MediaType.MOVIE,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeName: String? = null,
    val videoSources: List<VideoSource> = emptyList(),
    val currentSourceIndex: Int = 0,
    val currentUrl: String = "",
    val isLoading: Boolean = true,
    val isPlaying: Boolean = false,
    val showControls: Boolean = true,
    val error: String? = null,
    val currentPosition: Long = 0,
    val totalDuration: Long = 0,
    val progress: Float = 0f,
    val isCompleted: Boolean = false,
    val resumePosition: Long = 0,
    val defaultServerId: VideoServerId? = null
) {
    val currentSource: VideoSource? get() = videoSources.getOrNull(currentSourceIndex)
    val displayTitle: String get() = if (episodeName != null) {
        "$title - $episodeName"
    } else {
        title
    }
    val hasMultipleSources: Boolean get() = videoSources.size > 1
    val formattedPosition: String get() = formatTime(currentPosition)
    val formattedDuration: String get() = formatTime(totalDuration)
    
    private fun formatTime(millis: Long): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        return if (hours > 0) {
            "%d:%02d:%02d".format(hours, minutes % 60, seconds % 60)
        } else {
            "%d:%02d".format(minutes, seconds % 60)
        }
    }
}

sealed class PlayerEvent {
    data object Exit : PlayerEvent()
}

@Parcelize
data class PlaybackArgs(
    val contentId: Int,
    val title: String,
    val mediaType: MediaType,
    val sources: List<VideoSource>,
    val seasonNumber: Int? = null,
    val episodeNumber: Int? = null,
    val episodeName: String? = null,
    val resumePosition: Long = 0
) : Parcelable
