package com.mybrain.playlistmaker.domain.repository

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.PlaylistDomain
import kotlinx.coroutines.flow.Flow

interface PlaylistsRepository {
    suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long
    suspend fun updatePlaylist(playlistDomain: PlaylistDomain)
    fun getPlaylists(): Flow<List<PlaylistDomain>>
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
    suspend fun addTrackToPlaylist(playlistDomain: PlaylistDomain, track: TrackDomain)
}
