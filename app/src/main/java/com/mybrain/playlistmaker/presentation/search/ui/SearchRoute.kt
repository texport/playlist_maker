package com.mybrain.playlistmaker.presentation.search.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.search.SearchUiState
import com.mybrain.playlistmaker.presentation.search.SearchViewModel

@Composable
fun SearchRoute(
    viewModel: SearchViewModel,
    onOpenPlayer: (TrackUI) -> Unit,
) {
    val uiState by viewModel.uiState.observeAsState(SearchUiState.IDLE)
    val results by viewModel.searchResults.observeAsState(emptyList())
    val history by viewModel.historyItems.observeAsState(emptyList())

    var query by rememberSaveable { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val hideKeyboard: () -> Unit = {
        focusManager.clearFocus()
        keyboardController?.hide()
    }

    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.second_background))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = hideKeyboard,
            ),
    ) {
        Text(
            text = stringResource(R.string.search),
            modifier = Modifier.padding(16.dp),
            fontFamily = FontFamily(Font(R.font.ys_bold)),
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = colorResource(R.color.main_text_color),
        )

        SearchInputBar(
            query = query,
            onQueryChange = { newValue ->
                query = newValue
                viewModel.onSearchTextChanged(newValue)
            },
            uiState = uiState,
            focusRequester = focusRequester,
            onFocusChanged = { hasFocus -> viewModel.onSearchInputFocusChanged(hasFocus) },
            onImeSearch = { q ->
                if (q.isNotBlank()) {
                    hideKeyboard()
                    viewModel.onSearchAction(q)
                }
            },
            onClear = {
                query = ""
                viewModel.onSearchTextChanged("")
                focusRequester.requestFocus()
            },
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 16.dp),
        )

        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            when (uiState) {
                SearchUiState.IDLE -> Unit
                SearchUiState.HISTORY -> SearchHistoryContent(
                    history = history,
                    onTrackClick = { track ->
                        viewModel.onTrackClicked(track)
                        onOpenPlayer(track)
                    },
                    onClearHistory = { viewModel.onClearHistoryClicked() },
                )
                SearchUiState.LOADING -> SearchLoadingPlaceholder()
                SearchUiState.LIST -> SearchResultsList(
                    tracks = results,
                    onTrackClick = { track ->
                        viewModel.onTrackClicked(track)
                        onOpenPlayer(track)
                    },
                )
                SearchUiState.EMPTY -> SearchEmptyPlaceholder()
                SearchUiState.ERROR -> SearchErrorPlaceholder(
                    onRetry = { viewModel.onRetryClicked() },
                )
            }
        }
    }
}

@Composable
private fun SearchInputBar(
    query: String,
    onQueryChange: (String) -> Unit,
    uiState: SearchUiState,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onImeSearch: (String) -> Unit,
    onClear: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(36.dp)
            .background(
                colorResource(R.color.search_background),
                RoundedCornerShape(8.dp),
            ),
    ) {
        RowSearchField(
            query = query,
            onQueryChange = onQueryChange,
            focusRequester = focusRequester,
            onFocusChanged = onFocusChanged,
            onImeSearch = onImeSearch,
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = 12.dp,
                    top = 8.5.dp,
                    end = 40.dp,
                    bottom = 8.5.dp,
                ),
        )
        if (query.isNotBlank()) {
            Box(
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(16.dp)
                    .clickable(
                        enabled = uiState != SearchUiState.LOADING,
                        onClick = onClear,
                    ),
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_clear_16),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(16.dp),
                )
            }
        }
    }
}

