package com.mybrain.playlistmaker.data.repository

import com.mybrain.playlistmaker.data.datasource.local.SearchHistoryLocalDataSource
import com.mybrain.playlistmaker.data.mappers.toTrackDomain
import com.mybrain.playlistmaker.data.mappers.toTrackLocalDto
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryRepositoryImpl(
    private val local: SearchHistoryLocalDataSource
) : SearchHistoryRepository {

    override fun get(): List<TrackDomain> =
        local.get().map { it.toTrackDomain() }

    override fun add(track: TrackDomain) {
        local.add(track.toTrackLocalDto())
    }

    override fun clear() = local.clear()
}
