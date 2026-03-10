package com.mybrain.playlistmaker.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.mybrain.playlistmaker.data.db.dao.FavoriteTracksDao
import com.mybrain.playlistmaker.data.db.entity.TrackEntity

@Database(entities = [TrackEntity::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteTracksDao(): FavoriteTracksDao
}
