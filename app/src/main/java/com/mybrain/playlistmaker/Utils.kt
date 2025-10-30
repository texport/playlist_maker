package com.mybrain.playlistmaker

import java.util.Locale

object Utils {
    fun formatTime(ms: Int?): String {
        val totalSeconds = (ms ?: 0) / 1000
        val minutes = totalSeconds / 60
        val seconds = totalSeconds % 60
        return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
    }
}
