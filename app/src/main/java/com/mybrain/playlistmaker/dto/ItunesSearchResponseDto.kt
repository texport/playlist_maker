package com.mybrain.playlistmaker.dto

data class ItunesSearchResponseDto(
    val resultCount: Int,
    val results: List<ItunesTrackDto>
)