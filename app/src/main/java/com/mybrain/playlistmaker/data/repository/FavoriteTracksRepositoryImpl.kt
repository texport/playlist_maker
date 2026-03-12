package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.db.dao.FavoriteTracksDao
import com.mybrain.playlistmaker.data.db.dao.PlaylistTracksDao
import com.mybrain.playlistmaker.data.db.dao.TrackDao
import com.mybrain.playlistmaker.data.mappers.toDomain
import com.mybrain.playlistmaker.data.mappers.toFavoriteTrackEntity
import com.mybrain.playlistmaker.data.mappers.toTrackEntity
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.repository.FavoriteTracksRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class FavoriteTracksRepositoryImpl(
    private val favoriteTracksDao: FavoriteTracksDao,
    private val trackDao: TrackDao,
    private val playlistTracksDao: PlaylistTracksDao
) : FavoriteTracksRepository {

    override suspend fun addTrackToFavorites(track: TrackDomain) {
        trackDao.upsertTrack(track.toTrackEntity())
        favoriteTracksDao.insertFavorite(track.toFavoriteTrackEntity())
    }

    override suspend fun removeTrackFromFavorites(track: TrackDomain) {
        favoriteTracksDao.deleteFavoriteById(track.trackId)
        val stillFavorite = favoriteTracksDao.isTrackFavorite(track.trackId)
        if (stillFavorite) return
        val inAnyPlaylist = playlistTracksDao.isTrackInAnyPlaylist(track.trackId)
        if (!inAnyPlaylist) {
            trackDao.deleteTrackById(track.trackId)
        }
    }

    override fun getFavoriteTracks(): Flow<List<TrackDomain>> {
        return favoriteTracksDao.getFavoriteTracks()
            .distinctUntilChanged()
            .map { favorites ->
                if (favorites.isEmpty()) return@map emptyList()
                val ids = favorites.map { it.trackId }
                val tracks = trackDao.getTracksByIds(ids)
                val trackById = tracks.associateBy { it.trackId }
                favorites
                    .sortedByDescending { it.addedAt }
                    .mapNotNull { favorite ->
                        trackById[favorite.trackId]?.toDomain(isFavorite = true)
                    }
            }
    }

    override suspend fun getFavoriteTrackIds(): List<Long> {
        return favoriteTracksDao.getFavoriteTrackIds()
    }
}
