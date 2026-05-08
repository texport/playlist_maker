package com.mybrain.playlistmaker.presentation.media.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.media.FavoriteTracksState
import com.mybrain.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.mybrain.playlistmaker.presentation.search.ui.SearchTrackRow

@Composable
fun FavoriteTracksTab(
    viewModel: FavoriteTracksViewModel,
    onTrackClick: (TrackUI) -> Unit,
) {
    val initial = viewModel.state.value ?: FavoriteTracksState.Empty
    val state by viewModel.state.observeAsState(initial)

    when (val s = state) {
        FavoriteTracksState.Empty -> FavoriteTracksEmptyPlaceholder()
        is FavoriteTracksState.Content -> FavoriteTracksList(
            tracks = s.tracks,
            onTrackClick = onTrackClick,
        )
    }
}

@Composable
private fun FavoriteTracksEmptyPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 106.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Image(
            painter = painterResource(R.drawable.im_placeholder_empty),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.no_favorite_tracks),
            fontFamily = FontFamily(Font(R.font.ys_medium)),
            fontWeight = FontWeight.Normal,
            fontSize = 19.sp,
            color = colorResource(R.color.main_text_color),
        )
    }
}

@Composable
private fun FavoriteTracksList(
    tracks: List<TrackUI>,
    onTrackClick: (TrackUI) -> Unit,
) {
    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 20.dp),
    ) {
        items(tracks, key = { it.trackId }) { track ->
            SearchTrackRow(
                track = track,
                onClick = { onTrackClick(track) },
            )
        }
    }
}
