package com.mybrain.playlistmaker.di

import com.google.gson.Gson
import com.mybrain.playlistmaker.data.datasource.local.PrefsLocalDataSource
import com.mybrain.playlistmaker.data.datasource.local.SearchHistoryLocalDataSource
import com.mybrain.playlistmaker.data.datasource.local.ThemePreferencesLocalDataSource
import com.mybrain.playlistmaker.data.datasource.remote.ItunesRemoteDataSource
import com.mybrain.playlistmaker.data.db.AppDatabase
import com.mybrain.playlistmaker.data.storage.PlaylistImageStorage
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

    // db
    single { 
        androidx.room.Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "database.db"
        )
            .build()
    }
    single { get<AppDatabase>().trackDao() }
    single { get<AppDatabase>().favoriteTracksDao() }
    single { get<AppDatabase>().playlistDao() }
    single { get<AppDatabase>().playlistTracksDao() }

    // storage
    single { PlaylistImageStorage(androidContext()) }
}
