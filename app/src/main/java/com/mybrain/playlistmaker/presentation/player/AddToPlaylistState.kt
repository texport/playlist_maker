package com.mybrain.playlistmaker.presentation.player

sealed interface AddToPlaylistState {
    data class Added(val playlistName: String) : AddToPlaylistState
    data class AlreadyAdded(val playlistName: String) : AddToPlaylistState
}
