package com.mybrain.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.domain.interactors.PermissionsInteractor
import com.mybrain.playlistmaker.domain.entity.PermissionStatus
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.mappers.toUI
import kotlinx.coroutines.launch
import kotlinx.coroutines.flow.first

class CreatePlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val permissionsInteractor: PermissionsInteractor
) : ViewModel() {

    private val _state = MutableLiveData<CreatePlaylistState>(CreatePlaylistState.Idle)
    val state: LiveData<CreatePlaylistState> = _state

    private val _permissionState =
        MutableLiveData<PermissionUiState>(PermissionUiState.Idle)
    val permissionState: LiveData<PermissionUiState> = _permissionState

    private val _editPlaylist = MutableLiveData<PlaylistUI?>()
    val editPlaylist: LiveData<PlaylistUI?> = _editPlaylist

    private var editTracksCount: Int = 0
    private var editCoverPath: String? = null

    fun createPlaylist(name: String, description: String?, coverUri: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistsInteractor.createPlaylist(name, description, coverUri)
            _state.value = CreatePlaylistState.Created(name)
        }
    }

    fun loadPlaylistForEdit(playlistId: Long) {
        if (playlistId <= 0) return
        viewModelScope.launch {
            val playlist = playlistsInteractor.getPlaylist(playlistId).first() ?: return@launch
            editTracksCount = playlist.tracksCount
            editCoverPath = playlist.coverPath
            _editPlaylist.value = playlist.toUI()
        }
    }

    fun updatePlaylist(playlistId: Long, name: String, description: String?, coverUri: String?) {
        if (playlistId <= 0 || name.isBlank()) return
        viewModelScope.launch {
            playlistsInteractor.updatePlaylistDetails(
                playlistId = playlistId,
                name = name,
                description = description,
                coverUri = coverUri,
                currentCoverPath = editCoverPath,
                tracksCount = editTracksCount
            )
            _state.value = CreatePlaylistState.Updated
        }
    }

    fun onPickCoverClicked() {
        viewModelScope.launch {
            _permissionState.value = when (permissionsInteractor.requestReadImagesPermission()) {
                PermissionStatus.Granted -> PermissionUiState.Granted
                PermissionStatus.NeedsRationale -> PermissionUiState.NeedsRationale
                PermissionStatus.DeniedPermanently -> PermissionUiState.DeniedPermanently
                PermissionStatus.Denied -> PermissionUiState.Denied
            }
        }
    }

    fun resetPermissionState() {
        _permissionState.value = PermissionUiState.Idle
    }

    fun resetState() {
        _state.value = CreatePlaylistState.Idle
    }
}
