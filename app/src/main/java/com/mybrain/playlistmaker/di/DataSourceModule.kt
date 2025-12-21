package com.mybrain.playlistmaker.di

import com.google.gson.Gson
import com.mybrain.playlistmaker.data.datasource.local.PrefsLocalDataSource
import com.mybrain.playlistmaker.data.datasource.local.SearchHistoryLocalDataSource
import com.mybrain.playlistmaker.data.datasource.local.ThemePreferencesLocalDataSource
import com.mybrain.playlistmaker.data.datasource.remote.ItunesRemoteDataSource
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataSourceModule = module {
    single { Gson() }

    // local
    single { PrefsLocalDataSource(androidContext()) }
    single { ThemePreferencesLocalDataSource(get()) }
    single { SearchHistoryLocalDataSource(get(), get()) }

    // remote
    single { ItunesRemoteDataSource(get()) }
}