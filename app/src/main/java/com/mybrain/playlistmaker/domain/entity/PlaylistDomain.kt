package com.mybrain.playlistmaker.domain.entity

data class PlaylistDomain(
    val playlistId: Long = 0,
    val name: String,
    val description: String?,
    val coverPath: String?,
    val tracksCount: Int
)