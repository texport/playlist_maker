package com.mybrain.playlistmaker.data.mappers

import com.mybrain.playlistmaker.data.db.entity.PlaylistTrackEntity
import com.mybrain.playlistmaker.domain.entity.TrackDomain

fun TrackDomain.toPlaylistTrackEntity(playlistId: Long): PlaylistTrackEntity {
    return PlaylistTrackEntity(
        playlistId = playlistId,
        trackId = trackId,
        addedAt = System.currentTimeMillis()
    )
}
