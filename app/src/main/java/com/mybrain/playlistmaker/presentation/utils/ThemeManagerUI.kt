package com.mybrain.playlistmaker.presentation.utils

import androidx.appcompat.app.AppCompatDelegate

object ThemeManagerUI {
    fun apply(isDark: Boolean) {
        AppCompatDelegate.setDefaultNightMode(
            if (isDark) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}