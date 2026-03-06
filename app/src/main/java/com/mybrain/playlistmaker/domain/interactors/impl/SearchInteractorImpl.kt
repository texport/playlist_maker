package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.domain.repository.TrackRepository
import kotlinx.coroutines.flow.Flow

class SearchInteractorImpl(
    private val repository: TrackRepository
) : SearchInteractor {
    override fun search(params: TrackSearchParams): Flow<Result<List<TrackDomain>>> {
        return repository.search(params)
    }
}
