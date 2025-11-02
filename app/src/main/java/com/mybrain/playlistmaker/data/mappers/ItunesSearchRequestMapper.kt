package com.mybrain.playlistmaker.data.mappers

import com.mybrain.playlistmaker.data.dto.ItunesSearchRequestDto
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams

fun TrackSearchParams.toRequestDto(): ItunesSearchRequestDto  {
    return ItunesSearchRequestDto(
        term = this.term
    )
}