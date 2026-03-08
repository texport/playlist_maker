package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.db.dao.FavoriteTracksDao
import com.mybrain.playlistmaker.data.mappers.toDomain
import com.mybrain.playlistmaker.data.mappers.toTrackEntity
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(private val favoriteTracksDao: FavoriteTracksDao) :
        FavoriteTracksRepository {

    override suspend fun addTrackToFavorites(track: TrackDomain) {
        val entity = track.toTrackEntity()
        favoriteTracksDao.insertTrack(entity)
    }

    override suspend fun removeTrackFromFavorites(track: TrackDomain) {
        val entity = track.toTrackEntity()
        favoriteTracksDao.deleteTrack(entity)
    }

    override fun getFavoriteTracks(): Flow<List<TrackDomain>> {
        return favoriteTracksDao.getFavoriteTracks()
            .distinctUntilChanged()
            .map { entities ->
                entities.map { it.toDomain() }
            }
    }

    override suspend fun getFavoriteTrackIds(): List<Long> {
        return favoriteTracksDao.getFavoriteTrackIds()
    }
}
