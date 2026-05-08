package com.mybrain.playlistmaker.presentation.playlist.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberBottomSheetScaffoldState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.min
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.playlist.PlaylistViewModel
import com.mybrain.playlistmaker.presentation.search.ui.SearchTrackRow
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val MENU_HEIGHT_RATIO = 0.479f
private const val LONG_PRESS_SCALE = 0.97f
private const val PRESS_ANIM_MS = 80
private const val RELEASE_ANIM_MS = 120
private const val LONG_PRESS_DIALOG_DELAY_MS = 120L

@Composable
fun PlaylistRoute(
    viewModel: PlaylistViewModel,
    playlistId: Long,
    onNavigateUp: () -> Unit,
    onOpenPlayer: (TrackUI) -> Unit,
    onEditPlaylist: (Long) -> Unit,
    onPlaylistDeletedResult: (String) -> Unit,
    showMessage: (String) -> Unit,
    onSharePlaylist: (PlaylistUI, List<TrackUI>) -> Unit,
    onRequestDeleteTrack: (TrackUI) -> Unit,
    onRequestDeletePlaylist: () -> Unit,
) {
    val context = LocalContext.current
    val density = LocalDensity.current

    val playlist by viewModel.playlist.observeAsState()
    val tracks by viewModel.tracks.observeAsState(emptyList())
    val durationMinutes by viewModel.durationMinutes.observeAsState("00")
    val playlistDeleted by viewModel.playlistDeleted.observeAsState()

    var rootHeightPx by remember { mutableIntStateOf(0) }
    var actionsBottomInRootPx by remember { mutableIntStateOf(0) }
    var menuVisible by remember { mutableStateOf(false) }
    var pendingShare by remember { mutableStateOf(false) }

    val menuSheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val spacing16Px = remember {
        context.resources.getDimensionPixelSize(R.dimen.spacing_16)
    }
    val spacing8Px = remember {
        context.resources.getDimensionPixelSize(R.dimen.spacing_8)
    }

    val peekHeightPx =
        remember(rootHeightPx, actionsBottomInRootPx) {
            if (rootHeightPx <= 0 || actionsBottomInRootPx <= 0) {
                0
            } else {
                val topOffset = actionsBottomInRootPx + spacing16Px + spacing8Px
                (rootHeightPx - topOffset).coerceAtLeast(0)
            }
        }

    val peekDp =
        with(density) {
            peekHeightPx.toDp().coerceAtLeast(1.dp)
        }

    val scaffoldState = rememberBottomSheetScaffoldState()

    LaunchedEffect(playlistDeleted) {
        val name = playlistDeleted ?: return@LaunchedEffect
        if (name.isBlank()) return@LaunchedEffect
        onPlaylistDeletedResult(name)
        viewModel.clearPlaylistDeleted()
        onNavigateUp()
    }

    LaunchedEffect(menuVisible, pendingShare) {
        if (!menuVisible && pendingShare) {
            val pl = playlist
            if (pl != null && tracks.isNotEmpty()) {
                onSharePlaylist(pl, tracks)
            }
            pendingShare = false
        }
    }

    Box(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colorResource(R.color.ya_gray))
                .onGloballyPositioned { coords ->
                    rootHeightPx = coords.size.height
                },
    ) {
        BottomSheetScaffold(
            scaffoldState = scaffoldState,
            sheetPeekHeight = peekDp,
            sheetSwipeEnabled = true,
            sheetDragHandle = null,
            sheetContainerColor = colorResource(R.color.second_background),
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                PlaylistTracksSheetContent(
                    tracks = tracks,
                    onTrackClick = onOpenPlayer,
                    onTrackLongPress = onRequestDeleteTrack,
                )
            },
            content = {
                PlaylistHeaderContent(
                    playlist = playlist,
                    durationLabel =
                        stringResource(
                            R.string.playlist_duration_minutes,
                            durationMinutes,
                        ),
                    tracksCountLabel = formatTracksCount(tracks.size),
                    onNavigateUp = onNavigateUp,
                    onShareClick = {
                        if (tracks.isEmpty()) {
                            showMessage(context.getString(R.string.playlist_share_empty))
                        } else {
                            val pl = playlist ?: return@PlaylistHeaderContent
                            onSharePlaylist(pl, tracks)
                        }
                    },
                    onMenuClick = { menuVisible = true },
                    onActionsPositioned = { bottomInRoot ->
                        actionsBottomInRootPx = bottomInRoot
                    },
                )
            },
        )

        if (menuVisible) {
            ModalBottomSheet(
                onDismissRequest = { menuVisible = false },
                sheetState = menuSheetState,
                containerColor = colorResource(R.color.second_background),
                scrimColor = Color(0x99000000),
                dragHandle = null,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            ) {
                PlaylistMenuSheet(
                    playlist = playlist,
                    tracksCountLabel = formatTracksCount(tracks.size),
                    onShare = {
                        pendingShare = true
                        menuVisible = false
                    },
                    onEdit = {
                        menuVisible = false
                        onEditPlaylist(playlistId)
                    },
                    onDelete = {
                        menuVisible = false
                        onRequestDeletePlaylist()
                    },
                )
            }
        }
    }
}

