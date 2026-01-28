package com.simplstudios.simplstream.presentation.detail

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.leanback.widget.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.*
import com.simplstudios.simplstream.presentation.common.ContentCardPresenter
import com.simplstudios.simplstream.presentation.player.PlaybackArgs
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Detail fragment for movies and TV shows
 */
@AndroidEntryPoint
class DetailFragment : Fragment(R.layout.fragment_detail) {
    
    private val viewModel: DetailViewModel by viewModels()
    private val args: DetailFragmentArgs by navArgs()
    
    // Views
    private lateinit var backdropImage: ImageView
    private lateinit var posterImage: ImageView
    private lateinit var titleText: TextView
    private lateinit var yearText: TextView
    private lateinit var ratingText: TextView
    private lateinit var genresText: TextView
    private lateinit var runtimeText: TextView
    private lateinit var overviewText: TextView
    private lateinit var castLabel: TextView
    private lateinit var castRecycler: RecyclerView
    private lateinit var playButton: Button
    private lateinit var watchlistButton: Button
    private lateinit var resumeProgress: ProgressBar
    private lateinit var seasonsContainer: View
    private lateinit var seasonSpinner: TextView
    private lateinit var episodesRecycler: RecyclerView
    private lateinit var recommendationsRecycler: RecyclerView
    private lateinit var loadingView: View
    private lateinit var errorView: View
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        bindViews(view)
        setupButtons()
        observeState()
        observeEvents()
        
