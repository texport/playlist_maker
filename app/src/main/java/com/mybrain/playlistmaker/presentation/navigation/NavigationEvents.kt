package com.mybrain.playlistmaker.presentation.navigation

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

class NavigationEvents {
    private val _playlistCreated =
        MutableSharedFlow<String>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val playlistCreated: SharedFlow<String> = _playlistCreated.asSharedFlow()

    private val _playlistDeleted =
        MutableSharedFlow<String>(
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
    val playlistDeleted: SharedFlow<String> = _playlistDeleted.asSharedFlow()

    fun notifyPlaylistCreated(name: String) {
        _playlistCreated.tryEmit(name)
    }

    fun notifyPlaylistDeleted(name: String) {
        _playlistDeleted.tryEmit(name)
    }
}