@Composable
private fun formatTracksCount(count: Int): String {
    val context = LocalContext.current
    return context.resources.getQuantityString(R.plurals.tracks_count, count, count)
}

@Composable
private fun PlaylistHeaderContent(
    playlist: PlaylistUI?,
    durationLabel: String,
    tracksCountLabel: String,
    onNavigateUp: () -> Unit,
    onShareClick: () -> Unit,
    onMenuClick: () -> Unit,
    onActionsPositioned: (Int) -> Unit,
) {
    val context = LocalContext.current
    if (playlist == null) return

    Column(modifier = Modifier.fillMaxSize()) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            val maxCover = 360.dp
            val coverHeight = min(maxWidth, maxCover)

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(coverHeight),
            ) {
                val coverFile = playlist.coverPath?.let { File(it) }
                AsyncImage(
                    model =
                        ImageRequest.Builder(context)
                            .data(coverFile)
                            .crossfade(true)
                            .build(),
                    contentDescription = stringResource(R.string.playlist_cover),
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop,
                    placeholder = painterResource(R.drawable.placeholder_track),
                    error = painterResource(R.drawable.placeholder_track),
                )
                CenterAlignedTopAppBar(
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
                        TopAppBarDefaults.centerAlignedTopAppBarColors(
                            containerColor = Color.Transparent,
                        ),
                    windowInsets = WindowInsets(0, 0, 0, 0),
                )
            }
        }

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, end = 16.dp)
                    .padding(top = 24.dp),
        ) {
            Text(
                text = playlist.name,
                fontFamily = FontFamily(Font(R.font.ys_bold)),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = colorResource(R.color.ya_black),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            val desc = playlist.description.orEmpty()
            if (desc.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = desc,
                    fontFamily = FontFamily(Font(R.font.ys_regular)),
                    fontSize = 18.sp,
                    color = colorResource(R.color.ya_black),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = durationLabel,
                    fontFamily = FontFamily(Font(R.font.ys_regular)),
                    fontSize = 18.sp,
                    color = colorResource(R.color.ya_black),
                )
                Text(
                    text = "•",
                    modifier = Modifier.padding(start = 5.dp),
                    fontFamily = FontFamily(Font(R.font.ys_regular)),
                    fontSize = 13.sp,
                    color = colorResource(R.color.ya_black),
                )
                Text(
                    text = tracksCountLabel,
                    modifier = Modifier.padding(start = 5.dp),
                    fontFamily = FontFamily(Font(R.font.ys_regular)),
                    fontSize = 18.sp,
                    color = colorResource(R.color.ya_black),
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier =
                    Modifier.onGloballyPositioned { coords ->
                        val bottom = coords.positionInRoot().y + coords.size.height
                        onActionsPositioned(bottom.toInt())
                    },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier =
                        Modifier
                            .size(32.dp)
                            .clickable(onClick = onShareClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_share_24),
                        contentDescription = stringResource(R.string.share_action),
                        tint = colorResource(R.color.ya_black),
                        modifier = Modifier.size(24.dp),
                    )
                }
                Box(
                    modifier =
                        Modifier
                            .padding(start = 16.dp)
                            .size(32.dp)
                            .clickable(onClick = onMenuClick),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_more_vert_24),
                        contentDescription = stringResource(R.string.playlist_menu),
                        tint = colorResource(R.color.ya_black),
                        modifier = Modifier.size(24.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun ColumnScope.PlaylistTracksSheetContent(
    tracks: List<TrackUI>,
    onTrackClick: (TrackUI) -> Unit,
    onTrackLongPress: (TrackUI) -> Unit,
) {
    val scope = rememberCoroutineScope()
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(top = 8.dp, bottom = 24.dp),
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
        if (tracks.isEmpty()) {
            Text(
                text = stringResource(R.string.playlist_empty_tracks),
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 24.dp),
                textAlign = TextAlign.Center,
                fontFamily = FontFamily(Font(R.font.ys_medium)),
                fontSize = 19.sp,
                color = colorResource(R.color.main_text_color),
            )
        } else {
            LazyColumn(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp)
                        .weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
            ) {
                items(tracks, key = { it.trackId }) { track ->
                    PlaylistTrackRowWithLongPressAnimation(
                        track = track,
                        onClick = { onTrackClick(track) },
                        onLongPressAfterAnimation = {
                            onTrackLongPress(track)
                        },
                        scope = scope,
                    )
                }
            }
        }
    }
}

