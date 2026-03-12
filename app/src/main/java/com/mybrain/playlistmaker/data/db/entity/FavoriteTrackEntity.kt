package com.mybrain.playlistmaker.data.db.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "favorite_tracks",
    primaryKeys = ["trackId"],
    foreignKeys = [
        ForeignKey(
            entity = TrackEntity::class,
            parentColumns = ["trackId"],
            childColumns = ["trackId"],
            onDelete = ForeignKey.CASCADE,
        ),
    ],
    indices = [Index("trackId")],
)
data class FavoriteTrackEntity(
    val trackId: Long,
    val addedAt: Long
)
