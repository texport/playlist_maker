package com.mybrain.playlistmaker.data.repository

import android.Manifest
import android.os.Build
import com.markodevcic.peko.PermissionRequester
import com.markodevcic.peko.PermissionResult
import com.mybrain.playlistmaker.domain.entity.PermissionStatus
import com.mybrain.playlistmaker.domain.repository.PermissionsRepository
import kotlinx.coroutines.flow.toList

class PermissionsRepositoryImpl(
    private val permissionRequester: PermissionRequester
) : PermissionsRepository {

    override suspend fun requestReadImagesPermission(): PermissionStatus {
        val permissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_IMAGES)
        } else {
            @Suppress("DEPRECATION")
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }

        if (permissionRequester.areGranted(*permissions)) {
            return PermissionStatus.Granted
        }

        val results = permissionRequester.request(*permissions).toList()
        if (results.isEmpty()) return PermissionStatus.Denied

        if (results.any { it is PermissionResult.Denied.DeniedPermanently }) {
            return PermissionStatus.DeniedPermanently
        }
        if (results.any { it is PermissionResult.Denied.NeedsRationale }) {
            return PermissionStatus.NeedsRationale
        }
        return if (results.all { it is PermissionResult.Granted }) {
            PermissionStatus.Granted
        } else {
            PermissionStatus.Denied
        }
    }
}
