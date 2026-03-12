package com.mybrain.playlistmaker.data.storage

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import java.io.File
import androidx.core.net.toUri

class PlaylistImageStorage(private val context: Context) {

    fun saveCover(coverUri: String?): String? {
        if (coverUri.isNullOrBlank()) return null

        val uri = coverUri.toUri()
        val inputStream = context.contentResolver.openInputStream(uri) ?: return null

        val folder = File(context.filesDir, COVERS_DIR)
        if (!folder.exists()) {
            folder.mkdirs()
        }

        val extension = getExtension(uri) ?: DEFAULT_EXTENSION
        val destFile = File(folder, "${COVER_PREFIX}${System.currentTimeMillis()}.$extension")

        inputStream.use { input ->
            destFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        return destFile.absolutePath
    }

    private fun getExtension(uri: Uri): String? {
        val mimeType = context.contentResolver.getType(uri) ?: return null
        return MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType)
    }

    companion object {
        private const val COVERS_DIR = "playlist_covers"
        private const val COVER_PREFIX = "cover_"
        private const val DEFAULT_EXTENSION = "jpg"
    }
}
