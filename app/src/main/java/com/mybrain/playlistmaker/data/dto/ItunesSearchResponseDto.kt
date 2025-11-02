package com.mybrain.playlistmaker.data.dto

data class ItunesSearchResponseDto(
    val resultCount: Int,
    val results: List<ItunesTrackDto>
)