package com.mybrain.playlistmaker.domain.repository

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksRepository {
    suspend fun addTrackToFavorites(track: TrackDomain)
    suspend fun removeTrackFromFavorites(track: TrackDomain)
    fun getFavoriteTracks(): Flow<List<TrackDomain>>
}
