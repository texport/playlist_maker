package com.mybrain.playlistmaker.data.mappers

import com.mybrain.playlistmaker.data.db.entity.FavoriteTrackEntity
import com.mybrain.playlistmaker.data.db.entity.TrackEntity
import com.mybrain.playlistmaker.domain.entity.TrackDomain

fun TrackDomain.toTrackEntity(): TrackEntity {
    return TrackEntity(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}

fun TrackDomain.toFavoriteTrackEntity(): FavoriteTrackEntity {
    return FavoriteTrackEntity(
        trackId = trackId,
        addedAt = System.currentTimeMillis()
    )
}

fun TrackEntity.toDomain(isFavorite: Boolean = false): TrackDomain {
    return TrackDomain(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl,
        isFavorite = isFavorite
    )
}
