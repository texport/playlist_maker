package com.mybrain.playlistmaker.presentation.entity

data class PlaylistUI(
    val playlistId: Long,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val tracksCount: Int
)
