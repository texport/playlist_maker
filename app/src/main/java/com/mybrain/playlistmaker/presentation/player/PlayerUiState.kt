package com.mybrain.playlistmaker.presentation.player

import com.mybrain.playlistmaker.presentation.entity.TrackUI

data class PlayerUiState(
    val track: TrackUI,
    val progress: String,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val isFavorite: Boolean = track.isFavorite
) {
    val isPlaying: Boolean
        get() = playbackState == PlaybackState.PLAYING

    val isPlayButtonEnabled: Boolean
        get() = playbackState != PlaybackState.IDLE
}