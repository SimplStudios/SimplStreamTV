package com.simplstudios.simplstream.presentation.common

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.leanback.widget.Presenter
import coil.load
import coil.transform.RoundedCornersTransformation
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.Content
import com.simplstudios.simplstream.domain.model.MediaType
import com.simplstudios.simplstream.domain.model.WatchHistory

/**
 * Presenter for content cards in Leanback rows
 */
class ContentCardPresenter : Presenter() {
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_content_card, parent, false)
        
        // Apply focus handling
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        
        view.setOnFocusChangeListener { v, hasFocus ->
            val scale = if (hasFocus) 1.1f else 1.0f
            v.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(150)
                .start()
            
            v.isSelected = hasFocus
        }
        
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val content = item as Content
        val view = viewHolder.view
        
        val posterImage = view.findViewById<ImageView>(R.id.poster_image)
        val titleText = view.findViewById<TextView>(R.id.title_text)
        val yearText = view.findViewById<TextView>(R.id.year_text)
        val ratingText = view.findViewById<TextView>(R.id.rating_text)
        val typeIndicator = view.findViewById<View>(R.id.type_indicator)
        
        // Load poster
        posterImage.load(content.posterUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_card)
            error(R.drawable.bg_card)
            transformations(RoundedCornersTransformation(12f))
        }
        
        // Set text
        titleText.text = content.title
        yearText.text = content.year ?: ""
        ratingText.text = "★ ${content.ratingDisplay}"
        
        // Type indicator color
        val indicatorColor = when (content.mediaType) {
            MediaType.MOVIE -> Color.parseColor("#3B82F6") // Blue for movies
            MediaType.TV -> Color.parseColor("#10B981")    // Green for TV
        }
        typeIndicator.setBackgroundColor(indicatorColor)
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // Clean up if needed
    }
}

/**
 * Presenter for continue watching cards with progress
 */
class ContinueWatchingCardPresenter : Presenter() {
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_continue_watching_card, parent, false)
        
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        
        view.setOnFocusChangeListener { v, hasFocus ->
            val scale = if (hasFocus) 1.08f else 1.0f
            v.animate()
                .scaleX(scale)
                .scaleY(scale)
                .setDuration(150)
                .start()
            
            v.isSelected = hasFocus
        }
        
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val history = item as WatchHistory
        val view = viewHolder.view
        
        val backdropImage = view.findViewById<ImageView>(R.id.backdrop_image)
        val titleText = view.findViewById<TextView>(R.id.title_text)
        val episodeText = view.findViewById<TextView>(R.id.episode_text)
        val progressBar = view.findViewById<ProgressBar>(R.id.watch_progress)
        
        // Load backdrop (wider format for continue watching)
        backdropImage.load(history.backdropUrl ?: history.posterUrl) {
            crossfade(true)
            placeholder(R.drawable.bg_card)
            error(R.drawable.bg_card)
            transformations(RoundedCornersTransformation(12f))
        }
        
        // Set text
        titleText.text = history.title
        episodeText.text = history.episodeInfo ?: ""
        episodeText.visibility = if (history.episodeInfo != null) View.VISIBLE else View.GONE
        
        // Set progress
        progressBar.progress = history.progressPercent
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        // Clean up
    }
}

/**
 * Presenter for genre chips
 */
class GenreChipPresenter : Presenter() {
    
    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_genre_chip, parent, false)
        
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        
        view.setOnFocusChangeListener { v, hasFocus ->
            v.isSelected = hasFocus
            val scale = if (hasFocus) 1.1f else 1.0f
            v.animate().scaleX(scale).scaleY(scale).setDuration(150).start()
        }
        
        return ViewHolder(view)
    }
    
    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any) {
        val textView = viewHolder.view as TextView
        // Handle both Genre and GenreItem types
        val genreName = when (item) {
            is com.simplstudios.simplstream.domain.model.Genre -> item.name
            else -> {
                // Use reflection-safe approach for GenreItem
                try {
                    val genreField = item.javaClass.getDeclaredField("genre")
                    genreField.isAccessible = true
                    val genre = genreField.get(item) as com.simplstudios.simplstream.domain.model.Genre
                    genre.name
                } catch (e: Exception) {
                    "Unknown"
                }
            }
        }
        textView.text = genreName
    }
    
    override fun onUnbindViewHolder(viewHolder: ViewHolder) {}
}
