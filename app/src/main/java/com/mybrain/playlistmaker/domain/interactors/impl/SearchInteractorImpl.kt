package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.domain.repository.TrackRepository

class SearchInteractorImpl(
    private val repository: TrackRepository
) : SearchInteractor {
    override fun search(params: TrackSearchParams, callback: (Result<List<TrackDomain>>) -> Unit) {
        repository.search(params, callback)
    }
}
