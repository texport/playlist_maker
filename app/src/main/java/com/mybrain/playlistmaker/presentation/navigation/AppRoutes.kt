package com.mybrain.playlistmaker.presentation.navigation

import com.mybrain.playlistmaker.presentation.entity.TrackUI

object AppRoutes {
    const val MEDIA = "media"
    const val SEARCH = "search"
    const val SETTINGS = "settings"

    const val PLAYER = "player/{track}"

    const val PLAYLIST = "playlist/{playlistId}"

    const val CREATE_PLAYLIST = "createPlaylist/{playlistId}"

    fun playerRoute(track: TrackUI): String =
        "player/${TrackUiNavType.serializeAsValue(track)}"

    fun playlistRoute(playlistId: Long): String = "playlist/$playlistId"

    fun createPlaylistRoute(playlistId: Long): String = "createPlaylist/$playlistId"
}
