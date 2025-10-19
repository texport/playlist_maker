package com.mybrain.playlistmaker.mappers

import com.mybrain.playlistmaker.dto.ItunesTrackDto
import com.mybrain.playlistmaker.models.Track
import java.util.Locale

fun ItunesTrackDto.toDomain(): Track {
    val totalSec = (trackTimeMillis ?: 0L) / 1000
    val m = totalSec / 60
    val s = totalSec % 60

    return Track(
        trackName = trackName.orEmpty(),
        artistName = artistName.orEmpty(),
        trackTime = String.format(Locale.getDefault(), "%02d:%02d", m, s),
        artworkUrl100 = artworkUrl100.orEmpty()
    )
}