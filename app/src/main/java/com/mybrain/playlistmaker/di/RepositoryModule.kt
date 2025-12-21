package com.mybrain.playlistmaker.di

import com.mybrain.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.mybrain.playlistmaker.data.repository.SettingsRepositoryImpl
import com.mybrain.playlistmaker.data.repository.TrackRepositoryImpl
import com.mybrain.playlistmaker.domain.repository.SearchHistoryRepository
import com.mybrain.playlistmaker.domain.repository.SettingsRepository
import com.mybrain.playlistmaker.domain.repository.TrackRepository
import org.koin.dsl.module

val repositoryModule = module {
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<TrackRepository> { TrackRepositoryImpl(get()) }
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get()) }
}