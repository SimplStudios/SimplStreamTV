package com.simplstudios.simplstream.presentation.search

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplstudios.simplstream.data.preferences.SessionManager
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.Genre
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.repository.ContentRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class SearchViewModel @Inject constructor(
    private val contentRepository: ContentRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()
    
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()
    
    private var searchJob: Job? = null
    private var currentPage = 1
    private var hasMorePages = true
    
    init {
        loadGenres()
        setupSearchDebounce()
        loadLastQuery()
    }
    
    private fun loadLastQuery() {
        viewModelScope.launch {
            sessionManager.lastSearchQuery.first().let { lastQuery ->
                if (lastQuery.isNotEmpty()) {
                    _query.value = lastQuery
                }
            }
        }
    }
    
    private fun setupSearchDebounce() {
        viewModelScope.launch {
            _query
                .debounce(300)
                .distinctUntilChanged()
                .collectLatest { query ->
                    if (query.length >= 2) {
                        performSearch(query, reset = true)
                    } else if (query.isEmpty()) {
                        _uiState.update { 
                            it.copy(
                                searchResults = emptyList(),
                                isSearching = false,
                                hasSearched = false
                            )
                        }
                    }
                }
        }
    }
    
    fun setQuery(newQuery: String) {
        _query.value = newQuery
    }
    
    fun clearQuery() {
        _query.value = ""
        _uiState.update { 
            it.copy(
                searchResults = emptyList(),
                hasSearched = false
            )
        }
        viewModelScope.launch {
            sessionManager.setLastSearchQuery("")
        }
    }
    
    private fun performSearch(query: String, reset: Boolean = false) {
        searchJob?.cancel()
        
        if (reset) {
            currentPage = 1
            hasMorePages = true
            _uiState.update { it.copy(searchResults = emptyList()) }
        }
        
        if (!hasMorePages) return
        
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true) }
            
            val result = contentRepository.searchMulti(query, currentPage)
            
            result.onSuccess { pagedContent ->
                hasMorePages = pagedContent.hasMore
                currentPage++
                
                val newResults = if (reset) {
                    pagedContent.items
                } else {
                    _uiState.value.searchResults + pagedContent.items
                }
                
                _uiState.update { 
                    it.copy(
                        searchResults = newResults,
                        isSearching = false,
                        hasSearched = true,
                        error = null
                    )
                }
                
                // Save search query
                sessionManager.setLastSearchQuery(query)
                
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isSearching = false,
                        hasSearched = true,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun loadMoreResults() {
        val currentQuery = _query.value
        if (currentQuery.length >= 2 && hasMorePages && !_uiState.value.isSearching) {
            performSearch(currentQuery, reset = false)
        }
    }
    
    private fun loadGenres() {
        viewModelScope.launch {
            val movieGenres = contentRepository.getMovieGenres()
            val tvGenres = contentRepository.getTvGenres()
            
            _uiState.update { 
                it.copy(
                    movieGenres = movieGenres.getOrNull() ?: emptyList(),
                    tvGenres = tvGenres.getOrNull() ?: emptyList()
                )
            }
        }
    }
    
    fun selectFilter(filter: SearchFilter) {
        _uiState.update { it.copy(selectedFilter = filter) }
        
        // Re-search with filter if there's a query
        val currentQuery = _query.value
        if (currentQuery.length >= 2) {
            searchWithFilter(currentQuery, filter)
        }
    }
    
    private fun searchWithFilter(query: String, filter: SearchFilter) {
        searchJob?.cancel()
        currentPage = 1
        hasMorePages = true
        
        searchJob = viewModelScope.launch {
            _uiState.update { it.copy(isSearching = true, searchResults = emptyList()) }
            
            val result = when (filter) {
                SearchFilter.ALL -> contentRepository.searchMulti(query, 1)
                SearchFilter.MOVIES -> contentRepository.searchMovies(query, 1)
                SearchFilter.TV_SHOWS -> contentRepository.searchTvShows(query, 1)
            }
            
            result.onSuccess { pagedContent ->
                hasMorePages = pagedContent.hasMore
                currentPage++
                
                _uiState.update { 
                    it.copy(
                        searchResults = pagedContent.items,
                        isSearching = false,
                        hasSearched = true
                    )
                }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isSearching = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun browseGenre(genre: Genre, mediaType: MediaType) {
        viewModelScope.launch {
            _uiState.update { 
                it.copy(
                    isSearching = true, 
                    browsingGenre = genre,
                    searchResults = emptyList(),
                    hasSearched = true
                )
            }
            
            val result = when (mediaType) {
                MediaType.MOVIE -> contentRepository.discoverMovies(genreId = genre.id)
                MediaType.TV -> contentRepository.discoverTvShows(genreId = genre.id)
            }
            
            result.onSuccess { pagedContent ->
                _uiState.update { 
                    it.copy(
                        searchResults = pagedContent.items,
                        isSearching = false
                    )
                }
            }.onFailure { e ->
                _uiState.update { 
                    it.copy(
                        isSearching = false,
                        error = e.message
                    )
                }
            }
        }
    }
    
    fun clearGenreBrowse() {
        _uiState.update { 
            it.copy(
                browsingGenre = null,
                searchResults = emptyList(),
                hasSearched = false
            )
        }
    }
}

data class SearchUiState(
    val searchResults: List<Content> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val error: String? = null,
    val selectedFilter: SearchFilter = SearchFilter.ALL,
    val movieGenres: List<Genre> = emptyList(),
    val tvGenres: List<Genre> = emptyList(),
    val browsingGenre: Genre? = null
) {
    val isEmpty: Boolean get() = searchResults.isEmpty() && hasSearched && !isSearching
    val showGenres: Boolean get() = !hasSearched && browsingGenre == null
}

enum class SearchFilter {
    ALL, MOVIES, TV_SHOWS
}
