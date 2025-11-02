package com.mybrain.playlistmaker.domain.repository

interface SettingsRepository {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}