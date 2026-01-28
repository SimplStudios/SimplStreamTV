package com.simplstudios.simplstream.presentation.mylist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.simplstudios.simplstream.data.preferences.SessionManager
import com.simplstudios.simplstream.domain.model.WatchlistItem
import com.simplstudios.simplstream.domain.repository.WatchlistRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MyListViewModel @Inject constructor(
    private val watchlistRepository: WatchlistRepository,
    private val sessionManager: SessionManager
) : ViewModel() {
    
    private val _watchlist = MutableStateFlow<List<WatchlistItem>>(emptyList())
    val watchlist: StateFlow<List<WatchlistItem>> = _watchlist.asStateFlow()
    
    init {
        loadWatchlist()
    }
    
    private fun loadWatchlist() {
        viewModelScope.launch {
            sessionManager.currentProfileId
                .filter { it != SessionManager.NO_PROFILE }
                .collectLatest { profileId ->
                    watchlistRepository.getWatchlist(profileId)
                        .collect { items ->
                            _watchlist.value = items
                        }
                }
        }
    }
}
