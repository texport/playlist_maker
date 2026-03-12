package com.mybrain.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mybrain.playlistmaker.data.db.dao.FavoriteTracksDao
import com.mybrain.playlistmaker.data.db.dao.PlaylistDao
import com.mybrain.playlistmaker.data.db.dao.PlaylistTracksDao
import com.mybrain.playlistmaker.data.db.dao.TrackDao
import com.mybrain.playlistmaker.data.db.entity.FavoriteTrackEntity
import com.mybrain.playlistmaker.data.db.entity.PlaylistEntity
import com.mybrain.playlistmaker.data.db.entity.PlaylistTrackEntity
import com.mybrain.playlistmaker.data.db.entity.TrackEntity

@Database(
    entities = [
        TrackEntity::class,
        FavoriteTrackEntity::class,
        PlaylistEntity::class,
        PlaylistTrackEntity::class,
    ],
    version = 1,
    exportSchema = false,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun trackDao(): TrackDao
    abstract fun favoriteTracksDao(): FavoriteTracksDao
    abstract fun playlistDao(): PlaylistDao
    abstract fun playlistTracksDao(): PlaylistTracksDao
}
