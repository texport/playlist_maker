package com.mybrain.playlistmaker.presentation.media

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.domain.interactors.FavoriteTracksInteractor
import com.mybrain.playlistmaker.presentation.mappers.toUI
import kotlinx.coroutines.launch

class FavoriteTracksViewModel(private val favoriteTracksInteractor: FavoriteTracksInteractor) :
        ViewModel() {

    private val _state = MutableLiveData<FavoriteTracksState>()
    val state: LiveData<FavoriteTracksState> = _state

    init {
        viewModelScope.launch {
            favoriteTracksInteractor.getFavoriteTracks().collect { tracks ->
                if (tracks.isEmpty()) {
                    _state.postValue(FavoriteTracksState.Empty)
                } else {
                    _state.postValue(FavoriteTracksState.Content(tracks.map { it.toUI() }))
                }
            }
        }
    }
}
