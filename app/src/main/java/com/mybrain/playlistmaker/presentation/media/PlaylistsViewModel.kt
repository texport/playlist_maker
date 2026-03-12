package com.mybrain.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.mappers.toUI
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class PlaylistsViewModel(
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _state = MutableLiveData<PlaylistsState>()
    val state: LiveData<PlaylistsState> = _state

    private val _shareEvent = MutableLiveData<PlaylistsShareEvent?>()
    val shareEvent: LiveData<PlaylistsShareEvent?> = _shareEvent

    private val _playlistDeleted = MutableLiveData<String?>()
    val playlistDeleted: LiveData<String?> = _playlistDeleted

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

    fun requestShare(playlist: PlaylistUI) {
        viewModelScope.launch {
            val tracks = playlistsInteractor.getPlaylistTracks(playlist.playlistId).first()
            if (tracks.isEmpty()) {
                _shareEvent.value = PlaylistsShareEvent.Empty
            } else {
                val uiTracks = tracks.map { it.toUI() }
                _shareEvent.value = PlaylistsShareEvent.Ready(playlist, uiTracks)
            }
        }
    }

    fun clearShareEvent() {
        _shareEvent.value = null
    }

    fun deletePlaylist(playlistId: Long, name: String) {
        viewModelScope.launch {
            playlistsInteractor.deletePlaylist(playlistId)
            _playlistDeleted.value = name
        }
    }

    fun clearPlaylistDeleted() {
        _playlistDeleted.value = null
    }
}

sealed interface PlaylistsShareEvent {
    data class Ready(val playlist: PlaylistUI, val tracks: List<TrackUI>) : PlaylistsShareEvent
    data object Empty : PlaylistsShareEvent
}
