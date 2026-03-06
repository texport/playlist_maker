package com.mybrain.playlistmaker.domain.repository

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams
import kotlinx.coroutines.flow.Flow

interface TrackRepository {
    fun search(params: TrackSearchParams): Flow<Result<List<TrackDomain>>>
}
