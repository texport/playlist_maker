package com.mybrain.playlistmaker.domain.interactors

import com.mybrain.playlistmaker.domain.entity.TrackDomain

interface SearchInteractor {
    fun search(term: String, callback: (Result<List<TrackDomain>>) -> Unit)
}