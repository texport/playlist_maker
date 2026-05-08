package com.mybrain.playlistmaker.presentation.playlist.ui

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.livedata.observeAsState
import androidx.core.net.toUri
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.playlist.CreatePlaylistState
import com.mybrain.playlistmaker.presentation.playlist.CreatePlaylistViewModel
import com.mybrain.playlistmaker.presentation.playlist.PermissionUiState
import java.io.File

@Composable
fun CreatePlaylistRoute(
    viewModel: CreatePlaylistViewModel,
    playlistId: Long,
    onNavigateUp: () -> Unit,
    onCreatedPlaylist: (String) -> Unit,
) {
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current

    val state by viewModel.state.observeAsState(CreatePlaylistState.Idle)
    val permissionState by viewModel.permissionState.observeAsState(PermissionUiState.Idle)
    val editPlaylist by viewModel.editPlaylist.observeAsState()

    val isEditMode = playlistId > 0

    var nameText by rememberSaveable { mutableStateOf("") }
    var descriptionText by rememberSaveable { mutableStateOf("") }
    var coverUriString by rememberSaveable { mutableStateOf<String?>(null) }

    var nameFocused by remember { mutableStateOf(false) }
    var descriptionFocused by remember { mutableStateOf(false) }

    var showExitDialog by remember { mutableStateOf(false) }
    var showPermRationale by remember { mutableStateOf(false) }
    var showPermSettings by remember { mutableStateOf(false) }

    val descriptionFocusRequester = remember { FocusRequester() }

    val pickLauncher =
        rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia(),
        ) { uri ->
            if (uri != null) {
                coverUriString = uri.toString()
            }
        }

    LaunchedEffect(permissionState) {
        when (permissionState) {
            PermissionUiState.Granted -> {
                pickLauncher.launch(
                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly),
                )
                viewModel.resetPermissionState()
            }
            PermissionUiState.NeedsRationale -> showPermRationale = true
            PermissionUiState.DeniedPermanently -> showPermSettings = true
            PermissionUiState.Denied -> {
                Toast.makeText(
                    context,
                    context.getString(R.string.permission_denied),
                    Toast.LENGTH_SHORT,
                ).show()
                viewModel.resetPermissionState()
            }
            PermissionUiState.Idle -> Unit
        }
    }

    LaunchedEffect(editPlaylist) {
        val p = editPlaylist ?: return@LaunchedEffect
        if (!isEditMode) return@LaunchedEffect
        nameText = p.name
        descriptionText = p.description.orEmpty()
        coverUriString =
            p.coverPath?.let { path ->
                if (path.contains("://")) path else File(path).toUri().toString()
            }
    }

    LaunchedEffect(state) {
        when (val s = state) {
            is CreatePlaylistState.Created -> {
                if (!isEditMode) {
                    onCreatedPlaylist(s.playlistName)
                }
                viewModel.resetState()
                onNavigateUp()
            }
            CreatePlaylistState.Updated -> {
                viewModel.resetState()
                onNavigateUp()
            }
            CreatePlaylistState.Idle -> Unit
        }
    }

    LaunchedEffect(isEditMode) {
        if (isEditMode) {
            viewModel.loadPlaylistForEdit(playlistId)
        }
    }

    val toolbarTitleStyle =
        TextStyle(
            fontFamily = FontFamily(Font(R.font.ys_medium)),
            fontSize = 22.sp,
            fontWeight = FontWeight.Normal,
        )

    fun hideKeyboardAndClearFocus() {
        keyboard?.hide()
        focusManager.clearFocus()
    }

    fun handleClose() {
        if (isEditMode) {
            onNavigateUp()
            return
        }
        val hasChanges =
            nameText.isNotBlank() ||
                descriptionText.isNotBlank() ||
                coverUriString != null
        if (hasChanges) {
            showExitDialog = true
        } else {
            onNavigateUp()
        }
    }

    BackHandler {
        handleClose()
    }

    val scrollState = rememberScrollState()
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .background(colorResource(R.color.second_background))
                .verticalScroll(scrollState)
                .clickable(
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() },
                ) {
                    hideKeyboardAndClearFocus()
                },
    ) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text =
                        stringResource(
                            if (isEditMode) R.string.edit_playlist_title else R.string.create_playlist_title,
                        ),
                    style = toolbarTitleStyle,
                    color = colorResource(R.color.main_text_color),
                )
            },
            navigationIcon = {
                IconButton(onClick = { handleClose() }) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_24),
                        contentDescription = null,
                        tint = colorResource(R.color.main_text_color),
                    )
                }
            },
            colors =
                TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = colorResource(R.color.second_background),
                ),
            windowInsets = WindowInsets(0, 0, 0, 0),
        )

        Column(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
        ) {
            CreatePlaylistCoverBlock(
                coverUriString = coverUriString,
                onCoverClick = {
                    hideKeyboardAndClearFocus()
                    viewModel.onPickCoverClicked()
                },
            )

            Spacer(modifier = Modifier.height(32.dp))

            CreatePlaylistOutlinedField(
                value = nameText,
                onValueChange = { nameText = it },
                label = stringResource(R.string.playlist_name_hint),
                focused = nameFocused,
                empty = nameText.isBlank(),
                onFocusChange = { nameFocused = it },
                imeAction = ImeAction.Next,
                keyboardActions =
                    KeyboardActions(
                        onNext = {
                            descriptionFocusRequester.requestFocus()
                        },
                    ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(modifier = Modifier.height(16.dp))

            CreatePlaylistOutlinedField(
                value = descriptionText,
                onValueChange = { descriptionText = it },
                label = stringResource(R.string.playlist_description_hint),
                focused = descriptionFocused,
                empty = descriptionText.isBlank(),
                onFocusChange = { descriptionFocused = it },
                imeAction = ImeAction.Done,
                keyboardActions =
                    KeyboardActions(
                        onDone = {
                            hideKeyboardAndClearFocus()
                        },
                    ),
                singleLine = false,
                minLines = 1,
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .focusRequester(descriptionFocusRequester),
            )
        }

        val enabled = nameText.isNotBlank()
        Button(
            onClick = {
                hideKeyboardAndClearFocus()
                val name = nameText.trim()
                val description = descriptionText.trim().ifBlank { null }
                val cover = coverUriString
                if (isEditMode) {
                    viewModel.updatePlaylist(playlistId, name, description, cover)
                } else {
                    viewModel.createPlaylist(name, description, cover)
                }
            },
            enabled = enabled,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(start = 17.dp, end = 17.dp, top = 8.dp, bottom = 32.dp)
                    .alpha(if (enabled) 1f else 0.5f),
            shape = RoundedCornerShape(8.dp),
            colors =
                ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.blue),
                    disabledContainerColor = colorResource(R.color.gray),
                    contentColor = colorResource(R.color.white),
                    disabledContentColor = colorResource(R.color.white),
                ),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 0.dp),
        ) {
            Text(
                text =
                    stringResource(
                        if (isEditMode) {
                            R.string.save_playlist_button
                        } else {
                            R.string.create_playlist_button
                        },
                    ),
                fontFamily = FontFamily(Font(R.font.ys_medium)),
                fontSize = 16.sp,
            )
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = { Text(stringResource(R.string.exit_create_playlist_title)) },
            text = { Text(stringResource(R.string.exit_create_playlist_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitDialog = false
                        onNavigateUp()
                    },
                ) {
                    Text(stringResource(R.string.exit_create_playlist_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text(stringResource(R.string.exit_create_playlist_cancel))
                }
            },
        )
    }

    if (showPermRationale) {
        AlertDialog(
            onDismissRequest = {
                showPermRationale = false
                viewModel.resetPermissionState()
            },
            title = { Text(stringResource(R.string.permission_title)) },
            text = { Text(stringResource(R.string.permission_rationale)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermRationale = false
                        viewModel.resetPermissionState()
                        viewModel.onPickCoverClicked()
                    },
                ) {
                    Text(stringResource(R.string.permission_allow))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermRationale = false
                        viewModel.resetPermissionState()
                    },
                ) {
                    Text(stringResource(R.string.permission_cancel))
                }
            },
        )
    }

    if (showPermSettings) {
        AlertDialog(
            onDismissRequest = {
                showPermSettings = false
                viewModel.resetPermissionState()
            },
            title = { Text(stringResource(R.string.permission_title)) },
            text = { Text(stringResource(R.string.permission_settings_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPermSettings = false
                        viewModel.resetPermissionState()
                        context.startActivity(
                            Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null),
                            ),
                        )
                    },
                ) {
                    Text(stringResource(R.string.permission_settings))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showPermSettings = false
                        viewModel.resetPermissionState()
                    },
                ) {
                    Text(stringResource(R.string.permission_cancel))
                }
            },
        )
    }
}

