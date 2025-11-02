package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.interactors.SettingsInteractor
import com.mybrain.playlistmaker.domain.repository.SettingsRepository

class SettingsInteractorImpl(
    private val repository: SettingsRepository
) : SettingsInteractor {

    override fun isDarkTheme(): Boolean = repository.isDarkTheme()

    override fun setDarkTheme(enabled: Boolean) = repository.setDarkTheme(enabled)
}