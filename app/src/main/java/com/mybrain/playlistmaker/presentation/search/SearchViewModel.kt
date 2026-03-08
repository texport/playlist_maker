package com.mybrain.playlistmaker.presentation.search

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.mybrain.playlistmaker.domain.interactors.SearchHistoryInteractor
import com.mybrain.playlistmaker.domain.interactors.SearchInteractor
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.mappers.toTrackDomain
import com.mybrain.playlistmaker.presentation.mappers.toTrackSearchParams
import com.mybrain.playlistmaker.presentation.mappers.toUI
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class SearchViewModel(
    private val searchInteractor: SearchInteractor,
    private val historyInteractor: SearchHistoryInteractor
) : ViewModel() {

    private var searchJob: Job? = null
    private var clickJob: Job? = null
    private var isClickAllowed = true

    private var currentSearchText: String = ""
    private var lastQuery: String? = null
    private var hasFocus: Boolean = false

    private val _uiState = MutableLiveData<SearchUiState>(SearchUiState.IDLE)
    val uiState: LiveData<SearchUiState> = _uiState

    private val _searchResults = MutableLiveData<List<TrackUI>>(emptyList())
    val searchResults: LiveData<List<TrackUI>> = _searchResults

    private val _historyItems = MutableLiveData<List<TrackUI>>(emptyList())
    val historyItems: LiveData<List<TrackUI>> = _historyItems

    init {
        loadHistory()
        recalcUiState()
    }

    fun onSearchTextChanged(text: String) {
        currentSearchText = text
        searchJob?.cancel()

        if (text.isBlank()) {
            _searchResults.value = emptyList()
            recalcUiState()
        } else {
            searchJob = viewModelScope.launch {
                delay(SEARCH_DEBOUNCE_DELAY_MS)
                doSearch(text)
            }
        }
    }

    fun onSearchAction(text: String) {
        searchJob?.cancel()
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
        if (clickDebounce()) {
            historyInteractor.add(track.toTrackDomain())
            loadHistory()
            recalcUiState()
        }
    }

    private fun clickDebounce(): Boolean {
        val current = isClickAllowed
        if (isClickAllowed) {
            isClickAllowed = false
            clickJob = viewModelScope.launch {
                delay(CLICK_DEBOUNCE_DELAY_MS)
                isClickAllowed = true
            }
        }
        return current
    }

    private fun loadHistory() {
        _historyItems.value = historyInteractor.getAll().map { it.toUI() }
    }

    private fun doSearch(q: String) {
        if (q.isBlank()) return

        lastQuery = q
        _uiState.value = SearchUiState.LOADING

        viewModelScope.launch {
            searchInteractor.search(q.toTrackSearchParams())
                .collect { result ->
                    result.onSuccess { tracks ->
                        val uiTracks = tracks.map { it.toUI() }
                        _searchResults.value = uiTracks

                        if (tracks.isEmpty()) {
                            _uiState.value = SearchUiState.EMPTY
                        } else {
                            _uiState.value = SearchUiState.LIST
                        }
                    }.onFailure {
                        _searchResults.value = emptyList()
                        _uiState.value = SearchUiState.ERROR
                    }
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

    companion object {
        private const val SEARCH_DEBOUNCE_DELAY_MS = 2000L
        private const val CLICK_DEBOUNCE_DELAY_MS = 1000L
    }
}