@Composable
private fun CreatePlaylistCoverBlock(
    coverUriString: String?,
    onCoverClick: () -> Unit,
) {
    val context = LocalContext.current
    val fabMargin = 16.dp
    val dashStroke = colorResource(R.color.gray)

    Box(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(top = 26.dp)
                .aspectRatio(1f)
                .clip(RoundedCornerShape(8.dp))
                .drawBehind {
                    val strokePx = 1.dp.toPx()
                    val dash = 30.dp.toPx()
                    val gap = 30.dp.toPx()
                    drawRoundRect(
                        color = dashStroke,
                        style =
                            Stroke(
                                width = strokePx,
                                pathEffect =
                                    PathEffect.dashPathEffect(
                                        floatArrayOf(dash, gap),
                                    ),
                            ),
                        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
                    )
                }
                .clickable(onClick = onCoverClick),
    ) {
        if (coverUriString == null) {
            Image(
                painter = painterResource(R.drawable.placeholder_photo_100),
                contentDescription = stringResource(R.string.playlist_cover),
                modifier =
                    Modifier
                        .align(Alignment.Center)
                        .padding(fabMargin),
                contentScale = ContentScale.Fit,
            )
        } else {
            val corner = dimensionResource(R.dimen.corner_8)
            AsyncImage(
                model =
                    ImageRequest.Builder(context)
                        .data(coverUriString.toUri())
                        .crossfade(true)
                        .build(),
                contentDescription = stringResource(R.string.playlist_cover),
                modifier =
                    Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(corner)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder_photo_100),
                error = painterResource(R.drawable.placeholder_photo_100),
            )
        }
    }
}

