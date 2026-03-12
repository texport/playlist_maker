package com.mybrain.playlistmaker.domain.interactors.impl

import com.mybrain.playlistmaker.domain.interactors.PermissionsInteractor
import com.mybrain.playlistmaker.domain.entity.PermissionStatus
import com.mybrain.playlistmaker.domain.repository.PermissionsRepository

class PermissionsInteractorImpl(
    private val repository: PermissionsRepository
) : PermissionsInteractor {
    override suspend fun requestReadImagesPermission(): PermissionStatus {
        return repository.requestReadImagesPermission()
    }
}
