package com.mybrain.playlistmaker.data.db.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mybrain.playlistmaker.data.db.entity.TrackEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteTracksDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrack(track: TrackEntity)

    @Delete
    suspend fun deleteTrack(track: TrackEntity)

    @Query("SELECT * FROM favorite_tracks ORDER BY timestamp DESC")
    fun getFavoriteTracks(): Flow<List<TrackEntity>>

    @Query("SELECT trackId FROM favorite_tracks")
    suspend fun getFavoriteTrackIds(): List<Long>
}
