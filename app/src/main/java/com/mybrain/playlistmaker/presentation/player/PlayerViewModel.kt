package com.mybrain.playlistmaker.presentation.player

import android.media.MediaPlayer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.mappers.toDomain
import com.mybrain.playlistmaker.presentation.mappers.toTrackDomain
import com.mybrain.playlistmaker.presentation.mappers.toUI
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class PlayerViewModel(
        private val track: TrackUI,
        private val mediaPlayer: MediaPlayer,
        private val favoriteTracksInteractor: FavoriteTracksInteractor,
        private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private var timerJob: Job? = null
    private var shouldPlayWhenPrepared = false

    private val _uiState =
            MutableLiveData(
                    PlayerUiState(
                            track = track,
                            playbackState = PlaybackState.IDLE,
                            progress = "00:00",
                            isFavorite = track.isFavorite
                    )
            )
    val uiState: LiveData<PlayerUiState> = _uiState

    private val _playlists = MutableLiveData<List<PlaylistUI>>()
    val playlists: LiveData<List<PlaylistUI>> = _playlists

    private val _addToPlaylistState = MutableLiveData<AddToPlaylistState?>()
    val addToPlaylistState: LiveData<AddToPlaylistState?> = _addToPlaylistState

    init {
        preparePlayer(track.previewUrl)
        viewModelScope.launch {
            val inFavorites = favoriteTracksInteractor.isTrackInFavorites(track.trackId)
            val current = _uiState.value ?: return@launch
            if (current.isFavorite != inFavorites) {
                _uiState.value = current.copy(isFavorite = inFavorites)
            }
        }
    }

    fun loadPlaylists() {
        viewModelScope.launch {
            val items = playlistsInteractor.getPlaylists().first().map { it.toUI() }
            _playlists.value = items
        }
    }

    fun onPlaylistClicked(playlist: PlaylistUI) {
        viewModelScope.launch {
            val alreadyAdded =
                playlistsInteractor.isTrackInPlaylist(playlist.playlistId, track.trackId)
            if (alreadyAdded) {
                _addToPlaylistState.value = AddToPlaylistState.AlreadyAdded(playlist.name)
            } else {
                playlistsInteractor.addTrackToPlaylist(playlist.toDomain(), track.toTrackDomain())
                _addToPlaylistState.value = AddToPlaylistState.Added(playlist.name)
            }
        }
    }

    fun resetAddToPlaylistState() {
        _addToPlaylistState.value = null
    }

    fun onFavoriteClicked() {
        val current = _uiState.value ?: return
        val newIsFavorite = !current.isFavorite

        viewModelScope.launch {
            val domainTrack = track.toTrackDomain()
            if (newIsFavorite) {
                favoriteTracksInteractor.addTrackToFavorites(domainTrack)
            } else {
                favoriteTracksInteractor.removeTrackFromFavorites(domainTrack)
            }
        }

        _uiState.value = current.copy(isFavorite = newIsFavorite)
    }

    fun onPlayPauseClicked() {
        val current = _uiState.value ?: return

        when (current.playbackState) {
            PlaybackState.IDLE -> {
                shouldPlayWhenPrepared = true
            }
            PlaybackState.PREPARED, PlaybackState.COMPLETED -> {
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

    fun releasePlayer() {
        stopProgressUpdates()
        runCatching { mediaPlayer.stop() }
        runCatching { mediaPlayer.reset() }
        runCatching { mediaPlayer.release() }
    }

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) {
            _uiState.value =
                    _uiState.value?.copy(playbackState = PlaybackState.IDLE, progress = "00:00")
            return
        }

        mediaPlayer.setDataSource(previewUrl)

        mediaPlayer.setOnPreparedListener {
            val current = _uiState.value ?: return@setOnPreparedListener
            _uiState.value =
                    current.copy(playbackState = PlaybackState.PREPARED, progress = "00:00")

            if (shouldPlayWhenPrepared) {
                shouldPlayWhenPrepared = false
                startPlayback()
            }
        }

        mediaPlayer.setOnCompletionListener {
            val current = _uiState.value ?: return@setOnCompletionListener
            stopProgressUpdates()
            _uiState.value =
                    current.copy(playbackState = PlaybackState.COMPLETED, progress = "00:00")
        }

        mediaPlayer.prepareAsync()
    }

    private fun startPlayback() {
        mediaPlayer.start()
        val current = _uiState.value ?: return
        _uiState.value = current.copy(playbackState = PlaybackState.PLAYING)
        startProgressUpdates()
    }

    private fun resumePlayback() = startPlayback()

    private fun pausePlayback() {
        mediaPlayer.pause()
        val current = _uiState.value ?: return
        _uiState.value = current.copy(playbackState = PlaybackState.PAUSED)
        stopProgressUpdates()
    }

    private fun startProgressUpdates() {
        timerJob =
                viewModelScope.launch {
                    while (mediaPlayer.isPlaying) {
                        val current = _uiState.value ?: break
                        _uiState.value =
                                current.copy(
                                        progress = Utils.formatTime(mediaPlayer.currentPosition)
                                )
                        delay(UPDATE_DELAY_MS)
                    }
                }
    }

    private fun stopProgressUpdates() {
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        releasePlayer()
    }

    companion object {
        private const val UPDATE_DELAY_MS = 300L
    }
}
