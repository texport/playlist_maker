package com.mybrain.playlistmaker.mappers

import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.dto.ItunesTrackDto
import com.mybrain.playlistmaker.models.Track

fun ItunesTrackDto.toDomain(): Track {
    return Track(
        trackId ?: 0L,
        trackName = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        trackTime = Utils.formatTime((trackTimeMillis?.toInt())),
        artworkUrl100 = artworkUrl100.orEmpty(),
        collectionName = collectionName.orEmpty(),
        releaseDate = releaseDate.orEmpty(),
        primaryGenreName = primaryGenreName.orEmpty(),
        country = country.orEmpty(),
        previewUrl = previewUrl.orEmpty()
    )
}