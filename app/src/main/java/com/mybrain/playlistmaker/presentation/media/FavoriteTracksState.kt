package com.mybrain.playlistmaker.presentation.media

import com.mybrain.playlistmaker.presentation.entity.TrackUI

sealed interface FavoriteTracksState {
    data object Empty : FavoriteTracksState
    data class Content(val tracks: List<TrackUI>) : FavoriteTracksState
}
