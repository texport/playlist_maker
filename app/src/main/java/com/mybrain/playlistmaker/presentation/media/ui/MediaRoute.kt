package com.mybrain.playlistmaker.presentation.media.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.mybrain.playlistmaker.presentation.media.PlaylistsViewModel
import kotlinx.coroutines.launch

@Composable
fun MediaRoute(
    favoriteViewModel: FavoriteTracksViewModel,
    playlistsViewModel: PlaylistsViewModel,
    onTrackClick: (TrackUI) -> Unit,
    onOpenPlaylist: (Long) -> Unit,
    onCreatePlaylist: () -> Unit,
    onEditPlaylist: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val pagerState = rememberPagerState(pageCount = { 2 })
    val tabs = listOf(
        stringResource(R.string.favorite_tracks),
        stringResource(R.string.playlists),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorResource(R.color.second_background)),
    ) {
        Text(
            text = stringResource(R.string.media_library),
            modifier = Modifier.padding(16.dp),
            fontFamily = FontFamily(Font(R.font.ys_bold)),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = colorResource(R.color.main_text_color),
        )

        TabRow(
            selectedTabIndex = pagerState.currentPage,
            containerColor = colorResource(R.color.second_background),
            contentColor = colorResource(R.color.main_text_color),
            indicator = { positions ->
                val current = positions[pagerState.currentPage]
                Box(
                    modifier = Modifier
                        .tabIndicatorOffset(current)
                        .padding(horizontal = 24.dp)
                        .fillMaxWidth()
                        .height(2.dp)
                        .background(colorResource(R.color.main_text_color))
                )
            },
            divider = {},
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = pagerState.currentPage == index,
                    onClick = {
                        if (pagerState.currentPage != index) {
                            scope.launch { pagerState.animateScrollToPage(index) }
                        }
                    },
                    selectedContentColor = colorResource(R.color.main_text_color),
                    unselectedContentColor = colorResource(R.color.main_text_color),
                    text = {
                        Text(
                            text = title,
                            fontFamily = FontFamily(Font(R.font.ys_medium)),
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                        )
                    },
                )
            }
        }

        HorizontalPager(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            state = pagerState,
            beyondViewportPageCount = 1,
        ) { page ->
            when (page) {
                0 -> FavoriteTracksTab(
                    viewModel = favoriteViewModel,
                    onTrackClick = onTrackClick,
                )

                else -> PlaylistsTab(
                    viewModel = playlistsViewModel,
                    onOpenPlaylist = onOpenPlaylist,
                    onCreatePlaylist = onCreatePlaylist,
                    onEditPlaylist = onEditPlaylist,
                )
            }
        }
    }
}
