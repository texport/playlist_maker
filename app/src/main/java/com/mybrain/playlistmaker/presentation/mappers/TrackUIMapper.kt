package com.mybrain.playlistmaker.presentation.mappers

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.presentation.entity.TrackUI

fun TrackDomain.toUI(): TrackUI {
    return TrackUI(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime.toString(),
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}

fun TrackUI.toTrackDomain(): TrackDomain {
    return TrackDomain(
        trackId = trackId,
        trackName = trackName,
        artistName = artistName,
        trackTime = trackTime.toLong(),
        artworkUrl100 = artworkUrl100,
        collectionName = collectionName,
        releaseDate = releaseDate,
        primaryGenreName = primaryGenreName,
        country = country,
        previewUrl = previewUrl
    )
}