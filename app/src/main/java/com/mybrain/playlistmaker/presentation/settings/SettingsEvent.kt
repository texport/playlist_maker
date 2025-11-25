package com.mybrain.playlistmaker.presentation.settings

sealed interface SettingsEvent {
    object Share : SettingsEvent
    object Support : SettingsEvent
    object License : SettingsEvent
}
