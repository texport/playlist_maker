package com.mybrain.playlistmaker.presentation.player.ui

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.os.Build
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.content.ContextCompat
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.navigation.AppRoutes
import com.mybrain.playlistmaker.presentation.player.AddToPlaylistState
import com.mybrain.playlistmaker.presentation.player.PlaybackState
import com.mybrain.playlistmaker.presentation.player.AudioPlayerService
import com.mybrain.playlistmaker.presentation.player.PlaybackButtonView
import com.mybrain.playlistmaker.presentation.player.PlayerUiState
import com.mybrain.playlistmaker.presentation.player.PlayerViewModel
import java.io.File
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun PlayerRoute(
    track: TrackUI,
    navController: NavController,
    onNavigateUp: () -> Unit,
    snackbarHost: (
        message: String,
        onShow: () -> Unit,
        onDismiss: () -> Unit,
    ) -> Unit,
) {
    val context = LocalContext.current
    val viewModel: PlayerViewModel = koinViewModel(parameters = { parametersOf(track) })

    var playlistSheetVisible by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val progressPlaceholder = stringResource(R.string.playback_progress_placeholder)
    val uiState by viewModel.uiState.observeAsState(
        PlayerUiState(
            track = track,
            progress = progressPlaceholder,
            playbackState = PlaybackState.IDLE,
            isFavorite = track.isFavorite,
        ),
    )
    val playlists by viewModel.playlists.observeAsState(emptyList())
    val addToPlaylistState by viewModel.addToPlaylistState.observeAsState()

    val notificationPermissionLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
        ) { granted ->
            viewModel.onNotificationsPermissionChanged(granted)
        }

    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted =
                ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS,
                ) == PackageManager.PERMISSION_GRANTED
            viewModel.onNotificationsPermissionChanged(granted)
            if (!granted) {
                notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        } else {
            viewModel.onNotificationsPermissionChanged(true)
        }
    }

    DisposableEffect(track) {
        val serviceConnection =
            object : ServiceConnection {
                override fun onServiceConnected(
                    name: ComponentName?,
                    service: android.os.IBinder?,
                ) {
                    val binder = service as? AudioPlayerService.LocalBinder ?: return
                    viewModel.attachPlayerController(binder.getService())
                }

                override fun onServiceDisconnected(name: ComponentName?) {
                    viewModel.detachPlayerController()
                }
            }
        val intent =
            Intent(context, AudioPlayerService::class.java).apply {
                putExtra(AudioPlayerService.EXTRA_PREVIEW_URL, track.previewUrl)
                putExtra(AudioPlayerService.EXTRA_ARTIST_NAME, track.artistName)
                putExtra(AudioPlayerService.EXTRA_TRACK_NAME, track.trackName)
            }
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        onDispose {
            context.unbindService(serviceConnection)
            viewModel.detachPlayerController()
        }
    }

    LaunchedEffect(addToPlaylistState) {
        val state = addToPlaylistState ?: return@LaunchedEffect
        when (state) {
            is AddToPlaylistState.Added -> {
                playlistSheetVisible = false
                snackbarHost(
                    context.getString(R.string.playlist_added, state.playlistName),
                    {},
                    {},
                )
            }
            is AddToPlaylistState.AlreadyAdded -> {
                snackbarHost(
                    context.getString(R.string.playlist_already_added, state.playlistName),
                    {},
                    {},
                )
            }
        }
        viewModel.resetAddToPlaylistState()
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colorResource(R.color.second_background)),
    ) {
        Column(Modifier.fillMaxSize()) {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onNavigateUp) {
                        Icon(
                            painter = painterResource(R.drawable.ic_arrow_back_24),
                            contentDescription = null,
                            tint = colorResource(R.color.main_text_color),
                        )
                    }
                },
                colors =
                    TopAppBarDefaults.topAppBarColors(
                        containerColor = colorResource(R.color.second_background),
                    ),
                windowInsets = WindowInsets(0, 0, 0, 0),
            )

            Column(
                modifier =
                    Modifier
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
            ) {
                val coverUrl = track.artworkUrl100.replace("100x100bb", "512x512bb")
                AsyncImage(
                    model =
                        ImageRequest.Builder(context)
                            .data(coverUrl)
                            .crossfade(true)
                            .build(),
                    contentDescription = null,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 26.dp, end = 24.dp)
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(dimensionResource(R.dimen.corner_8))),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder_track),
                    error = painterResource(R.drawable.placeholder_track),
                )

                Text(
                    text = track.trackName,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 24.dp, end = 24.dp),
                    fontFamily = FontFamily(Font(R.font.ys_medium)),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Normal,
                    color = colorResource(R.color.main_text_color),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = track.artistName,
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .padding(start = 24.dp, top = 12.dp, end = 24.dp),
                    fontFamily = FontFamily(Font(R.font.ys_medium)),
                    fontSize = 14.sp,
                    color = colorResource(R.color.main_text_color),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                PlayerActionsBlock(
                    progress = uiState.progress,
                    isPlaying = uiState.isPlaying,
                    isPlayEnabled = uiState.isPlayButtonEnabled,
                    isFavorite = uiState.isFavorite,
                    onPlayPause = { viewModel.onPlayPauseClicked() },
                    onFavorite = { viewModel.onFavoriteClicked() },
                    onAddToPlaylist = {
                        viewModel.loadPlaylists()
                        playlistSheetVisible = true
                    },
                )

                Spacer(Modifier.height(30.dp))
                PlayerInfoBlock(
                    label = stringResource(R.string.label_duration),
                    value = Utils.formatTime(track.trackTime.toInt()),
                )
                PlayerInfoBlock(
                    label = stringResource(R.string.label_album),
                    value = track.collectionName,
                )
                PlayerInfoBlock(
                    label = stringResource(R.string.label_year),
                    value = extractYear(track.releaseDate),
                )
                PlayerInfoBlock(
                    label = stringResource(R.string.label_genre),
                    value = track.primaryGenreName,
                )
                PlayerInfoBlock(
                    label = stringResource(R.string.label_country),
                    value = track.country,
                )
            }
        }

        if (playlistSheetVisible) {
            ModalBottomSheet(
                onDismissRequest = { playlistSheetVisible = false },
                sheetState = sheetState,
                containerColor = colorResource(R.color.second_background),
                scrimColor = Color(0x99000000),
                dragHandle = null,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
                val screenHeight = LocalConfiguration.current.screenHeightDp.dp
                val sheetMax = screenHeight * 2f / 3f
                Column(
                    modifier =
                        Modifier
                            .fillMaxWidth()
                            .height(sheetMax)
                            .padding(start = 16.dp, top = 8.dp, end = 16.dp, bottom = 24.dp),
                ) {
                    Box(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp, bottom = 16.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        Surface(
                            modifier =
                                Modifier
                                    .width(50.dp)
                                    .height(4.dp),
                            shape = RoundedCornerShape(2.dp),
                            color = colorResource(R.color.bottom_sheet_handle_color),
                        ) {}
                    }
                    Text(
                        text = stringResource(R.string.add_to_playlist_title),
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontFamily = FontFamily(Font(R.font.ys_medium)),
                        fontSize = 19.sp,
                        color = colorResource(R.color.main_text_color),
                    )
                    Button(
                        onClick = {
                            playlistSheetVisible = false
                            navController.navigate(AppRoutes.createPlaylistRoute(0L))
                        },
                        modifier =
                            Modifier
                                .padding(top = 28.dp)
                                .align(Alignment.CenterHorizontally),
                        shape = RoundedCornerShape(54.dp),
                        elevation = ButtonDefaults.buttonElevation(0.dp),
                        colors =
                            ButtonDefaults.buttonColors(
                                containerColor = colorResource(R.color.placeholder_note_color),
                                contentColor = colorResource(R.color.second_background),
                            ),
                    ) {
                        Text(
                            text = stringResource(R.string.new_playlist),
                            fontFamily = FontFamily(Font(R.font.ys_medium)),
                            fontSize = 14.sp,
                            modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                        )
                    }
                    LazyColumn(
                        modifier =
                            Modifier
                                .fillMaxWidth()
                                .weight(1f)
                                .padding(top = 24.dp),
                    ) {
                        items(playlists, key = { it.playlistId }) { pl ->
                            PlayerPlaylistBottomSheetRow(
                                playlist = pl,
                                onClick = {
                                    viewModel.onPlaylistClicked(pl)
                                },
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PlayerPlaylistBottomSheetRow(
    playlist: PlaylistUI,
    onClick: () -> Unit,
) {
    val context = LocalContext.current
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val coverFile = playlist.coverPath?.let { File(it) }
        val corner = dimensionResource(R.dimen.corner_8)
        AsyncImage(
            model =
                ImageRequest.Builder(context)
                    .data(coverFile)
                    .crossfade(true)
                    .build(),
            contentDescription = null,
            modifier =
                Modifier
                    .size(45.dp)
                    .clip(RoundedCornerShape(corner)),
            contentScale = ContentScale.Crop,
            placeholder = painterResource(R.drawable.placeholder_track),
            error = painterResource(R.drawable.placeholder_track),
        )
        Column(
            modifier =
                Modifier
                    .padding(start = 8.dp)
                    .weight(1f),
        ) {
            Text(
                text = playlist.name,
                fontFamily = FontFamily(Font(R.font.ys_medium)),
                fontSize = 16.sp,
                color = colorResource(R.color.main_text_color),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text =
                    context.resources.getQuantityString(
                        R.plurals.tracks_count,
                        playlist.tracksCount,
                        playlist.tracksCount,
                    ),
                modifier = Modifier.padding(top = 1.dp),
                fontFamily = FontFamily(Font(R.font.ys_regular)),
                fontSize = 11.sp,
                color = colorResource(R.color.bottom_sheet_handle_color),
            )
        }
    }
}

@Composable
private fun PlayerActionsBlock(
    progress: String,
    isPlaying: Boolean,
    isPlayEnabled: Boolean,
    isFavorite: Boolean,
    onPlayPause: () -> Unit,
    onFavorite: () -> Unit,
    onAddToPlaylist: () -> Unit,
) {
    val playPauseUpdated by rememberUpdatedState(onPlayPause)
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.second_background)),
    ) {
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = onAddToPlaylist,
                modifier = Modifier.size(51.dp),
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_add_track_51),
                    contentDescription = null,
                    modifier = Modifier.size(51.dp),
                )
            }
            Spacer(Modifier.width(55.dp))
            AndroidView(
                factory = { ctx ->
                    val px = (ctx.resources.displayMetrics.density * 100f).toInt()
                    PlaybackButtonView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(px, px)
                        setOnClickListener { playPauseUpdated() }
                    }
                },
                update = { view ->
                    view.setPlaying(isPlaying)
                    view.isEnabled = isPlayEnabled
                },
                modifier = Modifier.size(100.dp),
            )
            Spacer(Modifier.width(55.dp))
            IconButton(
                onClick = onFavorite,
                modifier = Modifier.size(51.dp),
            ) {
                Image(
                    painter =
                        painterResource(
                            if (isFavorite) {
                                R.drawable.ic_like_button_active_51
                            } else {
                                R.drawable.ic_like_button_51
                            },
                        ),
                    contentDescription = null,
                    modifier = Modifier.size(51.dp),
                )
            }
        }
        Text(
            text = progress,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
            textAlign = TextAlign.Center,
            fontFamily = FontFamily(Font(R.font.ys_medium)),
            fontSize = 14.sp,
            color = colorResource(R.color.main_text_color),
        )
    }
}

@Composable
private fun PlayerInfoBlock(
    label: String,
    value: String?,
) {
    if (value.isNullOrBlank()) return
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(32.dp)
                .padding(horizontal = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            fontFamily = FontFamily(Font(R.font.ys_regular)),
            fontSize = 13.sp,
            color = colorResource(R.color.gray),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
        Text(
            text = value,
            fontFamily = FontFamily(Font(R.font.ys_regular)),
            fontSize = 13.sp,
            color = colorResource(R.color.main_text_color),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.End,
        )
    }
}

private fun extractYear(releaseDate: String?): String? {
    if (releaseDate.isNullOrBlank()) return null
    return if (releaseDate.length >= 4 && releaseDate[0].isDigit()) {
        releaseDate.substring(0, 4)
    } else {
        null
    }
}
