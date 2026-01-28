package com.simplstudios.simplstream.presentation.common

import android.graphics.drawable.Drawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.leanback.widget.Presenter
import androidx.leanback.widget.RowHeaderPresenter
import com.simplstudios.simplstream.R

/**
 * Custom header presenter with icon support
 */
class IconHeaderPresenter : RowHeaderPresenter() {

    override fun onCreateViewHolder(parent: ViewGroup): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_icon_header, parent, false)
        view.isFocusable = true
        view.isFocusableInTouchMode = true
        return ViewHolder(view)
    }

    override fun onBindViewHolder(viewHolder: Presenter.ViewHolder, item: Any?) {
        val headerItem = item as? IconHeaderItem
        val view = viewHolder.view
        
        val iconView = view.findViewById<ImageView>(R.id.header_icon)
        val textView = view.findViewById<TextView>(R.id.header_title)
        
        textView.text = headerItem?.name ?: ""
        
        if (headerItem?.icon != null) {
            iconView.setImageDrawable(headerItem.icon)
            iconView.visibility = View.VISIBLE
        } else {
            iconView.visibility = View.GONE
        }
    }

    override fun onUnbindViewHolder(viewHolder: Presenter.ViewHolder) {
        val view = viewHolder.view
        view.findViewById<ImageView>(R.id.header_icon)?.setImageDrawable(null)
    }
}
