package com.mybrain.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.presentation.mappers.toUI
import kotlinx.coroutines.launch

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _state = MutableLiveData<PlaylistsState>()
    val state: LiveData<PlaylistsState> = _state

    init {
        observePlaylists()
    }

    private fun observePlaylists() {
        viewModelScope.launch {
            playlistsInteractor.getPlaylists().collect { playlists ->
                val uiItems = playlists.map { it.toUI() }
                _state.value =
                    if (uiItems.isEmpty()) PlaylistsState.Empty else PlaylistsState.Content(uiItems)
            }
        }
    }
}
