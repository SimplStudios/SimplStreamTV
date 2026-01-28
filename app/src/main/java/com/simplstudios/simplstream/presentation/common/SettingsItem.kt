package com.simplstudios.simplstream.presentation.common

import androidx.annotation.DrawableRes

/**
 * Settings/profile item for the sidebar
 */
data class SettingsItem(
    val id: Int,
    val title: String,
    val description: String? = null,
    @DrawableRes val iconRes: Int
) {
    companion object {
        const val ID_SWITCH_PROFILE = 1
        const val ID_SIGN_OUT = 2
        const val ID_SETTINGS = 3
        const val ID_MY_LIST = 4
    }
}
