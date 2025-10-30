package com.mybrain.playlistmaker.search

import android.content.Intent
import android.os.Bundle
import android.os.Looper
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import com.mybrain.playlistmaker.repository.ItunesRepository
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mybrain.playlistmaker.App
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.models.Track
import com.mybrain.playlistmaker.player.PlayerActivity

class SearchActivity() : AppCompatActivity() {
    // Переменные для элементов интерфейса
    private lateinit var rootLayout: View
    private lateinit var app: App
    private lateinit var toolbar: Toolbar

    private lateinit var searchInput: EditText
    private lateinit var clearSearchButton: ImageView

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchTrackAdapter

    private lateinit var placeholderEmpty: View
    private lateinit var placeholderLoading: View

    private lateinit var placeholderError: View
    private lateinit var retryButton: Button

    private lateinit var searchHistory: View
    private lateinit var historyRV: RecyclerView
    private lateinit var clearHistoryButton: Button
    private lateinit var historyAdapter: SearchTrackAdapter

    private var currentSearchText: String = ""
    private val openPlayerDebounceHandler = Handler(Looper.getMainLooper())
    private var canOpenPlayer = true
    private val debounceDelayMs = 2000L
    private val handler = Handler(Looper.getMainLooper())
    private val searchRunnable = Runnable {
        val q = currentSearchText.trim()
        if (q.isNotEmpty()) {
            hideKeyboard(searchInput)
            doSearch(q)
        }
    }
    private var uiState: UiState = UiState.IDLE
    private var lastQuery: String? = null
    private var currentRequestId = 0
    private val repo by lazy { ItunesRepository() }

    private enum class UiState { IDLE, HISTORY, LOADING, LIST, EMPTY, ERROR }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        app = application as App
        setContentView(R.layout.search_activity)

        // базовые компоненты
        searchInput = findViewById(R.id.input_search)
        clearSearchButton = findViewById(R.id.button_clear_search)
        toolbar = findViewById(R.id.toolbar_search)
        rootLayout = findViewById(R.id.root_layout_search)

