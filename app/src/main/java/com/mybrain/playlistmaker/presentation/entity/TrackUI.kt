package com.mybrain.playlistmaker.presentation.entity

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class TrackUI(
    val trackId: Long,
    val trackName: String,
    val artistName: String,
    val trackTime: String,
    val artworkUrl100: String,
    val collectionName: String,
    val releaseDate: String,
    val primaryGenreName: String,
    val country: String,
    val previewUrl: String
) : Parcelable