@Composable
private fun RowSearchField(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester,
    onFocusChanged: (Boolean) -> Unit,
    onImeSearch: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val regular = FontFamily(Font(R.font.ys_regular))
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_search_themed_16),
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier.size(16.dp),
        )
        Spacer(modifier = Modifier.width(8.dp))
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            modifier = Modifier
                .weight(1f)
                .focusRequester(focusRequester)
                .onFocusChanged { onFocusChanged(it.isFocused) },
            textStyle = TextStyle(
                color = colorResource(R.color.search_text_edit_color),
                fontSize = 16.sp,
                fontFamily = regular,
            ),
            singleLine = true,
            // XML uses @drawable/custom_cursor with @color/blue.
            cursorBrush = SolidColor(colorResource(R.color.blue)),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(
                onDone = {
                    val q = query.trim()
                    if (q.isNotBlank()) {
                        onImeSearch(q)
                    }
                },
            ),
            decorationBox = { inner ->
                Box {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.search),
                            style = TextStyle(
                                color = colorResource(R.color.edit_text_hint_color),
                                fontSize = 16.sp,
                                fontFamily = regular,
                            ),
                        )
                    }
                    inner()
                }
            },
        )
    }
}

@Composable
private fun SearchResultsList(
    tracks: List<TrackUI>,
    onTrackClick: (TrackUI) -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(top = 16.dp),
    ) {
        items(tracks, key = { it.trackId }) { track ->
            SearchTrackRow(track = track, onClick = { onTrackClick(track) })
        }
    }
}

@Composable
private fun SearchHistoryContent(
    history: List<TrackUI>,
    onTrackClick: (TrackUI) -> Unit,
    onClearHistory: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
    ) {
        item {
            Text(
                text = stringResource(R.string.you_searched),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 42.dp),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(Font(R.font.ys_medium)),
                fontSize = 19.sp,
                color = colorResource(R.color.main_text_color),
            )
        }
        item {
            Spacer(modifier = Modifier.height(20.dp))
        }
        items(history, key = { it.trackId }) { track ->
            SearchTrackRow(track = track, onClick = { onTrackClick(track) })
        }
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center,
            ) {
                Button(
                    onClick = onClearHistory,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colorResource(R.color.placeholder_note_color),
                        contentColor = colorResource(R.color.second_background),
                    ),
                    shape = RoundedCornerShape(54.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
                ) {
                    Text(
                        text = stringResource(R.string.clear_history),
                        fontFamily = FontFamily(Font(R.font.ys_medium)),
                        fontSize = 14.sp,
                    )
                }
            }
        }
    }
}

@Composable
private fun SearchEmptyPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.second_background)),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.im_placeholder_empty),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 102.dp)
                .size(120.dp),
        )
        Text(
            text = stringResource(R.string.search_no_results),
            modifier = Modifier.padding(top = 16.dp),
            fontFamily = FontFamily(Font(R.font.ys_medium)),
            fontSize = 19.sp,
            color = colorResource(R.color.main_text_color),
        )
    }
}

@Composable
private fun SearchErrorPlaceholder(
    onRetry: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.second_background))
            .padding(horizontal = 16.dp)
            .padding(top = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Image(
            painter = painterResource(R.drawable.im_placeholder_error),
            contentDescription = null,
            modifier = Modifier
                .padding(top = 102.dp)
                .size(120.dp),
        )
        Text(
            text = stringResource(R.string.search_server_error_title),
            modifier = Modifier.padding(top = 16.dp),
            fontFamily = FontFamily(Font(R.font.ys_medium)),
            fontSize = 19.sp,
            color = colorResource(R.color.main_text_color),
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.search_server_error_subtitle),
            modifier = Modifier.padding(top = 20.dp),
            fontFamily = FontFamily(Font(R.font.ys_medium)),
            fontSize = 19.sp,
            color = colorResource(R.color.main_text_color),
            textAlign = TextAlign.Center,
        )
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = colorResource(R.color.placeholder_note_color),
                contentColor = colorResource(R.color.second_background),
            ),
            shape = RoundedCornerShape(54.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Text(
                text = stringResource(R.string.retry),
                fontFamily = FontFamily(Font(R.font.ys_medium)),
                fontSize = 14.sp,
            )
        }
    }
}

@Composable
private fun SearchLoadingPlaceholder() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.second_background)),
        contentAlignment = Alignment.TopCenter,
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .padding(top = 140.dp)
                .size(44.dp),
            color = colorResource(R.color.blue),
            trackColor = Color.Transparent,
            strokeWidth = 4.dp,
        )
    }
}