        // Load content
        val mediaType = MediaType.valueOf(args.mediaType)
        viewModel.loadContent(args.contentId, mediaType)
    }
    
    private fun bindViews(view: View) {
        backdropImage = view.findViewById(R.id.backdrop_image)
        posterImage = view.findViewById(R.id.poster_image)
        titleText = view.findViewById(R.id.title_text)
        yearText = view.findViewById(R.id.year_text)
        ratingText = view.findViewById(R.id.rating_text)
        genresText = view.findViewById(R.id.genres_text)
        runtimeText = view.findViewById(R.id.runtime_text)
        overviewText = view.findViewById(R.id.overview_text)
        castLabel = view.findViewById(R.id.cast_label)
        castRecycler = view.findViewById(R.id.cast_recycler)
        playButton = view.findViewById(R.id.play_button)
        watchlistButton = view.findViewById(R.id.watchlist_button)
        resumeProgress = view.findViewById(R.id.resume_progress)
        seasonsContainer = view.findViewById(R.id.seasons_container)
        seasonSpinner = view.findViewById(R.id.season_spinner)
        episodesRecycler = view.findViewById(R.id.episodes_recycler)
        recommendationsRecycler = view.findViewById(R.id.recommendations_recycler)
        loadingView = view.findViewById(R.id.loading_view)
        errorView = view.findViewById(R.id.error_view)
    }
    
    private fun setupButtons() {
        playButton.setOnClickListener {
            val state = viewModel.uiState.value
            if (state.canResume) {
                viewModel.resumeWatching()
            } else if (state.isMovie) {
                viewModel.playMovie()
            } else {
                // For TV shows, play first episode
                state.currentSeasonDetail?.episodes?.firstOrNull()?.let {
                    viewModel.playEpisode(it)
                }
            }
        }
        
        watchlistButton.setOnClickListener {
            viewModel.toggleWatchlist()
        }
        
        // Focus handling
        playButton.requestFocus()
    }
    
    private fun observeState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    loadingView.isVisible = state.isLoading
                    errorView.isVisible = state.error != null && !state.isLoading
                    
                    if (!state.isLoading && state.error == null) {
                        updateUI(state)
                    }
                }
            }
        }
    }
    
    private fun updateUI(state: DetailUiState) {
        // Backdrop
        backdropImage.load(state.backdropUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_card)
        }
        
        // Poster
        posterImage.load(state.posterUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_card)
        }
        
        // Text content
        titleText.text = state.title
        yearText.text = state.year ?: ""
        ratingText.text = "★ ${state.rating}"
        genresText.text = state.genres
        overviewText.text = state.overview
        
        // Runtime (movies) or season info (TV)
        runtimeText.text = state.movieDetail?.runtimeDisplay 
            ?: state.tvShowDetail?.seasonCountDisplay 
            ?: ""
        
        // Watchlist button
        updateWatchlistButton(state.isInWatchlist)
        
        // Resume progress
        if (state.canResume) {
            playButton.text = getString(R.string.action_resume)
            resumeProgress.isVisible = true
            resumeProgress.progress = (state.resumeProgress * 100).toInt()
        } else {
            playButton.text = getString(R.string.action_play)
            resumeProgress.isVisible = false
        }
        
        // Cast
        val cast = state.movieDetail?.cast ?: state.tvShowDetail?.cast ?: emptyList()
        if (cast.isNotEmpty()) {
            castLabel.isVisible = true
            castRecycler.isVisible = true
            setupCastRecycler(cast)
        } else {
            castLabel.isVisible = false
            castRecycler.isVisible = false
        }
        
        // TV Show specific - seasons and episodes
        if (state.isTvShow) {
            seasonsContainer.isVisible = true
            setupSeasonsAndEpisodes(state)
        } else {
            seasonsContainer.isVisible = false
            episodesRecycler.isVisible = false
        }
        
        // Recommendations
        val recommendations = state.movieDetail?.recommendations ?: state.tvShowDetail?.recommendations ?: emptyList()
        if (recommendations.isNotEmpty()) {
            setupRecommendationsRecycler(recommendations)
            recommendationsRecycler.isVisible = true
        }
    }
    
    private fun updateWatchlistButton(isInWatchlist: Boolean) {
        watchlistButton.text = if (isInWatchlist) {
            getString(R.string.action_remove_from_list)
        } else {
            getString(R.string.action_add_to_list)
        }
        watchlistButton.isSelected = isInWatchlist
    }
    
    private fun setupCastRecycler(cast: List<CastMember>) {
        castRecycler.adapter = CastAdapter(cast)
    }
    
    private fun setupSeasonsAndEpisodes(state: DetailUiState) {
        val tvShow = state.tvShowDetail ?: return
        
        // Season selector
        val selectedSeason = tvShow.seasons.find { it.seasonNumber == state.selectedSeasonNumber }
        seasonSpinner.text = selectedSeason?.name ?: "Season ${state.selectedSeasonNumber}"
        
        seasonSpinner.setOnClickListener {
            showSeasonPicker(tvShow.seasons, state.selectedSeasonNumber)
        }
        
        // Episodes
        state.currentSeasonDetail?.let { season ->
            episodesRecycler.isVisible = true
            episodesRecycler.adapter = EpisodeAdapter(season.episodes) { episode ->
                viewModel.playEpisode(episode)
            }
        }
    }
    
    private fun showSeasonPicker(seasons: List<Season>, currentSeason: Int) {
        val seasonNames = seasons.map { it.name }.toTypedArray()
        val currentIndex = seasons.indexOfFirst { it.seasonNumber == currentSeason }
        
        androidx.appcompat.app.AlertDialog.Builder(requireContext(), R.style.SimplStreamDialogTheme)
            .setTitle("Select Season")
            .setSingleChoiceItems(seasonNames, currentIndex) { dialog, which ->
                val selected = seasons[which]
                viewModel.selectSeason(selected.seasonNumber)
                dialog.dismiss()
            }
            .show()
    }
    
    private fun setupRecommendationsRecycler(recommendations: List<Content>) {
        // Simple horizontal recycler for recommendations
        recommendationsRecycler.adapter = RecommendationsAdapter(recommendations) { content ->
            val action = DetailFragmentDirections.actionDetailSelf(
                contentId = content.id,
                mediaType = content.mediaType.name
            )
            findNavController().navigate(action)
        }
    }
    
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    try {
                        when (event) {
                            is DetailEvent.PlayContent -> {
                                if (event.sources.isNotEmpty()) {
                                    navigateToPlayer(event)
                                } else {
                                    Toast.makeText(requireContext(), "No video sources available", Toast.LENGTH_SHORT).show()
                                }
                            }
                            is DetailEvent.AddedToWatchlist -> {
                                Toast.makeText(requireContext(), R.string.added_to_watchlist, Toast.LENGTH_SHORT).show()
                            }
                            is DetailEvent.RemovedFromWatchlist -> {
                                Toast.makeText(requireContext(), R.string.removed_from_watchlist, Toast.LENGTH_SHORT).show()
                            }
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(requireContext(), "Error: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    
    private fun navigateToPlayer(event: DetailEvent.PlayContent) {
        try {
            val args = PlaybackArgs(
                contentId = event.contentId,
                title = event.title,
                mediaType = event.mediaType,
                sources = event.sources,
                seasonNumber = event.seasonNumber,
                episodeNumber = event.episodeNumber,
                episodeName = event.episodeName,
                resumePosition = event.resumePosition
            )
            
            // Use WebView player for embed URLs (HTML pages with embedded players)
            // Native player requires direct HLS/DASH stream URLs
            val action = DetailFragmentDirections.actionDetailToPlayer(args)
            findNavController().navigate(action)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}

/**
 * Cast adapter
 */
class CastAdapter(
    private val cast: List<CastMember>
) : RecyclerView.Adapter<CastAdapter.CastViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CastViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_cast, parent, false)
        return CastViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CastViewHolder, position: Int) {
        holder.bind(cast[position])
    }
    
    override fun getItemCount(): Int = cast.size
    
    class CastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        private val nameText: TextView = itemView.findViewById(R.id.name_text)
        private val characterText: TextView = itemView.findViewById(R.id.character_text)
        
        fun bind(castMember: CastMember) {
            profileImage.load(castMember.profileUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_card)
                error(R.drawable.bg_card)
            }
            nameText.text = castMember.name
            characterText.text = castMember.character ?: ""
        }
    }
}

