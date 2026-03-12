package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.db.dao.PlaylistDao
import com.mybrain.playlistmaker.data.db.dao.PlaylistTracksDao
import com.mybrain.playlistmaker.data.db.dao.TrackDao
import com.mybrain.playlistmaker.data.mappers.toDomain
import com.mybrain.playlistmaker.data.mappers.toEntity
import com.mybrain.playlistmaker.data.mappers.toPlaylistTrackEntity
import com.mybrain.playlistmaker.data.mappers.toTrackEntity
import com.mybrain.playlistmaker.data.storage.PlaylistImageStorage
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.PlaylistDomain
import com.mybrain.playlistmaker.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map

class PlaylistsRepositoryImpl(
    private val playlistDao: PlaylistDao,
    private val playlistTracksDao: PlaylistTracksDao,
    private val trackDao: TrackDao,
    private val imageStorage: PlaylistImageStorage
) : PlaylistsRepository {

    override suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long {
        val coverPath = imageStorage.saveCover(coverUri)
        val entity = PlaylistDomain(
            name = name,
            description = description,
            coverPath = coverPath,
            tracksCount = 0
        ).toEntity()
        return playlistDao.insertPlaylist(entity)
    }

    override suspend fun updatePlaylist(playlistDomain: PlaylistDomain) {
        playlistDao.updatePlaylist(playlistDomain.toEntity())
    }

    override fun getPlaylists(): Flow<List<PlaylistDomain>> {
        return playlistDao.getPlaylists()
            .distinctUntilChanged()
            .map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistTracksDao.isTrackInPlaylist(playlistId, trackId)
    }

    override suspend fun addTrackToPlaylist(playlistDomain: PlaylistDomain, track: TrackDomain) {
        trackDao.upsertTrack(track.toTrackEntity())
        val inserted = playlistTracksDao.insertTrack(track.toPlaylistTrackEntity(playlistDomain.playlistId))
        if (inserted != -1L) {
            val count = playlistTracksDao.getTrackCount(playlistDomain.playlistId)
            playlistDao.updateTracksCount(playlistDomain.playlistId, count)
        }
    }
}
