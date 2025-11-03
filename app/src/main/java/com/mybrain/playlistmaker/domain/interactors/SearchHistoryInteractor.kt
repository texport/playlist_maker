package com.mybrain.playlistmaker.domain.interactors

import com.mybrain.playlistmaker.domain.entity.TrackDomain

interface SearchHistoryInteractor {
    fun getAll(): List<TrackDomain>
    fun add(track: TrackDomain)
    fun clear()
}
