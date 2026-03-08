package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.db.AppDatabase
import com.mybrain.playlistmaker.data.mappers.toDomain
import com.mybrain.playlistmaker.data.mappers.toTrackEntity
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(private val appDatabase: AppDatabase) :
        FavoriteTracksRepository {

    override suspend fun addTrackToFavorites(track: TrackDomain) {
        val entity = track.toTrackEntity()
        appDatabase.favoriteTracksDao().insertTrack(entity)
    }

    override suspend fun removeTrackFromFavorites(track: TrackDomain) {
        val entity = track.toTrackEntity()
        appDatabase.favoriteTracksDao().deleteTrack(entity)
    }

    override fun getFavoriteTracks(): Flow<List<TrackDomain>> {
        return appDatabase.favoriteTracksDao().getFavoriteTracks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
}
