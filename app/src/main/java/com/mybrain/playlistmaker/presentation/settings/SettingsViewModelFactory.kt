package com.mybrain.playlistmaker.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mybrain.playlistmaker.Creator

class SettingsViewModelFactory(
    context: Context
) : ViewModelProvider.Factory {
    private val appContext = context.applicationContext

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(SettingsViewModel::class.java)) {
            return SettingsViewModel(
                settingsInteractor = Creator.settingsInteractor(appContext)
            ) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}