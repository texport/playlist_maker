package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.datasource.local.ThemePreferencesLocalDataSource
import com.mybrain.playlistmaker.domain.repository.SettingsRepository

class SettingsRepositoryImpl(
    private val themePrefs: ThemePreferencesLocalDataSource
) : SettingsRepository {

    override fun isDarkTheme(): Boolean = themePrefs.isDark()
    override fun setDarkTheme(enabled: Boolean) = themePrefs.setDark(enabled)
}
