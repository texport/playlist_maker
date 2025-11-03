package com.mybrain.playlistmaker.presentation.mappers

import com.mybrain.playlistmaker.domain.entity.TrackSearchParams

fun String.toTrackSearchParams(): TrackSearchParams {
    return TrackSearchParams(
            term = this.trim())
}