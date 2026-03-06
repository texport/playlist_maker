package com.mybrain.playlistmaker.presentation.search

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import org.koin.androidx.viewmodel.ext.android.viewModel

class SearchFragment : Fragment() {

    private lateinit var rootLayout: View

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
    private val viewModel: SearchViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initRecyclerViews()
        initListeners()
        observeViewModel()

        clearSearchButton.visibility =
            if (searchInput.text.isNullOrEmpty()) View.GONE else View.VISIBLE

        searchInput.requestFocus()
    }

    override fun onDestroy() {
        super.onDestroy()
        openPlayerDebounceHandler.removeCallbacksAndMessages(null)
    }

    private fun initViews(view: View) {
        searchInput = view.findViewById(R.id.input_search)
        clearSearchButton = view.findViewById(R.id.button_clear_search)
        rootLayout = view.findViewById(R.id.root_layout_search)

        recyclerView = view.findViewById(R.id.rvTracks)
        placeholderEmpty = view.findViewById(R.id.include_placeholder_empty)
        placeholderError = view.findViewById(R.id.include_placeholder_error)
        placeholderLoading = view.findViewById(R.id.include_placeholder_loading)
        retryButton = view.findViewById(R.id.button_retry)

        searchHistory = view.findViewById(R.id.search_history)
        historyRV = view.findViewById(R.id.rvHistory)
        clearHistoryButton = view.findViewById(R.id.btnClearHistory)
    }

    private fun initRecyclerViews() {
        adapter = SearchTrackAdapter(mutableListOf()) { track ->
            viewModel.onTrackClicked(track)
            openPlayer(track)
        }
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        historyAdapter = SearchTrackAdapter(mutableListOf()) { track ->
            viewModel.onTrackClicked(track)
            openPlayer(track)
        }
        historyRV.layoutManager = LinearLayoutManager(requireContext())
        historyRV.setHasFixedSize(true)
        historyRV.adapter = historyAdapter
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
            hideKeyboard(requireActivity().currentFocus ?: it)
        }

        retryButton.setOnClickListener {
            viewModel.onRetryClicked()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            renderState(state)
            clearSearchButton.isEnabled = state != SearchUiState.LOADING
        }

        viewModel.searchResults.observe(viewLifecycleOwner) { tracks ->
            adapter.updateData(tracks)
        }

        viewModel.historyItems.observe(viewLifecycleOwner) { tracks ->
            historyAdapter.updateData(tracks)
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
        val imm = requireActivity().getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
        view.clearFocus()
    }

    private fun openPlayer(track: TrackUI) {
        if (!canOpenPlayer) return

        canOpenPlayer = false

        val action = SearchFragmentDirections.actionSearchFragmentToPlayerFragment(track)
        findNavController().navigate(action)

        openPlayerDebounceHandler.postDelayed(
            { canOpenPlayer = true },
            500L
        )
    }
}
