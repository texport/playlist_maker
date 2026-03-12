package com.mybrain.playlistmaker.domain.repository

import com.mybrain.playlistmaker.domain.entity.PermissionStatus

interface PermissionsRepository {
    suspend fun requestReadImagesPermission(): PermissionStatus
}
