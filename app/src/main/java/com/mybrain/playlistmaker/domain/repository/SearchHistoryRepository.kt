package com.mybrain.playlistmaker.domain.repository

import com.mybrain.playlistmaker.domain.entity.TrackDomain

interface SearchHistoryRepository {
    fun get(): List<TrackDomain>
    fun add(track: TrackDomain)
    fun clear()
}