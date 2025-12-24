package com.mybrain.playlistmaker.di

import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.mybrain.playlistmaker.presentation.media.PlaylistsViewModel
import com.mybrain.playlistmaker.presentation.player.PlayerViewModel
import com.mybrain.playlistmaker.presentation.search.SearchViewModel
import com.mybrain.playlistmaker.presentation.settings.SettingsViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val viewModelModule = module {
    viewModel { SearchViewModel(get(), get()) }
    viewModel { SettingsViewModel(get()) }
    viewModel { (track: TrackUI) ->
        PlayerViewModel(track, get())
    }
    viewModel { PlaylistsViewModel() }
    viewModel { FavoriteTracksViewModel() }
}