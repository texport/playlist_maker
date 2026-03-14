package com.mybrain.playlistmaker.presentation.playlist

sealed interface CreatePlaylistState {
    data object Idle : CreatePlaylistState
    data class Created(val playlistName: String) : CreatePlaylistState
    data object Updated : CreatePlaylistState
}
