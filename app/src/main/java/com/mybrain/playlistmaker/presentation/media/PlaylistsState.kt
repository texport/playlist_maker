package com.mybrain.playlistmaker.presentation.media

import com.mybrain.playlistmaker.presentation.entity.PlaylistUI

sealed interface PlaylistsState {
    data object Empty : PlaylistsState
    data class Content(val playlists: List<PlaylistUI>) : PlaylistsState
}
