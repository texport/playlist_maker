package com.mybrain.playlistmaker.presentation.player

import com.mybrain.playlistmaker.presentation.entity.TrackUI

data class PlayerUiState(
    val track: TrackUI,
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val progress: String = "00:00"
) {
    val isPlaying: Boolean
        get() = playbackState == PlaybackState.PLAYING

    val isPlayButtonEnabled: Boolean
        get() = playbackState != PlaybackState.IDLE
}