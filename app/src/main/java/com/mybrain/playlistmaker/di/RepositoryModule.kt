package com.mybrain.playlistmaker.di

import com.mybrain.playlistmaker.data.repository.FavoriteTracksRepositoryImpl
import com.mybrain.playlistmaker.data.repository.PermissionsRepositoryImpl
import com.mybrain.playlistmaker.data.repository.PlaylistsRepositoryImpl
import com.mybrain.playlistmaker.data.repository.SearchHistoryRepositoryImpl
import com.mybrain.playlistmaker.data.repository.SettingsRepositoryImpl
import com.mybrain.playlistmaker.data.repository.TrackRepositoryImpl
import com.mybrain.playlistmaker.domain.repository.FavoriteTracksRepository
import com.mybrain.playlistmaker.domain.repository.PermissionsRepository
import com.mybrain.playlistmaker.domain.repository.PlaylistsRepository
import com.mybrain.playlistmaker.domain.repository.SearchHistoryRepository
import com.mybrain.playlistmaker.domain.repository.SettingsRepository
import com.mybrain.playlistmaker.domain.repository.TrackRepository
import com.markodevcic.peko.PermissionRequester
import org.koin.dsl.module

val repositoryModule = module {
    single { PermissionRequester.instance() }
    single<SettingsRepository> { SettingsRepositoryImpl(get()) }
    single<TrackRepository> { TrackRepositoryImpl(get()) }
    single<SearchHistoryRepository> { SearchHistoryRepositoryImpl(get()) }
    single<FavoriteTracksRepository> { FavoriteTracksRepositoryImpl(get(), get(), get()) }
    single<PlaylistsRepository> { PlaylistsRepositoryImpl(get(), get(), get(), get()) }
    single<PermissionsRepository> { PermissionsRepositoryImpl(get()) }
}
