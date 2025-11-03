package com.mybrain.playlistmaker.domain.interactors

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams

interface SearchInteractor {
    fun search(params: TrackSearchParams, callback: (Result<List<TrackDomain>>) -> Unit)
}