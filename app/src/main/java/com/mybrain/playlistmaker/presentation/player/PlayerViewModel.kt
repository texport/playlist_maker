package com.mybrain.playlistmaker.presentation.player

import android.media.MediaPlayer
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.TrackUI

class PlayerViewModel(
    private val track: TrackUI,
    private val mediaPlayer: MediaPlayer
) : ViewModel() {
    private val handler = Handler(Looper.getMainLooper())
    private var shouldPlayWhenPrepared = false

    private val _uiState = MutableLiveData(
        PlayerUiState(
            track = track,
            playbackState = PlaybackState.IDLE,
            progress = "00:00"
        )
    )
    val uiState: LiveData<PlayerUiState> = _uiState

    private val progressRunnable = object : Runnable {
        override fun run() {
            val current = _uiState.value ?: return
            if (current.playbackState == PlaybackState.PLAYING) {
                val positionMs = mediaPlayer.currentPosition
                _uiState.value = current.copy(
                    progress = Utils.formatTime(positionMs)
                )
                handler.postDelayed(this, 500L)
            }
        }
    }

    init {
        preparePlayer(track.previewUrl)
    }

    fun onPlayPauseClicked() {
        val current = _uiState.value ?: return

        when (current.playbackState) {
            PlaybackState.IDLE -> {
                // ещё готовимся: когда подготовимся – сразу стартанём
                shouldPlayWhenPrepared = true
            }

            PlaybackState.PREPARED,
            PlaybackState.COMPLETED -> {
                startPlayback()
            }

            PlaybackState.PLAYING -> {
                pausePlayback()
            }

            PlaybackState.PAUSED -> {
                resumePlayback()
            }
        }
    }

    fun onScreenPaused() {
        val current = _uiState.value ?: return
        if (current.playbackState == PlaybackState.PLAYING) {
            pausePlayback()
        }
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) {
            _uiState.value = _uiState.value?.copy(
                playbackState = PlaybackState.IDLE,
                progress = "00:00"
            )
            return
        }

        mediaPlayer.setDataSource(previewUrl)

        mediaPlayer.setOnPreparedListener {
            val current = _uiState.value ?: return@setOnPreparedListener
            _uiState.value = current.copy(
                playbackState = PlaybackState.PREPARED,
                progress = "00:00"
            )

            if (shouldPlayWhenPrepared) {
                shouldPlayWhenPrepared = false
                startPlayback()
            }
        }

        mediaPlayer.setOnCompletionListener {
            val current = _uiState.value ?: return@setOnCompletionListener
            stopProgressUpdates()
            _uiState.value = current.copy(
                playbackState = PlaybackState.COMPLETED,
                progress = "00:00"
            )
        }

        mediaPlayer.prepareAsync()
    }

    private fun startPlayback() {
        mediaPlayer.start()
        val current = _uiState.value ?: return
        _uiState.value = current.copy(
            playbackState = PlaybackState.PLAYING
        )
        startProgressUpdates()
    }

    private fun resumePlayback() = startPlayback()

    private fun pausePlayback() {
        mediaPlayer.pause()
        val current = _uiState.value ?: return
        _uiState.value = current.copy(
            playbackState = PlaybackState.PAUSED
        )
        stopProgressUpdates()
    }

    private fun startProgressUpdates() {
        handler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
    }

    override fun onCleared() {
        super.onCleared()
        stopProgressUpdates()
        runCatching { mediaPlayer.stop() }
        runCatching { mediaPlayer.reset() }
        runCatching { mediaPlayer.release() }
    }
}