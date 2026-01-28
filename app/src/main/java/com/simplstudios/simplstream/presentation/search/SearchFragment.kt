package com.simplstudios.simplstream.presentation.search

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.leanback.app.SearchSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.Genre
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.presentation.common.ContentCardPresenter
import com.simplstudios.simplstream.presentation.common.GenreChipPresenter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Search fragment using Leanback
 */
@AndroidEntryPoint
class SearchFragment : SearchSupportFragment(), SearchSupportFragment.SearchResultProvider {
    
    private val viewModel: SearchViewModel by viewModels()
    
    private var rowsAdapter: ArrayObjectAdapter? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
            setSearchResultProvider(this)
            
            setOnItemViewClickedListener { _, item, _, _ ->
                when (item) {
                    is Content -> navigateToDetail(item)
                    is GenreItem -> viewModel.browseGenre(item.genre, item.mediaType)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        try {
            observeState()
            
            // Set initial query if any
            viewLifecycleOwner.lifecycleScope.launch {
                viewModel.query.collect { query ->
                    if (query.isNotEmpty()) {
                        setSearchQuery(query, false)
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    override fun getResultsAdapter(): ObjectAdapter = rowsAdapter ?: ArrayObjectAdapter(ListRowPresenter())
    
    override fun onQueryTextChange(newQuery: String): Boolean {
        viewModel.setQuery(newQuery)
        return true
    }
    
    override fun onQueryTextSubmit(query: String): Boolean {
        viewModel.setQuery(query)
        return true
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateResults(state)
                }
            }
        }
    }
    
    private fun updateResults(state: SearchUiState) {
        val adapter = rowsAdapter ?: return
        adapter.clear()
        
        try {
            when {
                state.isSearching -> {
                    // Show loading state - Leanback handles this
                }
                state.showGenres -> {
                    // Show genre browsing
                    addGenreRows(state)
                }
                state.browsingGenre != null -> {
                    // Show genre results
                    if (state.searchResults.isNotEmpty()) {
                        val header = HeaderItem(0, state.browsingGenre.name)
                        val rowAdapter = ArrayObjectAdapter(ContentCardPresenter())
                        rowAdapter.addAll(0, state.searchResults)
                        adapter.add(ListRow(header, rowAdapter))
                    }
                }
                state.hasSearched -> {
                    if (state.searchResults.isEmpty()) {
                        // Empty state handled by Leanback
                    } else {
                        val header = HeaderItem(0, "Search Results")
                        val rowAdapter = ArrayObjectAdapter(ContentCardPresenter())
                        rowAdapter.addAll(0, state.searchResults)
                        adapter.add(ListRow(header, rowAdapter))
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun addGenreRows(state: SearchUiState) {
        val adapter = rowsAdapter ?: return
        
        try {
            // Movie genres
            if (state.movieGenres.isNotEmpty()) {
                val header = HeaderItem(0, "Movie Genres")
                val rowAdapter = ArrayObjectAdapter(GenreChipPresenter())
                rowAdapter.addAll(0, state.movieGenres.map { GenreItem(it, MediaType.MOVIE) })
                adapter.add(ListRow(header, rowAdapter))
            }
            
            // TV genres
            if (state.tvGenres.isNotEmpty()) {
                val header = HeaderItem(1, "TV Show Genres")
                val rowAdapter = ArrayObjectAdapter(GenreChipPresenter())
                rowAdapter.addAll(0, state.tvGenres.map { GenreItem(it, MediaType.TV) })
                adapter.add(ListRow(header, rowAdapter))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    private fun navigateToDetail(content: Content) {
        val action = SearchFragmentDirections.actionSearchToDetail(
            contentId = content.id,
            mediaType = content.mediaType.name
        )
        findNavController().navigate(action)
    }
}

data class GenreItem(
    val genre: Genre,
    val mediaType: MediaType
)
