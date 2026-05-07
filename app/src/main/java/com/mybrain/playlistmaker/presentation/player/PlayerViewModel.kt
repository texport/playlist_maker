package com.mybrain.playlistmaker.presentation.player

import android.content.Context
import android.os.Build
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.ProcessLifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.mybrain.playlistmaker.domain.interactors.PlaylistsInteractor
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.mappers.toDomain
import com.mybrain.playlistmaker.presentation.mappers.toTrackDomain
import com.mybrain.playlistmaker.presentation.mappers.toUI
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PlayerViewModel(
        private val appContext: Context,
        private val track: TrackUI,
        private val favoriteTracksInteractor: FavoriteTracksInteractor,
        private val playlistsInteractor: PlaylistsInteractor
) : ViewModel() {

    private val progressPlaceholder: String =
            appContext.getString(R.string.playback_progress_placeholder)

    private var playerController: PlayerServiceController? = null
    private var playbackStateJob: Job? = null
    private var isNotificationsPermissionGranted = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
    private val processObserver = LifecycleEventObserver { _, event ->
        when (event) {
            Lifecycle.Event.ON_STOP -> maybeShowNotification()
            Lifecycle.Event.ON_START -> playerController?.hidePlaybackNotification()
            else -> Unit
        }
    }

    private val _uiState =
            MutableLiveData(
                    PlayerUiState(
                            track = track,
                            progress = progressPlaceholder,
                            playbackState = PlaybackState.IDLE,
                            isFavorite = track.isFavorite
                    )
            )
    val uiState: LiveData<PlayerUiState> = _uiState

    private val _playlists = MutableLiveData<List<PlaylistUI>>()
    val playlists: LiveData<List<PlaylistUI>> = _playlists

    private val _addToPlaylistState = MutableLiveData<AddToPlaylistState?>()
    val addToPlaylistState: LiveData<AddToPlaylistState?> = _addToPlaylistState

    init {
        ProcessLifecycleOwner.get().lifecycle.addObserver(processObserver)
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
            PlaybackState.IDLE, PlaybackState.PREPARED, PlaybackState.COMPLETED -> {
                playerController?.startPlayback()
            }
            PlaybackState.PLAYING -> {
                playerController?.pausePlayback()
            }
            PlaybackState.PAUSED -> {
                playerController?.startPlayback()
            }
        }
    }

    fun attachPlayerController(controller: PlayerServiceController) {
        playerController = controller
        playbackStateJob?.cancel()
        playbackStateJob =
            viewModelScope.launch {
                controller.playbackState().collect { serviceState ->
                    val current = _uiState.value ?: return@collect
                    _uiState.value = current.copy(
                        playbackState = serviceState.playbackState,
                        progress = serviceState.progress
                    )

                    if (serviceState.playbackState != PlaybackState.PLAYING) {
                        controller.hidePlaybackNotification()
                    }
                }
            }
    }

    fun detachPlayerController() {
        playbackStateJob?.cancel()
        playbackStateJob = null
        playerController = null
    }

    fun onNotificationsPermissionChanged(granted: Boolean) {
        isNotificationsPermissionGranted = granted
        if (!granted) {
            playerController?.hidePlaybackNotification()
        } else {
            maybeShowNotification()
        }
    }

    private fun maybeShowNotification() {
        if (!isNotificationsPermissionGranted) return
        val current = _uiState.value ?: return
        if (current.playbackState == PlaybackState.PLAYING) {
            playerController?.showPlaybackNotification()
        }
    }

    override fun onCleared() {
        ProcessLifecycleOwner.get().lifecycle.removeObserver(processObserver)
        playerController?.hidePlaybackNotification()
        detachPlayerController()
        super.onCleared()
    }
}
