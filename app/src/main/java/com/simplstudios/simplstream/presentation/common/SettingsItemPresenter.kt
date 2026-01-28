package com.simplstudios.simplstream.presentation.common

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.leanback.widget.Presenter
import com.simplstudios.simplstream.R

/**
 * Presenter for settings/profile items
 */
class SettingsItemPresenter : Presenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_settings, parent, false)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: ViewHolder, item: Any?) {
        val settingsItem = item as? SettingsItem ?: return
        val view = viewHolder.view
        
        val iconView = view.findViewById<ImageView>(R.id.settings_icon)
        val titleView = view.findViewById<TextView>(R.id.settings_title)
        val descView = view.findViewById<TextView>(R.id.settings_description)
        
        iconView.setImageResource(settingsItem.iconRes)
        titleView.text = settingsItem.title
        
        if (settingsItem.description.isNullOrEmpty()) {
            descView.visibility = android.view.View.GONE
        } else {
            descView.visibility = android.view.View.VISIBLE
            descView.text = settingsItem.description
        }
    }

    override fun onUnbindViewHolder(viewHolder: ViewHolder) {
        val view = viewHolder.view
        view.findViewById<ImageView>(R.id.settings_icon)?.setImageDrawable(null)
    }
}
