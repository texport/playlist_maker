package com.mybrain.playlistmaker.presentation.mappers

import com.mybrain.playlistmaker.domain.entity.PlaylistDomain
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI

fun PlaylistDomain.toUI(): PlaylistUI {
    return PlaylistUI(
        playlistId = playlistId,
        name = name,
        description = description,
        coverPath = coverPath,
        tracksCount = tracksCount
    )
}

fun PlaylistUI.toDomain(): PlaylistDomain {
    return PlaylistDomain(
        playlistId = playlistId,
        name = name,
        description = description,
        coverPath = coverPath,
        tracksCount = tracksCount
    )
}
