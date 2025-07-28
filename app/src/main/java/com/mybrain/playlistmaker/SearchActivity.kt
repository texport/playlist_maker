package com.mybrain.playlistmaker

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class SearchActivity : AppCompatActivity() {
    // Переменные для элементов интерфейса
    private lateinit var searchInput: EditText
    private lateinit var clearSearchButton: ImageView
    private lateinit var toolbar: androidx.appcompat.widget.Toolbar
    private lateinit var rootLayout: View

    private var currentSearchText: String = ""

    companion object {
        private const val SEARCH_TEXT_KEY = "SEARCH_TEXT_KEY"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.search_activity)

        searchInput = findViewById(R.id.input_search)
        clearSearchButton = findViewById(R.id.button_clear_search)
        toolbar = findViewById(R.id.toolbar_search)
        rootLayout = findViewById(R.id.root_layout_search)

        val searchTextWatcher = object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                currentSearchText = s.toString()

                if (s.isNullOrEmpty()) {
                    clearSearchButton.visibility = View.GONE
                } else {
                    clearSearchButton.visibility = View.VISIBLE
                }
            }
        }

        toolbar.setNavigationOnClickListener {
            finish()
        }

        searchInput.addTextChangedListener(searchTextWatcher)

        clearSearchButton.setOnClickListener {
            searchInput.text.clear()
            hideKeyboard(searchInput)
        }

        rootLayout.setOnClickListener {
            currentFocus?.let { focusedView ->
                if (focusedView is EditText) {
                    hideKeyboard(focusedView)
                    focusedView.clearFocus()
                }
            }
        }

        clearSearchButton.visibility = if (searchInput.text.isNullOrEmpty()) View.GONE else View.VISIBLE
    }

    private fun hideKeyboard(view: View) {
        val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
        inputMethodManager?.hideSoftInputFromWindow(view.windowToken, 0)
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