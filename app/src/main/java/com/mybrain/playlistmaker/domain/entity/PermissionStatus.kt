package com.mybrain.playlistmaker.domain.entity

sealed interface PermissionStatus {
    data object Granted : PermissionStatus
    data object Denied : PermissionStatus
    data object NeedsRationale : PermissionStatus
    data object DeniedPermanently : PermissionStatus
}