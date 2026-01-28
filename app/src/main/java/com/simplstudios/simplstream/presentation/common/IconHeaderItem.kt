package com.simplstudios.simplstream.presentation.common

import android.graphics.drawable.Drawable
import androidx.leanback.widget.HeaderItem

/**
 * Custom header item with icon support for Leanback sidebar
 */
class IconHeaderItem(
    id: Long,
    name: String,
    val icon: Drawable? = null
) : HeaderItem(id, name)