@Composable
private fun CreatePlaylistOutlinedField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    focused: Boolean,
    empty: Boolean,
    onFocusChange: (Boolean) -> Unit,
    imeAction: ImeAction,
    keyboardActions: KeyboardActions,
    singleLine: Boolean,
    minLines: Int = 1,
    modifier: Modifier = Modifier,
) {
    val stroke = colorResource(R.color.create_playlist_text_edit_color)
    val mainText = colorResource(R.color.main_text_color)

    val labelColor =
        when {
            empty ->
                if (focused) {
                    stroke
                } else {
                    mainText
                }
            else -> stroke
        }

    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier =
            modifier.onFocusChanged {
                onFocusChange(it.isFocused)
            },
        label = {
            Text(
                text = label,
                color = labelColor,
                fontFamily = FontFamily(Font(R.font.ys_regular)),
                fontSize = 12.sp,
            )
        },
        textStyle =
            TextStyle(
                fontFamily = FontFamily(Font(R.font.ys_regular)),
                fontSize = 16.sp,
                color = mainText,
            ),
        singleLine = singleLine,
        minLines = minLines,
        keyboardOptions = KeyboardOptions(imeAction = imeAction),
        keyboardActions = keyboardActions,
        shape = RoundedCornerShape(4.dp),
        colors =
            OutlinedTextFieldDefaults.colors(
                focusedBorderColor = stroke,
                unfocusedBorderColor = stroke,
                cursorColor = colorResource(R.color.blue),
                focusedContainerColor = colorResource(R.color.second_background),
                unfocusedContainerColor = colorResource(R.color.second_background),
                focusedLabelColor = labelColor,
                unfocusedLabelColor = labelColor,
            ),
    )
}
