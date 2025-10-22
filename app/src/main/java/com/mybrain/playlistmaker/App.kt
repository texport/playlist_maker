package com.mybrain.playlistmaker

import android.app.Application
import com.mybrain.playlistmaker.managers.PrefsManager
import com.mybrain.playlistmaker.managers.SearchHistoryStorageManager
import com.mybrain.playlistmaker.managers.ThemeManager

class App : Application() {
    lateinit var prefs: PrefsManager
    lateinit var theme: ThemeManager
    lateinit var searchHistoryStorageManager: SearchHistoryStorageManager

    override fun onCreate() {
        super.onCreate()
        prefs = PrefsManager(this)

        theme = ThemeManager(prefs)
        theme.applySaved()

        searchHistoryStorageManager = SearchHistoryStorageManager(prefs)
    }
}