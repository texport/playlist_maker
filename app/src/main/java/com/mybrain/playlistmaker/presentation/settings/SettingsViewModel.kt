package com.mybrain.playlistmaker.presentation.settings

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mybrain.playlistmaker.domain.interactors.SettingsInteractor
import com.mybrain.playlistmaker.presentation.utils.ThemeManagerUI

class SettingsViewModel(
    private val settingsInteractor: SettingsInteractor
) : ViewModel() {
    private val _state = MutableLiveData<SettingsState>()
    private val _events = MutableLiveData<SettingsEvent>()
    val state: LiveData<SettingsState> = _state
    val events: LiveData<SettingsEvent> = _events

    init {
        val isDark = settingsInteractor.isDarkTheme()
        _state.value = SettingsState(isDark)
    }

    fun onThemeSwitched(isDark: Boolean) {
        settingsInteractor.setDarkTheme(isDark)
        ThemeManagerUI.apply(isDark)
        _state.value = SettingsState(isDark)
    }

    fun onShareClicked() {
        _events.value = SettingsEvent.Share
    }

    fun onSupportClicked() {
        _events.value = SettingsEvent.Support
    }

    fun onLicenseClicked() {
        _events.value = SettingsEvent.License
    }
}