package com.mybrain.playlistmaker.domain.interactors

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams
import kotlinx.coroutines.flow.Flow

interface SearchInteractor {
    fun search(params: TrackSearchParams): Flow<Result<List<TrackDomain>>>
}
