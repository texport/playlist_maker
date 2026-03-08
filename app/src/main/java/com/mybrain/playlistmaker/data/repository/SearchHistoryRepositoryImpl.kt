package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.datasource.local.SearchHistoryLocalDataSource
import com.mybrain.playlistmaker.data.mappers.toTrackDomain
import com.mybrain.playlistmaker.data.mappers.toTrackLocalDto
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val local: SearchHistoryLocalDataSource,
    private val appDatabase: com.mybrain.playlistmaker.data.db.AppDatabase
) : SearchHistoryRepository {

    override fun get(): List<TrackDomain> {
        val tracks = local.get().map { it.toTrackDomain() }
        val favoriteIds = kotlinx.coroutines.runBlocking(kotlinx.coroutines.Dispatchers.IO) {
            appDatabase.favoriteTracksDao().getFavoriteTrackIds()
        }
        return tracks.map { it.apply { isFavorite = favoriteIds.contains(trackId) } }
    }

    override fun add(track: TrackDomain) {
        local.add(track.toTrackLocalDto())
    }

    override fun clear() = local.clear()
}
