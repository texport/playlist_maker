package com.mybrain.playlistmaker.managers

import androidx.appcompat.app.AppCompatDelegate

class ThemeManager(private val prefs: PrefsManager) {
    companion object { private const val KEY_DARK = "dark_theme" }

    fun applySaved() {
        setDark(isDark())
    }

    fun isDark(): Boolean = prefs.getBoolean(KEY_DARK, false)

    fun setDark(enabled: Boolean) {
        prefs.putBoolean(KEY_DARK, enabled)
        AppCompatDelegate.setDefaultNightMode(
            if (enabled) AppCompatDelegate.MODE_NIGHT_YES
            else AppCompatDelegate.MODE_NIGHT_NO
        )
    }
}
