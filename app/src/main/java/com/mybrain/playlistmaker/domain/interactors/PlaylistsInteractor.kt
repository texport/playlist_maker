package com.mybrain.playlistmaker.domain.interactors

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.PlaylistDomain
import kotlinx.coroutines.flow.Flow

interface PlaylistsInteractor {
    suspend fun createPlaylist(name: String, description: String?, coverUri: String?): Long
    suspend fun updatePlaylist(playlistDomain: PlaylistDomain)
    fun getPlaylists(): Flow<List<PlaylistDomain>>
    fun getPlaylist(playlistId: Long): Flow<PlaylistDomain?>
    fun getPlaylistTracks(playlistId: Long): Flow<List<TrackDomain>>
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean
    suspend fun addTrackToPlaylist(playlistDomain: PlaylistDomain, track: TrackDomain)
    suspend fun deleteTrackFromPlaylist(playlistId: Long, trackId: Long)
    suspend fun deletePlaylist(playlistId: Long)
    suspend fun updatePlaylistDetails(
        playlistId: Long,
        name: String,
        description: String?,
        coverUri: String?,
        currentCoverPath: String?,
        tracksCount: Int
    )
}
