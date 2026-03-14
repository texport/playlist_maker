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

    override fun getPlaylist(playlistId: Long): Flow<PlaylistDomain?> {
        return playlistDao.getPlaylistById(playlistId)
            .distinctUntilChanged()
            .map { it?.toDomain() }
    }

    override fun getPlaylistTracks(playlistId: Long): Flow<List<TrackDomain>> {
        return playlistTracksDao.getTrackIdsByPlaylist(playlistId)
            .distinctUntilChanged()
            .map { ids ->
                if (ids.isEmpty()) return@map emptyList()
                val tracks = trackDao.getAllTracks().associateBy { it.trackId }
                ids.mapNotNull { trackId -> tracks[trackId]?.toDomain() }
            }
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

    override suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistTracksDao.deleteTrackFromPlaylist(playlistId, trackId)
        val count = playlistTracksDao.getTrackCount(playlistId)
        playlistDao.updateTracksCount(playlistId, count)
        if (!playlistTracksDao.isTrackInAnyPlaylist(trackId)) {
            trackDao.deleteTrackById(trackId)
        }
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        val trackIds = playlistTracksDao.getTrackIdsByPlaylistOnce(playlistId)
        playlistDao.deletePlaylistById(playlistId)
        trackIds.forEach { trackId ->
            if (!playlistTracksDao.isTrackInAnyPlaylist(trackId)) {
                trackDao.deleteTrackById(trackId)
            }
        }
    }

    override suspend fun updatePlaylistDetails(
        playlistId: Long,
        name: String,
        description: String?,
        coverUri: String?,
        currentCoverPath: String?,
        tracksCount: Int
    ) {
        val normalizedPath = coverUri?.let { android.net.Uri.parse(it).path }
        val coverPath = if (
            coverUri.isNullOrBlank() ||
            coverUri == currentCoverPath ||
            (normalizedPath != null && normalizedPath == currentCoverPath)
        ) {
            currentCoverPath
        } else {
            imageStorage.saveCover(coverUri)
        }
        val entity = PlaylistDomain(
            playlistId = playlistId,
            name = name,
            description = description,
            coverPath = coverPath,
            tracksCount = tracksCount
        ).toEntity()
        playlistDao.updatePlaylist(entity)
    }
}
