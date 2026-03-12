package com.mybrain.playlistmaker.presentation.playlist

sealed interface PermissionUiState {
    data object Idle : PermissionUiState
    data object Granted : PermissionUiState
    data object Denied : PermissionUiState
    data object NeedsRationale : PermissionUiState
    data object DeniedPermanently : PermissionUiState
}
