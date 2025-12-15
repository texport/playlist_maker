package com.mybrain.playlistmaker

import android.app.Application
import com.mybrain.playlistmaker.di.dataSourceModule
import com.mybrain.playlistmaker.di.interactorModule
import com.mybrain.playlistmaker.di.networkModule
import com.mybrain.playlistmaker.di.playerModule
import com.mybrain.playlistmaker.di.repositoryModule
import com.mybrain.playlistmaker.di.viewModelModule
import com.mybrain.playlistmaker.domain.interactors.SettingsInteractor
import com.mybrain.playlistmaker.presentation.utils.ThemeManagerUI
import org.koin.android.ext.android.getKoin
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    private val settingsInteractor: SettingsInteractor by lazy { getKoin().get() }

    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@App)
            modules(
                networkModule,
                dataSourceModule,
                repositoryModule,
                interactorModule,
                playerModule,
                viewModelModule
            )
        }

        ThemeManagerUI.apply(settingsInteractor.isDarkTheme())
    }
}