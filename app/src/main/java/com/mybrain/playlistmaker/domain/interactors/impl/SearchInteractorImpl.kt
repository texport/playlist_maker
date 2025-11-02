package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.domain.repository.TrackRepository
import com.mybrain.playlistmaker.presentation.mappers.toTrackSearchParams

class SearchInteractorImpl(
    private val repository: TrackRepository
) : SearchInteractor {
    override fun search(term: String, callback: (Result<List<TrackDomain>>) -> Unit) {
        repository.search(term.toTrackSearchParams(), callback)
    }
}
