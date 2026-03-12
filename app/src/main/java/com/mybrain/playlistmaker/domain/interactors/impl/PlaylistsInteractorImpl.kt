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

    override suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean {
        return playlistsRepository.isTrackInPlaylist(playlistId, trackId)
    }

    override suspend fun addTrackToPlaylist(playlistDomain: PlaylistDomain, track: TrackDomain) {
        playlistsRepository.addTrackToPlaylist(playlistDomain, track)
    }
}
