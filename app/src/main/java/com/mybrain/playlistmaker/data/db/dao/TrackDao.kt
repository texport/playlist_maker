package com.mybrain.playlistmaker.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.mybrain.playlistmaker.data.db.entity.TrackEntity

@Dao
interface TrackDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertTrack(track: TrackEntity): Long

    @Update
    suspend fun updateTrack(track: TrackEntity)

    @Transaction
    suspend fun upsertTrack(track: TrackEntity) {
        if (insertTrack(track) == -1L) {
            updateTrack(track)
        }
    }

    @Query("SELECT * FROM tracks WHERE trackId IN (:ids)")
    suspend fun getTracksByIds(ids: List<Long>): List<TrackEntity>

    @Query("DELETE FROM tracks WHERE trackId = :trackId")
    suspend fun deleteTrackById(trackId: Long)
}
