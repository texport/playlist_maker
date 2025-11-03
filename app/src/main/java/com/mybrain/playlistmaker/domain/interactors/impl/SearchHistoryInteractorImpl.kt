package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.interactors.SearchHistoryInteractor
import com.mybrain.playlistmaker.domain.repository.SearchHistoryRepository

class SearchHistoryInteractorImpl(
    private val repository: SearchHistoryRepository
) : SearchHistoryInteractor {
    override fun getAll(): List<TrackDomain> =
        repository.get()

    override fun add(track: TrackDomain) =
        repository.add(track)

    override fun clear() =
        repository.clear()
}
