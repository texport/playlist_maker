package com.mybrain.playlistmaker.presentation.player

import kotlinx.coroutines.flow.StateFlow

interface PlayerServiceController {
    fun startPlayback()
    fun pausePlayback()
    fun playbackState(): StateFlow<PlayerServiceState>
    fun showPlaybackNotification()
    fun hidePlaybackNotification()
}
