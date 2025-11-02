package com.mybrain.playlistmaker

import android.app.Application
import com.mybrain.playlistmaker.presentation.utils.ThemeManagerUI

class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val isDark = Creator.settingsInteractor(this).isDarkTheme()
        ThemeManagerUI.apply(isDark)
    }
}