        // список
        recyclerView = findViewById(R.id.rvTracks)
        adapter = SearchTrackAdapter(mutableListOf()) { track ->
            app.searchHistoryStorageManager.add(track)
            openPlayer(track)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        // плейсхолдеры
        placeholderEmpty = findViewById(R.id.include_placeholder_empty)
        placeholderError = findViewById(R.id.include_placeholder_error)
        placeholderLoading = findViewById(R.id.include_placeholder_loading)
        retryButton = findViewById(R.id.button_retry)

        // история
        searchHistory = findViewById(R.id.search_history)
        historyRV = findViewById(R.id.rvHistory)
        historyAdapter = SearchTrackAdapter(mutableListOf()) { track ->
            app.searchHistoryStorageManager.add(track)
            openPlayer(track)
        }
        historyRV.layoutManager = LinearLayoutManager(this)
        historyRV.adapter = historyAdapter
        historyRV.setHasFixedSize(true)
        clearHistoryButton = findViewById(R.id.btnClearHistory)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s?.toString().orEmpty()
                val textNow = currentSearchText.trim()

                clearSearchButton.visibility = if (textNow.isEmpty()) View.GONE else View.VISIBLE

                updateHistoryVisibility()

                if (textNow.isEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    adapter.updateData(emptyList())
                    return
                }

                handler.removeCallbacks(searchRunnable)
                handler.postDelayed(searchRunnable, debounceDelayMs)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val q = searchInput.text.toString().trim()
                if (q.isNotEmpty()) {
                    handler.removeCallbacks(searchRunnable)
                    hideKeyboard(searchInput)
                    doSearch(q)
                }
                true
            } else false
        }

        clearSearchButton.setOnClickListener {
            searchInput.text.clear()
            searchInput.requestFocus()
            updateHistoryVisibility()
        }

        clearHistoryButton.setOnClickListener {
            app.searchHistoryStorageManager.clear()
            renderHistory()
        }

        rootLayout.setOnClickListener {
            currentFocus?.let { focusedView ->
                if (focusedView is EditText) {
                    hideKeyboard(focusedView)
                    focusedView.clearFocus()
                }
            }
        }

        retryButton.setOnClickListener {
            lastQuery?.let { doSearch(it) }
        }

        clearSearchButton.visibility = if (searchInput.text.isNullOrEmpty()) View.GONE else View.VISIBLE

        searchInput.setOnFocusChangeListener { _, hasFocus ->
            if (uiState != UiState.LOADING) updateHistoryVisibility()
        }

        searchInput.requestFocus()
        if (searchInput.text.isNullOrEmpty()) renderHistory()
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(searchRunnable)
        openPlayerDebounceHandler.removeCallbacksAndMessages(null)
    }

    private fun renderHistory() {
        val items = app.searchHistoryStorageManager.get()

        if (items.isEmpty()) {
            setUiState(UiState.IDLE)
        } else {
            historyAdapter.updateData(items)
            setUiState(UiState.HISTORY)
        }
    }

    private fun updateHistoryVisibility() {
        if (uiState == UiState.LOADING) return

        val hasFocus = searchInput.hasFocus()
        val isEmpty = searchInput.text.isNullOrEmpty()
        val items = app.searchHistoryStorageManager.get()

        if (hasFocus && isEmpty && items.isNotEmpty()) {
            historyAdapter.updateData(items)
            setUiState(UiState.HISTORY)
        } else if (adapter.itemCount > 0) {
            setUiState(UiState.LIST)
        } else {
            when (uiState) {
                UiState.EMPTY -> setUiState(UiState.EMPTY)
                UiState.ERROR -> setUiState(UiState.ERROR)
                else          -> setUiState(UiState.IDLE)
            }
        }
    }

    private fun hideKeyboard(view: View) {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun doSearch(q: String) {
        val requestId = ++currentRequestId
        lastQuery = q
        clearSearchButton.isEnabled = false
        setUiState(UiState.LOADING)

        repo.search(q) { result ->
            runOnUiThread {
                if (requestId != currentRequestId) return@runOnUiThread

                clearSearchButton.isEnabled = true
                result.onSuccess { tracks ->
                    if (tracks.isEmpty()) {
                        adapter.updateData(emptyList())
                        setUiState(UiState.EMPTY)
                    } else {
                        adapter.updateData(tracks)
                        setUiState(UiState.LIST)
                    }
                }.onFailure {
                    adapter.updateData(emptyList())
                    setUiState(UiState.ERROR)
                }
            }
        }
    }

    private fun setUiState(state: UiState) {
        uiState = state

        when (state) {
            UiState.IDLE -> {
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.HISTORY -> {
                searchHistory.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.LOADING -> {
                placeholderLoading.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.LIST -> {
                recyclerView.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.EMPTY -> {
                placeholderEmpty.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.ERROR -> {
                placeholderError.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
            }
        }
    }

    private fun openPlayer(track: Track) {
        if (!canOpenPlayer) return
        canOpenPlayer = false
        val intent = Intent(this, PlayerActivity::class.java)
        intent.putExtra(PlayerActivity.EXTRA_TRACK_ID, track)
        startActivity(intent)

        openPlayerDebounceHandler.postDelayed(
            { canOpenPlayer = true },
            500L
        )
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, currentSearchText)
        outState.putString("LAST_QUERY_KEY", lastQuery)
        outState.putString("UI_STATE_KEY", uiState.name)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        currentSearchText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        searchInput.setText(currentSearchText)
        lastQuery = savedInstanceState.getString("LAST_QUERY_KEY")
        uiState = savedInstanceState.getString("UI_STATE_KEY")?.let { UiState.valueOf(it) } ?: UiState.IDLE

        when (uiState) {
            UiState.HISTORY -> renderHistory()
            UiState.LIST    -> setUiState(UiState.LIST)
            UiState.EMPTY   -> setUiState(UiState.EMPTY)
            UiState.ERROR   -> setUiState(UiState.ERROR)
            else            -> setUiState(UiState.IDLE)
        }
    }

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }
}