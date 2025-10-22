package com.mybrain.playlistmaker.dto

data class ItunesTrackDto(
    val trackId: Long?,
    val trackName: String?,
    val artistName: String?,
    val trackTimeMillis: Long?,
    val artworkUrl100: String?
)