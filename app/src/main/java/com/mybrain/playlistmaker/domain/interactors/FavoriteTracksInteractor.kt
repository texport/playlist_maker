package com.mybrain.playlistmaker.domain.interactors

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import kotlinx.coroutines.flow.Flow

interface FavoriteTracksInteractor {
    suspend fun addTrackToFavorites(track: TrackDomain)
    suspend fun removeTrackFromFavorites(track: TrackDomain)
    fun getFavoriteTracks(): Flow<List<TrackDomain>>
}
