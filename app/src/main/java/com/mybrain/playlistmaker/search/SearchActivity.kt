package com.mybrain.playlistmaker.search

import android.os.Bundle
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
import com.mybrain.playlistmaker.R

class SearchActivity : AppCompatActivity() {
    // Переменные для элементов интерфейса
    private lateinit var searchInput: EditText
    private lateinit var clearSearchButton: ImageView
    private lateinit var toolbar: Toolbar
    private lateinit var rootLayout: View
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: SearchTrackAdapter
    private lateinit var placeholderEmpty: View
    private lateinit var placeholderError: View
    private lateinit var placeholderLoading: View
    private lateinit var retryButton: Button
    private var currentSearchText: String = ""
    private var lastQuery: String? = null
    private val repo by lazy { ItunesRepository() }

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }

    private enum class UiState { IDLE, LOADING, LIST, EMPTY, ERROR }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.search_activity)

        // базовые компоненты
        searchInput = findViewById(R.id.input_search)
        clearSearchButton = findViewById(R.id.button_clear_search)
        toolbar = findViewById(R.id.toolbar_search)
        rootLayout = findViewById(R.id.root_layout_search)

        // список
        recyclerView = findViewById(R.id.rvTracks)
        adapter = SearchTrackAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // плейсхолдеры
        placeholderEmpty = findViewById(R.id.include_placeholder_empty)
        placeholderError = findViewById(R.id.include_placeholder_error)
        placeholderLoading = findViewById(R.id.include_placeholder_loading)
        retryButton = findViewById(R.id.button_retry)

        val searchTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s?.toString().orEmpty()
                clearSearchButton.visibility = if (currentSearchText.isEmpty()) View.GONE else View.VISIBLE

                if (currentSearchText.isEmpty()) {
                    setUiState(UiState.IDLE)
                    adapter.updateData(emptyList())
                }
            }
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchInput.addTextChangedListener(searchTextWatcher)

        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val q = searchInput.text.toString().trim()
                if (q.isNotEmpty()) doSearch(q)
                hideKeyboard(searchInput)
                true
            } else false
        }

        clearSearchButton.setOnClickListener {
            searchInput.text.clear()
            hideKeyboard(searchInput)
            adapter.updateData(emptyList())
            setUiState(UiState.IDLE)
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
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun doSearch(q: String) {
        lastQuery = q
        clearSearchButton.isEnabled = false
        setUiState(UiState.LOADING)

        repo.search(q) { result ->
            runOnUiThread {
                clearSearchButton.isEnabled = true

                result.onSuccess { tracks ->
                    if (tracks.isEmpty()) {
                        adapter.updateData(emptyList())
                        setUiState(UiState.EMPTY)
                    } else {
                        adapter.updateData(tracks)
                        setUiState(UiState.LIST)
                    }
                }.onFailure { e ->
                    adapter.updateData(emptyList())
                    setUiState(UiState.ERROR)
                }
            }
        }
    }

    private fun setUiState(state: UiState) {
        when (state) {
            UiState.IDLE -> {
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.LOADING -> {
                placeholderLoading.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.LIST -> {
                recyclerView.visibility = View.VISIBLE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.GONE
            }
            UiState.EMPTY -> {
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.VISIBLE
                placeholderError.visibility = View.GONE
            }
            UiState.ERROR -> {
                recyclerView.visibility = View.GONE
                placeholderLoading.visibility = View.GONE
                placeholderEmpty.visibility = View.GONE
                placeholderError.visibility = View.VISIBLE
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString(SEARCH_TEXT_KEY, currentSearchText)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        val restoredText = savedInstanceState.getString(SEARCH_TEXT_KEY, "")
        if (currentSearchText != restoredText) {
            currentSearchText = restoredText
            searchInput.setText(currentSearchText)
        }
    }
}