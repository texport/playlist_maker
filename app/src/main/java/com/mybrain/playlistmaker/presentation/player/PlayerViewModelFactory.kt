package com.mybrain.playlistmaker.presentation.player

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.mybrain.playlistmaker.presentation.entity.TrackUI

class PlayerViewModelFactory(
    private val track: TrackUI
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(track) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}