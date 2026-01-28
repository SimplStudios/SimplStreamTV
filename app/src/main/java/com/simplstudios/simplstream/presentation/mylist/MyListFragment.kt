package com.simplstudios.simplstream.presentation.mylist

import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.leanback.app.VerticalGridSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.presentation.common.ContentCardPresenter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Full-screen My List / Watchlist fragment
 * Shows all saved content in a grid layout
 */
@AndroidEntryPoint
class MyListFragment : VerticalGridSupportFragment() {
    
    private val viewModel: MyListViewModel by viewModels()
    
    private lateinit var gridAdapter: ArrayObjectAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setupUI()
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        observeState()
    }
    
    private fun setupUI() {
        title = "My List"
        
        // Grid presenter with 5 columns
        val gridPresenter = VerticalGridPresenter()
        gridPresenter.numberOfColumns = 5
        setGridPresenter(gridPresenter)
        
        // Content adapter
        gridAdapter = ArrayObjectAdapter(ContentCardPresenter())
        adapter = gridAdapter
        
        // Click listener
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            if (item is Content) {
                navigateToDetail(item)
            }
        }
        
        // Handle back press
        setOnSearchClickedListener {
            findNavController().navigateUp()
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.watchlist.collect { items ->
                    gridAdapter.clear()
                    if (items.isEmpty()) {
                        // Show empty state message
                        title = "My List (Empty)"
                    } else {
                        title = "My List (${items.size})"
                        gridAdapter.addAll(0, items.map { it.toContent() })
                    }
                }
            }
        }
    }
    
    private fun navigateToDetail(content: Content) {
        val action = MyListFragmentDirections.actionMylistToDetail(
            contentId = content.id,
            mediaType = content.mediaType.name
        )
        findNavController().navigate(action)
    }
}
