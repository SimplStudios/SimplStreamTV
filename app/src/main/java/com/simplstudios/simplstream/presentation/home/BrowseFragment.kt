package com.simplstudios.simplstream.presentation.home

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.leanback.app.BrowseSupportFragment
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import coil.load
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.model.WatchHistory
import com.simplstudios.simplstream.presentation.common.ContentCardPresenter
import com.simplstudios.simplstream.presentation.common.ContinueWatchingCardPresenter
import com.simplstudios.simplstream.presentation.common.IconHeaderItem
import com.simplstudios.simplstream.presentation.common.IconHeaderPresenter
import com.simplstudios.simplstream.presentation.common.SettingsItem
import com.simplstudios.simplstream.presentation.common.SettingsItemPresenter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Main browse fragment using Leanback library
 * Shows content rows with D-pad navigation
 */
@AndroidEntryPoint
class BrowseFragment : BrowseSupportFragment() {
    
    private val viewModel: HomeViewModel by viewModels()
    
    private var rowsAdapter: ArrayObjectAdapter? = null
    
    // Row IDs for special rows
    companion object {
        private const val ROW_ID_SETTINGS = 1000L
    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Setup rows adapter early
        rowsAdapter = ArrayObjectAdapter(ListRowPresenter())
        adapter = rowsAdapter
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        observeState()
    }
    
    private fun setupUI() {
        // Brand colors and styling - safe to use context in onViewCreated
        title = getString(R.string.app_name)
        brandColor = requireContext().getColor(R.color.simpl_blue)
        searchAffordanceColor = requireContext().getColor(R.color.simpl_blue_light)
        
        // Enable search
        setOnSearchClickedListener {
            findNavController().navigate(R.id.action_browse_to_search)
        }
        
        // Setup header presenter
        headersState = HEADERS_ENABLED
        isHeadersTransitionOnBackEnabled = true
        
        // Click listener for content items
        onItemViewClickedListener = OnItemViewClickedListener { _, item, _, _ ->
            when (item) {
                is Content -> navigateToDetail(item)
                is WatchHistory -> navigateToDetail(item.toContent())
                is SettingsItem -> handleSettingsClick(item)
            }
        }
        
        // Selection listener for hero area
        onItemViewSelectedListener = OnItemViewSelectedListener { _, item, _, _ ->
            when (item) {
                is Content -> viewModel.setSelectedContent(item)
                is WatchHistory -> viewModel.setSelectedContent(item.toContent())
            }
        }
    }
    
    private fun handleSettingsClick(item: SettingsItem) {
        when (item.id) {
            SettingsItem.ID_SETTINGS -> {
                findNavController().navigate(R.id.action_browse_to_settings)
            }
            SettingsItem.ID_SWITCH_PROFILE -> {
                findNavController().navigate(R.id.action_browse_to_profile)
            }
            SettingsItem.ID_SIGN_OUT -> {
                findNavController().navigate(R.id.action_browse_to_profile)
            }
        }
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    if (!state.isLoading) {
                        updateContent(state)
                    }
                }
            }
        }
    }
    
    private fun updateContent(state: HomeUiState) {
        val adapter = rowsAdapter ?: return
        adapter.clear()
        
        // Add content rows first
        state.contentRows.forEachIndexed { index, row ->
            val headerItem = HeaderItem(index.toLong(), row.title)
            
            val rowAdapter = when (row) {
                is ContentRow.ContinueWatchingRow -> {
                    ArrayObjectAdapter(ContinueWatchingCardPresenter()).apply {
                        addAll(0, row.items)
                    }
                }
                is ContentRow.MyListRow -> {
                    ArrayObjectAdapter(ContentCardPresenter()).apply {
                        addAll(0, row.items)
                    }
                }
                is ContentRow.SimpleRow -> {
                    ArrayObjectAdapter(ContentCardPresenter()).apply {
                        addAll(0, row.items)
                    }
                }
            }
            
            adapter.add(ListRow(headerItem, rowAdapter))
        }
        
        // Add profile rows at the end - these show up in the sidebar
        addProfileRows(adapter, state.contentRows.size)
    }
    
    private fun addProfileRows(adapter: ArrayObjectAdapter, startIndex: Int) {
        // Settings row - shows in sidebar with icon
        val settingsIcon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_settings)
        val settingsHeader = IconHeaderItem(ROW_ID_SETTINGS, "Settings", settingsIcon)
        val settingsAdapter = ArrayObjectAdapter(SettingsItemPresenter()).apply {
            add(SettingsItem(
                id = SettingsItem.ID_SETTINGS,
                title = "Profile Settings",
                description = "Manage your profile",
                iconRes = R.drawable.ic_settings
            ))
        }
        adapter.add(ListRow(settingsHeader, settingsAdapter))
    }
    
    private fun navigateToDetail(content: Content) {
        val action = BrowseFragmentDirections.actionBrowseToDetail(
            contentId = content.id,
            mediaType = content.mediaType.name
        )
        findNavController().navigate(action)
    }
}
