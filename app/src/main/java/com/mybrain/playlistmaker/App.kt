package com.mybrain.playlistmaker

import android.app.Application
import com.mybrain.playlistmaker.managers.PrefsManager
import com.mybrain.playlistmaker.managers.ThemeManager

class App : Application() {
    lateinit var prefs: PrefsManager;
    lateinit var theme: ThemeManager;

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(this)
        theme = ThemeManager(prefs)
        theme.applySaved()
    }
}