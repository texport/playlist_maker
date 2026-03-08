package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.mybrain.playlistmaker.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow

class FavoriteTracksInteractorImpl(private val repository: FavoriteTracksRepository) :
        FavoriteTracksInteractor {

    override suspend fun addTrackToFavorites(track: TrackDomain) {
        repository.addTrackToFavorites(track)
    }

    override suspend fun removeTrackFromFavorites(track: TrackDomain) {
        repository.removeTrackFromFavorites(track)
    }

    override fun getFavoriteTracks(): Flow<List<TrackDomain>> {
        return repository.getFavoriteTracks()
    }
}