@Composable
private fun PlaylistTrackRowWithLongPressAnimation(
    track: TrackUI,
    onClick: () -> Unit,
    onLongPressAfterAnimation: () -> Unit,
    scope: CoroutineScope,
) {
    val scale = remember(track.trackId) { Animatable(1f) }
    Box(
        modifier =
            Modifier.graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            },
    ) {
        SearchTrackRow(
            track = track,
            onClick = onClick,
            onLongClick = {
                scope.launch {
                    scale.animateTo(LONG_PRESS_SCALE, tween(PRESS_ANIM_MS))
                    scale.animateTo(1f, tween(RELEASE_ANIM_MS))
                    delay(LONG_PRESS_DIALOG_DELAY_MS)
                    onLongPressAfterAnimation()
                }
            },
        )
    }
}

@Composable
private fun PlaylistMenuSheet(
    playlist: PlaylistUI?,
    tracksCountLabel: String,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
) {
    if (playlist == null) return
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val menuHeight = screenHeightDp * MENU_HEIGHT_RATIO

    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .height(menuHeight)
                .padding(bottom = 24.dp),
    ) {
        Box(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp, bottom = 20.dp),
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
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 13.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            val coverFile = playlist.coverPath?.let { File(it) }
            val context = LocalContext.current
            AsyncImage(
                model =
                    ImageRequest.Builder(context)
                        .data(coverFile)
                        .crossfade(true)
                        .build(),
                contentDescription = stringResource(R.string.playlist_cover),
                modifier =
                    Modifier
                        .size(45.dp)
                        .clip(RoundedCornerShape(8.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder_track),
                error = painterResource(R.drawable.placeholder_track),
            )
            Column(
                modifier =
                    Modifier
                        .padding(start = 8.dp, top = 6.dp)
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
                    text = tracksCountLabel,
                    modifier = Modifier.padding(top = 1.dp),
                    fontFamily = FontFamily(Font(R.font.ys_regular)),
                    fontSize = 11.sp,
                    color = colorResource(R.color.bottom_sheet_handle_color),
                )
            }
        }
        Text(
            text = stringResource(R.string.share_action),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 37.dp)
                    .clickable(onClick = onShare),
            fontFamily = FontFamily(Font(R.font.ys_regular)),
            fontSize = 16.sp,
            color = colorResource(R.color.main_text_color),
        )
        Text(
            text = stringResource(R.string.edit_playlist),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 42.dp)
                    .clickable(onClick = onEdit),
            fontFamily = FontFamily(Font(R.font.ys_regular)),
            fontSize = 16.sp,
            color = colorResource(R.color.main_text_color),
        )
        Text(
            text = stringResource(R.string.delete_playlist),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 16.dp, top = 42.dp)
                    .clickable(onClick = onDelete),
            fontFamily = FontFamily(Font(R.font.ys_regular)),
            fontSize = 16.sp,
            color = colorResource(R.color.main_text_color),
        )
    }
}
