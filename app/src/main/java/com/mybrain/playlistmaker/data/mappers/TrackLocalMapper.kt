package com.mybrain.playlistmaker.data.mappers

import com.mybrain.playlistmaker.data.dto.TrackLocalDto
import com.mybrain.playlistmaker.domain.entity.TrackDomain

fun TrackDomain.toTrackLocalDto(): TrackLocalDto {
    return TrackLocalDto(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTimeMillis = trackTime,
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}

fun TrackLocalDto.toTrackDomain(): TrackDomain {
    return TrackDomain(
        trackId = trackId ?: 0L,
        trackName = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        trackTime = trackTimeMillis ?: 0L,
        artworkUrl100 = artworkUrl100.orEmpty(),
        collectionName = collectionName.orEmpty(),
        releaseDate = releaseDate.orEmpty(),
        primaryGenreName = primaryGenreName.orEmpty(),
        country = country.orEmpty(),
        previewUrl = previewUrl.orEmpty()
    )
}