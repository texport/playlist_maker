package com.mybrain.playlistmaker.data.mappers

import com.mybrain.playlistmaker.data.db.entity.PlaylistEntity
import com.mybrain.playlistmaker.domain.entity.PlaylistDomain

fun PlaylistEntity.toDomain(): PlaylistDomain {
    return PlaylistDomain(
        playlistId = playlistId,
        name = name,
        description = description,
        coverPath = coverPath,
        tracksCount = tracksCount
    )
}

fun PlaylistDomain.toEntity(): PlaylistEntity {
    return PlaylistEntity(
        playlistId = playlistId,
        name = name,
        description = description,
        coverPath = coverPath,
        tracksCount = tracksCount
    )
}
