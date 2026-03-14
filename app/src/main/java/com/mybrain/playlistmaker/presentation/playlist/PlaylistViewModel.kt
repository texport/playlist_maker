package com.mybrain.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.domain.entity.TrackDomain
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.mappers.toUI
import java.text.SimpleDateFormat
import java.util.Locale
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val playlistId: Long,
    private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val _playlist = MutableLiveData<PlaylistUI?>()
    val playlist: LiveData<PlaylistUI?> = _playlist

    private val _tracks = MutableLiveData<List<TrackUI>>(emptyList())
    val tracks: LiveData<List<TrackUI>> = _tracks

    private val _durationMinutes = MutableLiveData<String>("00")
    val durationMinutes: LiveData<String> = _durationMinutes

    private val _playlistDeleted = MutableLiveData<String?>()
    val playlistDeleted: LiveData<String?> = _playlistDeleted

    init {
        observePlaylist()
        observeTracks()
    }

    fun deleteTrack(trackId: Long) {
        viewModelScope.launch {
            playlistsInteractor.deleteTrackFromPlaylist(playlistId, trackId)
        }
    }

    fun deletePlaylist(name: String) {
        viewModelScope.launch {
            playlistsInteractor.deletePlaylist(playlistId)
            _playlistDeleted.value = name
        }
    }

    fun clearPlaylistDeleted() {
        _playlistDeleted.value = null
    }

    private fun observePlaylist() {
        viewModelScope.launch {
            playlistsInteractor.getPlaylist(playlistId).collect { playlist ->
                _playlist.value = playlist?.toUI()
            }
        }
    }

    private fun observeTracks() {
        viewModelScope.launch {
            playlistsInteractor.getPlaylistTracks(playlistId).collect { items ->
                _tracks.value = items.map { it.toUI() }
                _durationMinutes.value = formatMinutes(items)
            }
        }
    }

    private fun formatMinutes(tracks: List<TrackDomain>): String {
        val sum = tracks.sumOf { it.trackTime }
        return SimpleDateFormat("mm", Locale.getDefault()).format(sum)
    }
}
