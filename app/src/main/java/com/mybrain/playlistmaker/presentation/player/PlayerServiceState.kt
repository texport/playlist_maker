package com.mybrain.playlistmaker.presentation.player

data class PlayerServiceState(
    val playbackState: PlaybackState = PlaybackState.IDLE,
    val progress: String
)
