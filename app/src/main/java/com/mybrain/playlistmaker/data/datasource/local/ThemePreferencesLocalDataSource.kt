package com.mybrain.playlistmaker.data.datasource.local

class ThemePreferencesLocalDataSource(
    private val prefs: PrefsLocalDataSource
) {
    fun isDark(): Boolean = prefs.getBoolean(KEY_DARK, false)
    fun setDark(enabled: Boolean) = prefs.putBoolean(KEY_DARK, enabled)

    companion object { private const val KEY_DARK = "dark_theme" }
}
