package com.mybrain.playlistmaker.presentation.player

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.media.MediaPlayer
import android.os.Binder
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AudioPlayerService : Service(), PlayerServiceController {

    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val localBinder = LocalBinder()
    private val mediaPlayer = MediaPlayer()
    private lateinit var progressPlaceholder: String
    private lateinit var state: MutableStateFlow<PlayerServiceState>

    private var progressJob: Job? = null
    private var shouldPlayWhenPrepared = false
    private var isPrepared = false
    private var isForegroundNotificationVisible = false
    private var previewUrl: String = ""
    private var artistName: String = ""
    private var trackName: String = ""

    override fun onCreate() {
        super.onCreate()
        progressPlaceholder = getString(R.string.playback_progress_placeholder)
        state = MutableStateFlow(PlayerServiceState(progress = progressPlaceholder))
        createNotificationChannel()
    }

    override fun onBind(intent: Intent?): IBinder {
        previewUrl = intent?.getStringExtra(EXTRA_PREVIEW_URL).orEmpty()
        artistName = intent?.getStringExtra(EXTRA_ARTIST_NAME).orEmpty()
        trackName = intent?.getStringExtra(EXTRA_TRACK_NAME).orEmpty()
        preparePlayer(previewUrl)
        return localBinder
    }

    override fun onUnbind(intent: Intent?): Boolean {
        hidePlaybackNotification()
        stopPlayback()
        stopSelf()
        return super.onUnbind(intent)
    }

    override fun onDestroy() {
        releasePlayer()
        serviceScope.cancel()
        super.onDestroy()
    }

    override fun startPlayback() {
        if (previewUrl.isBlank()) return

        if (!isPrepared) {
            shouldPlayWhenPrepared = true
            return
        }

        if (state.value.playbackState == PlaybackState.COMPLETED) {
            mediaPlayer.seekTo(0)
        }

        mediaPlayer.start()
        state.value = state.value.copy(playbackState = PlaybackState.PLAYING)
        startProgressUpdates()
    }

    override fun pausePlayback() {
        if (!mediaPlayer.isPlaying) return
        mediaPlayer.pause()
        state.value = state.value.copy(playbackState = PlaybackState.PAUSED)
        stopProgressUpdates()
    }

    override fun playbackState(): StateFlow<PlayerServiceState> = state.asStateFlow()

    override fun showPlaybackNotification() {
        if (state.value.playbackState != PlaybackState.PLAYING || isForegroundNotificationVisible) {
            return
        }

        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            createPlaybackNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
        )
        isForegroundNotificationVisible = true
    }

    override fun hidePlaybackNotification() {
        if (!isForegroundNotificationVisible) return
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        isForegroundNotificationVisible = false
    }

    private fun preparePlayer(url: String) {
        stopProgressUpdates()
        shouldPlayWhenPrepared = false
        isPrepared = false
        runCatching { mediaPlayer.reset() }

        if (url.isBlank()) {
            state.value =
                    PlayerServiceState(playbackState = PlaybackState.IDLE, progress = progressPlaceholder)
            return
        }

        mediaPlayer.setDataSource(url)
        mediaPlayer.setOnPreparedListener {
            isPrepared = true
            state.value =
                    state.value.copy(playbackState = PlaybackState.PREPARED, progress = progressPlaceholder)

            if (shouldPlayWhenPrepared) {
                shouldPlayWhenPrepared = false
                startPlayback()
            }
        }
        mediaPlayer.setOnCompletionListener {
            stopProgressUpdates()
            state.value =
                    state.value.copy(playbackState = PlaybackState.COMPLETED, progress = progressPlaceholder)
            hidePlaybackNotification()
        }
        mediaPlayer.prepareAsync()
    }

    private fun startProgressUpdates() {
        stopProgressUpdates()
        progressJob = serviceScope.launch {
            while (mediaPlayer.isPlaying) {
                state.value = state.value.copy(progress = Utils.formatTime(mediaPlayer.currentPosition))
                delay(UPDATE_DELAY_MS)
            }
        }
    }

    private fun stopProgressUpdates() {
        progressJob?.cancel()
        progressJob = null
    }

    private fun releasePlayer() {
        stopPlayback()
        runCatching { mediaPlayer.release() }
    }

    private fun stopPlayback() {
        stopProgressUpdates()
        runCatching { mediaPlayer.stop() }
        runCatching { mediaPlayer.reset() }
        isPrepared = false
        shouldPlayWhenPrepared = false
        state.value = PlayerServiceState(playbackState = PlaybackState.IDLE, progress = progressPlaceholder)
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            getString(R.string.app_name),
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(channel)
    }

    private fun createPlaybackNotification(): Notification {
        return NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_media_24)
            .setContentTitle(getString(R.string.app_name))
            .setContentText("$artistName - $trackName")
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    companion object {
        const val EXTRA_PREVIEW_URL = "extra_preview_url"
        const val EXTRA_ARTIST_NAME = "extra_artist_name"
        const val EXTRA_TRACK_NAME = "extra_track_name"

        private const val UPDATE_DELAY_MS = 300L
        private const val NOTIFICATION_ID = 1001
        private const val NOTIFICATION_CHANNEL_ID = "player_playback_channel"
    }
}
