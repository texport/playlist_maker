package com.mybrain.playlistmaker.presentation.navigation

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.navArgument
import androidx.compose.runtime.livedata.observeAsState
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.media.FavoriteTracksViewModel
import com.mybrain.playlistmaker.presentation.media.ui.MediaRoute
import com.mybrain.playlistmaker.presentation.media.PlaylistsShareEvent
import com.mybrain.playlistmaker.presentation.media.PlaylistsViewModel
import com.mybrain.playlistmaker.presentation.playlist.ui.CreatePlaylistRoute
import com.mybrain.playlistmaker.presentation.playlist.ui.PlaylistRoute
import com.mybrain.playlistmaker.presentation.player.ui.PlayerRoute
import com.mybrain.playlistmaker.presentation.search.SearchViewModel
import com.mybrain.playlistmaker.presentation.search.ui.SearchRoute
import com.mybrain.playlistmaker.presentation.settings.SettingsViewModel
import com.mybrain.playlistmaker.presentation.settings.ui.SettingsRoute
import kotlinx.coroutines.flow.collectLatest
import com.mybrain.playlistmaker.presentation.playlist.CreatePlaylistViewModel
import com.mybrain.playlistmaker.presentation.playlist.PlaylistViewModel
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun AppNavHost(
    navController: NavHostController,
    snackbarHidesBottomBar: MutableState<Boolean>,
    navigationEvents: NavigationEvents,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val activity = context as AppCompatActivity

    LaunchedEffect(Unit) {
        navigationEvents.playlistCreated.collectLatest { name: String ->
            if (name.isNotBlank()) {
                showStyledSnackbar(
                    activity,
                    context.getString(R.string.playlist_created, name),
                    onShow = { snackbarHidesBottomBar.value = true },
                    onDismiss = { snackbarHidesBottomBar.value = false },
                )
            }
        }
    }
    LaunchedEffect(Unit) {
        navigationEvents.playlistDeleted.collectLatest { name: String ->
            if (name.isNotBlank()) {
                showStyledSnackbar(
                    activity,
                    context.getString(R.string.playlist_deleted_message, name),
                    onShow = { snackbarHidesBottomBar.value = true },
                    onDismiss = { snackbarHidesBottomBar.value = false },
                )
            }
        }
    }

    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val isTopLevelRoute =
        currentRoute == AppRoutes.MEDIA ||
            currentRoute == AppRoutes.SEARCH ||
            currentRoute == AppRoutes.SETTINGS
    val showBottomBar = isTopLevelRoute && !snackbarHidesBottomBar.value

    Scaffold(
        modifier = modifier.fillMaxSize(),
        bottomBar = {
            if (showBottomBar) {
                Column {
                    HorizontalDivider(
                        thickness = 0.5.dp,
                        color = colorResource(R.color.bottom_nav_divider),
                    )
                    NavigationBar(
                        modifier = Modifier.fillMaxWidth(),
                        containerColor = colorResource(R.color.second_background),
                    ) {
                        val tabs =
                            listOf(
                                Triple(AppRoutes.SEARCH, R.string.search, R.drawable.ic_search_24),
                                Triple(AppRoutes.MEDIA, R.string.media_library, R.drawable.ic_media_24),
                                Triple(AppRoutes.SETTINGS, R.string.settings, R.drawable.ic_settings_24),
                            )
                        tabs.forEach { (route, labelRes, iconRes) ->
                            NavigationBarItem(
                                icon = {
                                    Icon(
                                        painter = painterResource(iconRes),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                    )
                                },
                                label = { Text(stringResource(labelRes)) },
                                selected = currentRoute == route,
                                onClick = {
                                    navController.navigate(route) {
                                        popUpTo(navController.graph.findStartDestination().id) {
                                            saveState = true
                                        }
                                        launchSingleTop = true
                                        restoreState = true
                                    }
                                },
                                colors =
                                    NavigationBarItemDefaults.colors(
                                        selectedIconColor = colorResource(R.color.blue),
                                        selectedTextColor = colorResource(R.color.blue),
                                        indicatorColor = Color.Transparent,
                                        unselectedIconColor = colorResource(R.color.main_text_color),
                                        unselectedTextColor = colorResource(R.color.main_text_color),
                                    ),
                            )
                        }
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppRoutes.MEDIA,
            modifier = Modifier.padding(innerPadding),
        ) {
            composable(AppRoutes.MEDIA) {
                MediaTab(
                    navController = navController,
                    snackbarHidesBottomBar = snackbarHidesBottomBar,
                )
            }
            composable(AppRoutes.SEARCH) {
                val viewModel: SearchViewModel = koinViewModel()
                val openPlayer = rememberDebouncedPlayerNavigation(navController)
                SearchRoute(
                    viewModel = viewModel,
                    onOpenPlayer = openPlayer,
                )
            }
            composable(AppRoutes.SETTINGS) {
                val viewModel: SettingsViewModel = koinViewModel()
                SettingsRoute(viewModel = viewModel)
            }
            composable(
                route = AppRoutes.PLAYER,
                arguments =
                    listOf(
                        navArgument("track") {
                            type = TrackUiNavType
                        },
                    ),
            ) { entry ->
                val track =
                    entry.arguments?.let { bundle ->
                        TrackUiNavType.get(bundle, "track")
                    } ?: return@composable
                PlayerRoute(
                    track = track,
                    navController = navController,
                    onNavigateUp = { navController.navigateUp() },
                    snackbarHost = { message, onShow, onDismiss ->
                        showStyledSnackbar(
                            activity,
                            message,
                            onShow = {
                                snackbarHidesBottomBar.value = true
                                onShow()
                            },
                            onDismiss = {
                                snackbarHidesBottomBar.value = false
                                onDismiss()
                            },
                        )
                    },
                )
            }
            composable(
                route = AppRoutes.PLAYLIST,
                arguments =
                    listOf(
                        navArgument("playlistId") {
                            type = NavType.LongType
                        },
                    ),
            ) { entry ->
                val playlistId =
                    entry.arguments?.getLong("playlistId")
                        ?: return@composable
                PlaylistTab(
                    navController = navController,
                    playlistId = playlistId,
                    snackbarHidesBottomBar = snackbarHidesBottomBar,
                    navigationEvents = navigationEvents,
                    activity = activity,
                )
            }
            composable(
                route = AppRoutes.CREATE_PLAYLIST,
                arguments =
                    listOf(
                        navArgument("playlistId") {
                            type = NavType.LongType
                            defaultValue = 0L
                        },
                    ),
            ) { entry ->
                val playlistId =
                    entry.arguments?.getLong("playlistId") ?: 0L
                val viewModel = koinViewModel<CreatePlaylistViewModel>()
                CreatePlaylistRoute(
                    viewModel = viewModel,
                    playlistId = playlistId,
                    onNavigateUp = { navController.navigateUp() },
                    onCreatedPlaylist = { name ->
                        navigationEvents.notifyPlaylistCreated(name)
                    },
                )
            }
        }
    }
}

@Composable
private fun MediaTab(
    navController: NavController,
    snackbarHidesBottomBar: MutableState<Boolean>,
) {
    val context = LocalContext.current
    val activity = context as AppCompatActivity
    val favoriteViewModel: FavoriteTracksViewModel = koinViewModel()
    val playlistsViewModel: PlaylistsViewModel = koinViewModel()
    val openPlayer = rememberDebouncedPlayerNavigation(navController)

    val shareEvent by playlistsViewModel.shareEvent.observeAsState()
    LaunchedEffect(shareEvent) {
        val event = shareEvent ?: return@LaunchedEffect
        when (event) {
            PlaylistsShareEvent.Empty -> {
                showStyledSnackbar(
                    activity,
                    context.getString(R.string.playlist_share_empty),
                    onShow = { snackbarHidesBottomBar.value = true },
                    onDismiss = { snackbarHidesBottomBar.value = false },
                )
            }
            is PlaylistsShareEvent.Ready -> {
                val text =
                    buildShareText(context, event.playlist, event.tracks)
                val intent =
                    Intent(Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(Intent.EXTRA_TEXT, text)
                    }
                context.startActivity(
                    Intent.createChooser(intent, context.getString(R.string.share_playlist)),
                )
            }
        }
        playlistsViewModel.clearShareEvent()
    }

    val playlistDeletedName by playlistsViewModel.playlistDeleted.observeAsState()
    LaunchedEffect(playlistDeletedName) {
        val name = playlistDeletedName ?: return@LaunchedEffect
        if (name.isNotBlank()) {
            showStyledSnackbar(
                activity,
                context.getString(R.string.playlist_deleted_message, name),
                onShow = { snackbarHidesBottomBar.value = true },
                onDismiss = { snackbarHidesBottomBar.value = false },
            )
        }
        playlistsViewModel.clearPlaylistDeleted()
    }

    MediaRoute(
        favoriteViewModel = favoriteViewModel,
        playlistsViewModel = playlistsViewModel,
        onTrackClick = openPlayer,
        onOpenPlaylist = { playlistId: Long ->
            navController.navigate(AppRoutes.playlistRoute(playlistId))
        },
        onCreatePlaylist = {
            navController.navigate(AppRoutes.createPlaylistRoute(0L))
        },
        onEditPlaylist = { playlistId: Long ->
            navController.navigate(AppRoutes.createPlaylistRoute(playlistId))
        },
    )
}

@Composable
private fun rememberDebouncedPlayerNavigation(navController: NavController): (TrackUI) -> Unit {
    val lifecycleOwner = LocalLifecycleOwner.current
    var canOpen by remember { mutableStateOf(true) }
    val handler =
        remember {
            android.os.Handler(android.os.Looper.getMainLooper())
        }
    DisposableEffect(lifecycleOwner) {
        val observer =
            LifecycleEventObserver { _, event ->
                if (event == Lifecycle.Event.ON_RESUME) {
                    canOpen = true
                }
            }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            handler.removeCallbacksAndMessages(null)
        }
    }
    return remember(navController, handler) {
        nav@{ track: TrackUI ->
            if (!canOpen) return@nav
            canOpen = false
            navController.navigate(AppRoutes.playerRoute(track))
            handler.postDelayed({ canOpen = true }, 500L)
        }
    }
}

@Composable
private fun PlaylistTab(
    navController: NavController,
    playlistId: Long,
    snackbarHidesBottomBar: MutableState<Boolean>,
    navigationEvents: NavigationEvents,
    activity: AppCompatActivity,
) {
    val context = LocalContext.current
    val viewModel: PlaylistViewModel = koinViewModel(parameters = { parametersOf(playlistId) })
    val playlist by viewModel.playlist.observeAsState()
    val playlistLookupSettled by viewModel.playlistLookupSettled.observeAsState(false)

    var pendingDeleteTrack by remember { mutableStateOf<TrackUI?>(null) }
    var showDeletePlaylistDialog by remember { mutableStateOf(false) }

    LaunchedEffect(playlistLookupSettled, playlist) {
        if (playlistLookupSettled && playlist == null) {
            navController.navigateUp()
        }
    }

    pendingDeleteTrack?.let { track ->
        AlertDialog(
            onDismissRequest = { pendingDeleteTrack = null },
            text = { Text(stringResource(R.string.delete_track_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteTrack(track.trackId)
                        showStyledSnackbar(
                            activity,
                            context.getString(R.string.track_deleted_message, track.trackName),
                            onShow = { snackbarHidesBottomBar.value = true },
                            onDismiss = { snackbarHidesBottomBar.value = false },
                        )
                        pendingDeleteTrack = null
                    },
                ) {
                    Text(stringResource(R.string.delete_track_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { pendingDeleteTrack = null }) {
                    Text(stringResource(R.string.delete_track_cancel))
                }
            },
        )
    }

    if (showDeletePlaylistDialog) {
        AlertDialog(
            onDismissRequest = { showDeletePlaylistDialog = false },
            title = { Text(stringResource(R.string.delete_playlist_title)) },
            text = { Text(stringResource(R.string.delete_playlist_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        val name = playlist?.name.orEmpty()
                        viewModel.deletePlaylist(name)
                        showDeletePlaylistDialog = false
                    },
                ) {
                    Text(stringResource(R.string.delete_playlist_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeletePlaylistDialog = false }) {
                    Text(stringResource(R.string.delete_playlist_cancel))
                }
            },
        )
    }

    PlaylistRoute(
        viewModel = viewModel,
        playlistId = playlistId,
        onNavigateUp = { navController.navigateUp() },
        onOpenPlayer = { t ->
            navController.navigate(AppRoutes.playerRoute(t))
        },
        onEditPlaylist = { id ->
            navController.navigate(AppRoutes.createPlaylistRoute(id))
        },
        onPlaylistDeletedResult = { name ->
            navigationEvents.notifyPlaylistDeleted(name)
        },
        showMessage = { message ->
            showStyledSnackbar(
                activity,
                message,
                onShow = { snackbarHidesBottomBar.value = true },
                onDismiss = { snackbarHidesBottomBar.value = false },
            )
        },
        onSharePlaylist = { pl, tracks ->
            val text = buildShareText(context, pl, tracks)
            val intent =
                Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(Intent.EXTRA_TEXT, text)
                }
            context.startActivity(
                Intent.createChooser(intent, context.getString(R.string.share_playlist)),
            )
        },
        onRequestDeleteTrack = { pendingDeleteTrack = it },
        onRequestDeletePlaylist = { showDeletePlaylistDialog = true },
    )
}

private fun buildShareText(
    context: android.content.Context,
    playlist: PlaylistUI,
    tracks: List<TrackUI>,
): String {
    val builder = StringBuilder()
    builder.append(playlist.name).append('\n')
    if (!playlist.description.isNullOrBlank()) {
        builder.append(playlist.description).append('\n')
    }
    builder.append(
        context.resources.getQuantityString(R.plurals.tracks_count, tracks.size, tracks.size),
    ).append('\n')
    tracks.forEachIndexed { index, track ->
        val duration = Utils.formatTime(track.trackTime.toInt())
        builder.append(index + 1)
            .append(". ")
            .append(track.artistName)
            .append(" - ")
            .append(track.trackName)
            .append(" (")
            .append(duration)
            .append(')')
        if (index != tracks.lastIndex) {
            builder.append('\n')
        }
    }
    return builder.toString()
}
