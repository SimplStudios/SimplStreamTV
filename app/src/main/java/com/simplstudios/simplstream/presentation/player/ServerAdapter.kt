package com.simplstudios.simplstream.presentation.player

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.simplstudios.simplstream.R
import com.simplstudios.simplstream.domain.model.VideoServerId
import com.simplstudios.simplstream.domain.model.VideoSource

/**
 * Adapter for displaying video servers in the sidebar
 */
class ServerAdapter(
    private val onServerSelected: (VideoSource) -> Unit,
    private val onSetDefaultServer: (VideoSource) -> Unit
) : ListAdapter<ServerAdapter.ServerItem, ServerAdapter.ServerViewHolder>(ServerDiffCallback()) {

    data class ServerItem(
        val source: VideoSource,
        val isCurrentServer: Boolean,
        val isDefaultServer: Boolean
    )

    private var focusedPosition: Int = 0

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServerViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_server, parent, false)
        return ServerViewHolder(view)
    }

    override fun onBindViewHolder(holder: ServerViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    fun setFocusedPosition(position: Int) {
        focusedPosition = position
    }

    inner class ServerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val serverName: TextView = itemView.findViewById(R.id.server_name)
        private val serverDescription: TextView = itemView.findViewById(R.id.server_description)
        private val currentIndicator: View = itemView.findViewById(R.id.current_indicator)
        private val defaultStarButton: ImageButton = itemView.findViewById(R.id.default_star_button)

        fun bind(item: ServerItem) {
            val source = item.source
            
            // Set display name
            serverName.text = source.displayName
            
            // Set description if any
            val description = VideoSource.getDescription(source.id)
            if (description.isNotEmpty()) {
                serverDescription.text = description
                serverDescription.isVisible = true
            } else {
                serverDescription.isVisible = false
            }
            
            // Show current server indicator
            currentIndicator.isVisible = item.isCurrentServer
            
            // Update star button color based on default status
            val starColor = if (item.isDefaultServer) {
                ContextCompat.getColor(itemView.context, R.color.simpl_yellow)
            } else {
                ContextCompat.getColor(itemView.context, R.color.text_secondary)
            }
            defaultStarButton.setColorFilter(starColor)
            
            // Set content description for accessibility
            defaultStarButton.contentDescription = if (item.isDefaultServer) {
                "Default server. Tap to remove default"
            } else {
                "Set ${source.displayName} as default server"
            }
            
            // Click listeners
            itemView.setOnClickListener {
                onServerSelected(source)
            }
            
            defaultStarButton.setOnClickListener {
                onSetDefaultServer(source)
            }
            
            // D-pad focus handling
            itemView.setOnFocusChangeListener { _, hasFocus ->
                itemView.isSelected = hasFocus
                if (hasFocus) {
                    focusedPosition = adapterPosition
                }
            }
        }
    }

    private class ServerDiffCallback : DiffUtil.ItemCallback<ServerItem>() {
        override fun areItemsTheSame(oldItem: ServerItem, newItem: ServerItem): Boolean {
            return oldItem.source.id == newItem.source.id
        }

        override fun areContentsTheSame(oldItem: ServerItem, newItem: ServerItem): Boolean {
            return oldItem == newItem
        }
    }
}
