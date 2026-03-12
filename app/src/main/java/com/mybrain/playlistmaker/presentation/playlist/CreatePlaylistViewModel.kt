package com.mybrain.playlistmaker.presentation.playlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.domain.interactors.PermissionsInteractor
import com.mybrain.playlistmaker.domain.entity.PermissionStatus
import kotlinx.coroutines.launch

class CreatePlaylistViewModel(
    private val playlistsInteractor: PlaylistsInteractor,
    private val permissionsInteractor: PermissionsInteractor
) : ViewModel() {

    private val _state = MutableLiveData<CreatePlaylistState>(CreatePlaylistState.Idle)
    val state: LiveData<CreatePlaylistState> = _state

    private val _permissionState =
        MutableLiveData<PermissionUiState>(PermissionUiState.Idle)
    val permissionState: LiveData<PermissionUiState> = _permissionState

    fun createPlaylist(name: String, description: String?, coverUri: String?) {
        if (name.isBlank()) return
        viewModelScope.launch {
            playlistsInteractor.createPlaylist(name, description, coverUri)
            _state.value = CreatePlaylistState.Created(name)
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