/**
 * Episode adapter
 */
class EpisodeAdapter(
    private val episodes: List<Episode>,
    private val onEpisodeClick: (Episode) -> Unit
) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_episode, parent, false)
        return EpisodeViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position])
    }
    
    override fun getItemCount(): Int = episodes.size
    
    inner class EpisodeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val stillImage: ImageView = itemView.findViewById(R.id.still_image)
        private val episodeNumber: TextView = itemView.findViewById(R.id.episode_number)
        private val episodeName: TextView = itemView.findViewById(R.id.episode_name)
        private val episodeOverview: TextView = itemView.findViewById(R.id.episode_overview)
        private val episodeRuntime: TextView = itemView.findViewById(R.id.episode_runtime)
        
        fun bind(episode: Episode) {
            stillImage.load(episode.stillUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_card)
            }
            
            episodeNumber.text = episode.episodeCode
            episodeName.text = episode.name
            episodeOverview.text = episode.overview ?: ""
            episodeRuntime.text = episode.runtimeDisplay ?: ""
            
            itemView.setOnClickListener { onEpisodeClick(episode) }
            
            // Focus handling
            itemView.isFocusable = true
            itemView.setOnFocusChangeListener { v, hasFocus ->
                v.isSelected = hasFocus
            }
        }
    }
}

/**
 * Recommendations adapter
 */
class RecommendationsAdapter(
    private val recommendations: List<Content>,
    private val onContentClick: (Content) -> Unit
) : RecyclerView.Adapter<RecommendationsAdapter.RecommendationViewHolder>() {
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = android.view.LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_card, parent, false)
        return RecommendationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }
    
    override fun getItemCount(): Int = recommendations.size
    
    inner class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val posterImage: ImageView = itemView.findViewById(R.id.poster_image)
        private val titleText: TextView = itemView.findViewById(R.id.title_text)
        
        fun bind(content: Content) {
            posterImage.load(content.posterUrl) {
                crossfade(true)
                placeholder(R.drawable.bg_card)
            }
            titleText.text = content.title
            
            itemView.setOnClickListener { onContentClick(content) }
            
            itemView.isFocusable = true
            itemView.setOnFocusChangeListener { v, hasFocus ->
                val scale = if (hasFocus) 1.1f else 1.0f
                v.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
            }
        }
    }
}
