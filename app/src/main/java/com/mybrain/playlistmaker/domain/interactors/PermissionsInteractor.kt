package com.mybrain.playlistmaker.domain.interactors

import com.mybrain.playlistmaker.domain.entity.PermissionStatus

interface PermissionsInteractor {
    suspend fun requestReadImagesPermission(): PermissionStatus
}
