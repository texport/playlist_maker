@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.mybrain.playlistmaker.presentation.media.ui

import android.content.Context
import android.content.DialogInterface
import androidx.compose.foundation.Image
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import coil.compose.AsyncImage
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.media.PlaylistsState
import com.mybrain.playlistmaker.presentation.media.PlaylistsViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File

private const val LONG_PRESS_MENU_DELAY_MS = 120L

@Composable
fun PlaylistsTab(
    viewModel: PlaylistsViewModel,
    onOpenPlaylist: (Long) -> Unit,
    onCreatePlaylist: () -> Unit,
    onEditPlaylist: (Long) -> Unit,
) {
    val initial = viewModel.state.value ?: PlaylistsState.Empty
    val state by viewModel.state.observeAsState(initial)

    Column(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 24.dp),
            contentAlignment = Alignment.Center,
        ) {
            Button(
                onClick = onCreatePlaylist,
                shape = RoundedCornerShape(54.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.placeholder_note_color),
                    contentColor = colorResource(R.color.second_background),
                ),
            ) {
                Text(
                    text = stringResource(R.string.new_playlist),
                    fontFamily = FontFamily(Font(R.font.ys_medium)),
                    fontWeight = FontWeight.Normal,
                    fontSize = 14.sp,
                )
            }
        }

        when (val s = state) {
            PlaylistsState.Empty -> PlaylistsEmptyPlaceholder()
            is PlaylistsState.Content -> PlaylistsGrid(
                playlists = s.playlists,
                viewModel = viewModel,
                onOpenPlaylist = onOpenPlaylist,
                onEditPlaylist = onEditPlaylist,
            )
        }
    }
}

@Composable
private fun PlaylistsEmptyPlaceholder() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 46.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        Image(
            painter = painterResource(R.drawable.im_placeholder_empty),
            contentDescription = null,
        )
        Text(
            modifier = Modifier.padding(top = 16.dp),
            text = stringResource(R.string.no_playlists),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.ys_medium)),
            fontWeight = FontWeight.Normal,
            fontSize = 19.sp,
            color = colorResource(R.color.main_text_color),
        )
    }
}

@Composable
private fun PlaylistsGrid(
    playlists: List<PlaylistUI>,
    viewModel: PlaylistsViewModel,
    onOpenPlaylist: (Long) -> Unit,
    onEditPlaylist: (Long) -> Unit,
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 16.dp),
        contentPadding = PaddingValues(start = 16.dp, end = 16.dp, bottom = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(playlists, key = { it.playlistId }) { playlist ->
            PlaylistGridCell(
                playlist = playlist,
                viewModel = viewModel,
                onOpenPlaylist = { onOpenPlaylist(playlist.playlistId) },
                onEditPlaylist = onEditPlaylist,
            )
        }
    }
}

@Composable
private fun PlaylistGridCell(
    playlist: PlaylistUI,
    viewModel: PlaylistsViewModel,
    onOpenPlaylist: () -> Unit,
    onEditPlaylist: (Long) -> Unit,
) {
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    var menuExpanded by remember(playlist.playlistId) { mutableStateOf(false) }
    val cornerRadius = dimensionResource(R.dimen.corner_8)

    Column(modifier = Modifier.fillMaxWidth()) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f),
        ) {
            val coverFile = playlist.coverPath?.let { File(it) }
            AsyncImage(
                model = coverFile,
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(cornerRadius))
                    .combinedClickable(
                        onClick = onOpenPlaylist,
                        onLongClick = {
                            scope.launch {
                                delay(LONG_PRESS_MENU_DELAY_MS)
                                menuExpanded = true
                            }
                        },
                    ),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder_track),
                error = painterResource(R.drawable.placeholder_track),
            )
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.share_action)) },
                    onClick = {
                        menuExpanded = false
                        viewModel.requestShare(playlist)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.edit_playlist)) },
                    onClick = {
                        menuExpanded = false
                        onEditPlaylist(playlist.playlistId)
                    },
                )
                DropdownMenuItem(
                    text = { Text(stringResource(R.string.delete_playlist)) },
                    onClick = {
                        menuExpanded = false
                        showDeletePlaylistDialog(context, playlist, viewModel)
                    },
                )
            }
        }
        Text(
            text = playlist.name,
            modifier = Modifier.fillMaxWidth(),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            fontFamily = FontFamily(Font(R.font.ys_regular)),
            fontSize = 12.sp,
            color = colorResource(R.color.main_text_color),
        )
        Text(
            text = context.resources.getQuantityString(
                R.plurals.tracks_count,
                playlist.tracksCount,
                playlist.tracksCount,
            ),
            modifier = Modifier.fillMaxWidth(),
            fontFamily = FontFamily(Font(R.font.ys_regular)),
            fontSize = 12.sp,
            color = colorResource(R.color.main_text_color),
        )
    }
}

private fun showDeletePlaylistDialog(
    context: Context,
    playlist: PlaylistUI,
    viewModel: PlaylistsViewModel,
) {
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.delete_playlist_title)
        .setMessage(R.string.delete_playlist_message)
        .setNegativeButton(R.string.delete_playlist_cancel, null)
        .setPositiveButton(R.string.delete_playlist_confirm) { _, _ ->
            viewModel.deletePlaylist(playlist.playlistId, playlist.name)
        }
        .create()
    dialog.setOnShowListener {
        val color = ContextCompat.getColor(context, R.color.blue)
        dialog.getButton(DialogInterface.BUTTON_POSITIVE)?.setTextColor(color)
        dialog.getButton(DialogInterface.BUTTON_NEGATIVE)?.setTextColor(color)
    }
    dialog.show()
}
