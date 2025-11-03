package com.mybrain.playlistmaker.domain.repository

import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.entity.TrackSearchParams

interface TrackRepository {
    fun search(
        params: TrackSearchParams,
        onResult: (result: Result<List<TrackDomain>>) -> Unit
    )
}