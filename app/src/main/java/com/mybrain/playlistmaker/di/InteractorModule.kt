package com.mybrain.playlistmaker.di

import com.mybrain.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.domain.interactors.PermissionsInteractor
import com.mybrain.playlistmaker.domain.interactors.SearchHistoryInteractor
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.domain.interactors.SettingsInteractor
import com.mybrain.playlistmaker.domain.interactors.impl.FavoriteTracksInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.PlaylistsInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.PermissionsInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.SearchHistoryInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.SearchInteractorImpl
import com.mybrain.playlistmaker.domain.interactors.impl.SettingsInteractorImpl
import org.koin.dsl.module

val interactorModule = module {
    single<SettingsInteractor> { SettingsInteractorImpl(get()) }
    single<SearchInteractor> { SearchInteractorImpl(get()) }
    single<SearchHistoryInteractor> { SearchHistoryInteractorImpl(get()) }
    single<FavoriteTracksInteractor> { FavoriteTracksInteractorImpl(get()) }
    single<PlaylistsInteractor> { PlaylistsInteractorImpl(get()) }
    single<PermissionsInteractor> { PermissionsInteractorImpl(get()) }
}
