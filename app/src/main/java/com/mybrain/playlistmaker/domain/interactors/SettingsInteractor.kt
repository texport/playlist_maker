package com.mybrain.playlistmaker.domain.interactors

interface SettingsInteractor {
    fun isDarkTheme(): Boolean
    fun setDarkTheme(enabled: Boolean)
}