package com.mybrain.playlistmaker.presentation.search

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.lifecycle.ViewModelProvider
import com.mybrain.playlistmaker.Creator
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.player.PlayerActivity

class SearchActivity : AppCompatActivity() {

    private lateinit var rootLayout: View
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

    private val openPlayerDebounceHandler = Handler(Looper.getMainLooper())
    private var canOpenPlayer = true

    private lateinit var viewModel: SearchViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.search_activity)

        initViews()
        initToolbar()
        initRecyclerViews()
        initViewModel()
        initListeners()
        observeViewModel()
    }

    override fun onDestroy() {
        super.onDestroy()
        openPlayerDebounceHandler.removeCallbacksAndMessages(null)
    }

    private fun initViews() {
        searchInput = findViewById(R.id.input_search)
        clearSearchButton = findViewById(R.id.button_clear_search)
        toolbar = findViewById(R.id.toolbar_search)
        rootLayout = findViewById(R.id.root_layout_search)

        recyclerView = findViewById(R.id.rvTracks)
        placeholderEmpty = findViewById(R.id.include_placeholder_empty)
        placeholderError = findViewById(R.id.include_placeholder_error)
        placeholderLoading = findViewById(R.id.include_placeholder_loading)
        retryButton = findViewById(R.id.button_retry)

        searchHistory = findViewById(R.id.search_history)
        historyRV = findViewById(R.id.rvHistory)
        clearHistoryButton = findViewById(R.id.btnClearHistory)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun initRecyclerViews() {
        adapter = SearchTrackAdapter(mutableListOf()) { track ->
            viewModel.onTrackClicked(track)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        historyAdapter = SearchTrackAdapter(mutableListOf()) { track ->
            viewModel.onTrackClicked(track)
        }
        historyRV.layoutManager = LinearLayoutManager(this)
        historyRV.setHasFixedSize(true)
        historyRV.adapter = historyAdapter
    }

    private fun initViewModel() {
        val searchInteractor = Creator.searchInteractor()
        val historyInteractor = Creator.searchHistoryInteractor(this)

        val factory = SearchViewModelFactory(searchInteractor, historyInteractor)
        viewModel = ViewModelProvider(this, factory)[SearchViewModel::class.java]
    }

    private fun initListeners() {
        searchInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val textNow = s?.toString().orEmpty()
                clearSearchButton.visibility =
                    if (textNow.trim().isEmpty()) View.GONE else View.VISIBLE

                viewModel.onSearchTextChanged(textNow)
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH || actionId == EditorInfo.IME_ACTION_DONE) {
                val q = searchInput.text.toString()
                if (q.isNotBlank()) {
                    hideKeyboard(searchInput)
                    viewModel.onSearchAction(q)
                }
                true
            } else false
        }

        searchInput.setOnFocusChangeListener { _, hasFocus ->
            viewModel.onSearchInputFocusChanged(hasFocus)
        }

        clearSearchButton.setOnClickListener {
            searchInput.text.clear()
            searchInput.requestFocus()
        }

        clearHistoryButton.setOnClickListener {
            viewModel.onClearHistoryClicked()
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
            viewModel.onRetryClicked()
        }

        clearSearchButton.visibility =
            if (searchInput.text.isNullOrEmpty()) View.GONE else View.VISIBLE

        searchInput.requestFocus()
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
            renderState(state)
            clearSearchButton.isEnabled = state != SearchUiState.LOADING
        }

        viewModel.searchResults.observe(this) { tracks ->
            adapter.updateData(tracks)
        }

        viewModel.historyItems.observe(this) { tracks ->
            historyAdapter.updateData(tracks)
        }

        viewModel.openPlayerEvent.observe(this) { track ->
            openPlayer(track)
        }
    }

    private fun renderState(state: SearchUiState) {
        when (state) {
            SearchUiState.IDLE -> {
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            SearchUiState.HISTORY -> {
                searchHistory.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            SearchUiState.LOADING -> {
                placeholderLoading.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            SearchUiState.LIST -> {
                recyclerView.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            SearchUiState.EMPTY -> {
                placeholderEmpty.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            SearchUiState.ERROR -> {
                placeholderError.visibility = View.VISIBLE
                searchHistory.visibility = View.GONE
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
            }
        }
    }

    private fun hideKeyboard(view: View) {
        (getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun openPlayer(track: TrackUI) {
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
}