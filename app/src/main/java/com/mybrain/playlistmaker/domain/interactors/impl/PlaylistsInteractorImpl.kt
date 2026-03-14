package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.domain.entity.PlaylistDomain
import com.mybrain.playlistmaker.domain.repository.PlaylistsRepository
import kotlinx.coroutines.flow.Flow

class PlaylistsInteractorImpl(
    private val playlistsRepository: PlaylistsRepository
) : PlaylistsInteractor {
    override suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long {
        return playlistsRepository.createPlaylist(name, description, coverUri)
    }

    override suspend fun updatePlaylist(playlistDomain: PlaylistDomain) {
        playlistsRepository.updatePlaylist(playlistDomain)
    }

    override fun getPlaylists(): Flow<List<PlaylistDomain>> {
        return playlistsRepository.getPlaylists()
    }

    override fun getPlaylist(playlistId: Long): Flow<PlaylistDomain?> {
        return playlistsRepository.getPlaylist(playlistId)
    }

    override fun getPlaylistTracks(playlistId: Long): Flow<List<TrackDomain>> {
        return playlistsRepository.getPlaylistTracks(playlistId)
    }

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistsRepository.isTrackInPlaylist(playlistId, trackId)
    }

    override suspend fun addTrackToPlaylist(playlistDomain: PlaylistDomain, track: TrackDomain) {
        playlistsRepository.addTrackToPlaylist(playlistDomain, track)
    }

    override suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long) {
        playlistsRepository.deleteTrackFromPlaylist(playlistId, trackId)
    }

    override suspend fun deletePlaylist(playlistId: Long) {
        playlistsRepository.deletePlaylist(playlistId)
    }

    override suspend fun updatePlaylistDetails(
        playlistId: Long,
        name: String,
        description: String?,
        coverUri: String?,
        currentCoverPath: String?,
        tracksCount: Int
    ) {
        playlistsRepository.updatePlaylistDetails(
            playlistId = playlistId,
            name = name,
            description = description,
            coverUri = coverUri,
            currentCoverPath = currentCoverPath,
            tracksCount = tracksCount
        )
    }
}
