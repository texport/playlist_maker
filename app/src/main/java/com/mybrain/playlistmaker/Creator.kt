package com.mybrain.playlistmaker

import android.content.Context
import com.mybrain.playlistmaker.data.datasource.local.PrefsLocalDataSource
import com.mybrain.playlistmaker.data.datasource.local.SearchHistoryLocalDataSource
import com.mybrain.playlistmaker.data.datasource.local.ThemePreferencesLocalDataSource
import com.mybrain.playlistmaker.data.datasource.remote.ItunesRemoteDataSource
import com.mybrain.playlistmaker.data.network.api.ItunesApi
import com.mybrain.playlistmaker.data.network.client.RetrofitServiceFactory
import com.mybrain.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.mybrain.playlistmaker.data.repository.SettingsRepositoryImpl
import com.mybrain.playlistmaker.data.repository.TrackRepositoryImpl
import com.mybrain.playlistmaker.domain.interactors.SearchHistoryInteractor
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.domain.repository.SettingsRepository
import com.mybrain.playlistmaker.domain.interactors.SettingsInteractor
import com.mybrain.playlistmaker.domain.interactors.impl.SearchHistoryInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.SettingsInteractorImpl
import com.mybrain.playlistmaker.domain.repository.SearchHistoryRepository
import com.mybrain.playlistmaker.domain.repository.TrackRepository

object Creator {
    // Data Sources
    private const val ITUNES_BASE_URL = "https://itunes.apple.com/"

    private fun prefs(ctx: Context) =
        PrefsLocalDataSource(ctx.applicationContext)

    private fun themePrefs(ctx: Context) =
        ThemePreferencesLocalDataSource(prefs(ctx))

    private fun searchHistoryLocal(ctx: Context) =
        SearchHistoryLocalDataSource(prefs(ctx))

    private val itunesApi: ItunesApi by lazy {
        RetrofitServiceFactory.createApi<ItunesApi>(ITUNES_BASE_URL)
    }

    private fun itunesRemoteDataSource() =
        ItunesRemoteDataSource(itunesApi)

    // Repositories
    private fun settingsRepository(ctx: Context): SettingsRepository =
        SettingsRepositoryImpl(themePrefs(ctx))

    private fun trackRepository(): TrackRepository =
        TrackRepositoryImpl(itunesRemoteDataSource())

    private fun searchHistoryRepository(ctx: Context): SearchHistoryRepository =
        SearchHistoryRepositoryImpl(searchHistoryLocal(ctx))


    // Interactors
    fun settingsInteractor(ctx: Context): SettingsInteractor =
        SettingsInteractorImpl(settingsRepository(ctx))

    fun searchInteractor(): SearchInteractor =
        SearchInteractorImpl(trackRepository())

    fun searchHistoryInteractor(ctx: Context): SearchHistoryInteractor =
        SearchHistoryInteractorImpl(searchHistoryRepository(ctx))
}