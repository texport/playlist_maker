package com.mybrain.playlistmaker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mybrain.playlistmaker.data.db.entity.PlaylistTrackEntity

@Dao
interface PlaylistTracksDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: PlaylistTrackEntity): Long

    @Query(
        """
        SELECT EXISTS(
            SELECT 1 FROM playlist_tracks WHERE playlistId = :playlistId AND trackId = :trackId
        )
        """
    )
    suspend fun isTrackInPlaylist(playlistId: Long, trackId: Long): Boolean

    @Query("SELECT EXISTS(SELECT 1 FROM playlist_tracks WHERE trackId = :trackId)")
    suspend fun isTrackInAnyPlaylist(trackId: Long): Boolean

    @Query("SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = :playlistId")
    suspend fun getTrackCount(playlistId: Long): Int
}
