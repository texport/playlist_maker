package com.mybrain.playlistmaker.presentation.search

import android.os.Handler
import android.os.Looper
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.mybrain.playlistmaker.domain.interactors.SearchHistoryInteractor
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.mappers.toTrackDomain
import com.mybrain.playlistmaker.presentation.mappers.toTrackSearchParams
import com.mybrain.playlistmaker.presentation.mappers.toUI

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: SearchHistoryInteractor
) : ViewModel() {

    private val handler = Handler(Looper.getMainLooper())
    private val debounceDelayMs = 2000L

    private var currentSearchText: String = ""
    private var currentRequestId = 0
    private var lastQuery: String? = null
    private var hasFocus: Boolean = false

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.IDLE)
    val uiState: LiveData<SearchUiState> = _uiState

    private val _searchResults = MutableLiveData<List<TrackUI>>(emptyList())
    val searchResults: LiveData<List<TrackUI>> = _searchResults

    private val _historyItems = MutableLiveData<List<TrackUI>>(emptyList())
    val historyItems: LiveData<List<TrackUI>> = _historyItems

    private val _openPlayerEvent = MutableLiveData<TrackUI>()
    val openPlayerEvent: LiveData<TrackUI> = _openPlayerEvent

    private val searchRunnable = Runnable {
        val q = currentSearchText.trim()
        if (q.isNotEmpty()) {
            doSearch(q)
        } else {
            _searchResults.value = emptyList()
            recalcUiState()
        }
    }

    init {
        loadHistory()
        recalcUiState()
    }

    fun onSearchTextChanged(text: String) {
        currentSearchText = text
        handler.removeCallbacks(searchRunnable)

        if (text.isBlank()) {
            _searchResults.value = emptyList()
            recalcUiState()
        } else {
            handler.postDelayed(searchRunnable, debounceDelayMs)
        }
    }

    fun onSearchAction(text: String) {
        handler.removeCallbacks(searchRunnable)
        val q = text.trim()
        if (q.isNotEmpty()) {
            doSearch(q)
        }
    }

    fun onSearchInputFocusChanged(hasFocus: Boolean) {
        this.hasFocus = hasFocus
        recalcUiState()
    }

    fun onRetryClicked() {
        lastQuery?.let { doSearch(it) }
    }

    fun onClearHistoryClicked() {
        historyInteractor.clear()
        loadHistory()
        recalcUiState()
    }

    fun onTrackClicked(track: TrackUI) {
        historyInteractor.add(track.toTrackDomain())
        _openPlayerEvent.value = track
        loadHistory()
        recalcUiState()
    }

    private fun loadHistory() {
        _historyItems.value = historyInteractor.getAll().map { it.toUI() }
    }

    private fun doSearch(q: String) {
        val requestId = ++currentRequestId
        lastQuery = q

        _uiState.postValue(SearchUiState.LOADING)

        searchInteractor.search(q.toTrackSearchParams()) { result ->
            if (requestId != currentRequestId) return@search

            result.onSuccess { tracks ->
                val uiTracks = tracks.map { it.toUI() }
                _searchResults.postValue(uiTracks)

                if (tracks.isEmpty()) {
                    _uiState.postValue(SearchUiState.EMPTY)
                } else {
                    _uiState.postValue(SearchUiState.LIST)
                }
            }.onFailure {
                _searchResults.postValue(emptyList())
                _uiState.postValue(SearchUiState.ERROR)
            }
        }
    }

    private fun recalcUiState() {
        val textEmpty = currentSearchText.isBlank()
        val hasHistoryItems = !_historyItems.value.isNullOrEmpty()
        val hasResults = !_searchResults.value.isNullOrEmpty()

        val newState = when {
            _uiState.value == SearchUiState.LOADING -> SearchUiState.LOADING
            hasFocus && textEmpty && hasHistoryItems -> SearchUiState.HISTORY
            hasResults -> SearchUiState.LIST
            _uiState.value == SearchUiState.EMPTY -> SearchUiState.EMPTY
            _uiState.value == SearchUiState.ERROR -> SearchUiState.ERROR
            else -> SearchUiState.IDLE
        }

        _uiState.value = newState
    }

    override fun onCleared() {
        super.onCleared()
        handler.removeCallbacksAndMessages(null)
    }
}