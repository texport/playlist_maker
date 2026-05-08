package com.mybrain.playlistmaker.presentation.settings.ui

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.settings.SettingsEvent
import com.mybrain.playlistmaker.presentation.settings.SettingsState
import com.mybrain.playlistmaker.presentation.settings.SettingsViewModel

private val settingsTitleStyle
    @Composable
    get() = TextStyle(
        fontFamily = FontFamily(Font(R.font.ys_bold)),
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp,
        color = colorResource(R.color.main_text_color),
    )

private val settingsRowTextStyle
    @Composable
    get() = TextStyle(
        fontFamily = FontFamily(Font(R.font.ys_regular)),
        fontSize = 16.sp,
        color = colorResource(R.color.main_text_color),
    )

@Composable
fun SettingsRoute(viewModel: SettingsViewModel) {
    val context = LocalContext.current
    val initial = viewModel.state.value ?: SettingsState(isDarkTheme = false)
    val state by viewModel.state.observeAsState(initial)
    val event by viewModel.events.observeAsState()
    var isEulaOpened by remember { mutableStateOf(false) }

    when (event) {
        SettingsEvent.Share -> {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.android_course_url))
            }
            context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.share)))
            viewModel.onEventHandled()
        }
        SettingsEvent.Support -> {
            val emailIntent = Intent(Intent.ACTION_SENDTO).apply {
                data = "mailto:".toUri()
                putExtra(Intent.EXTRA_EMAIL, arrayOf(context.getString(R.string.developer_email)))
                putExtra(Intent.EXTRA_SUBJECT, context.getString(R.string.support_email_subject))
                putExtra(Intent.EXTRA_TEXT, context.getString(R.string.email_body))
            }
            context.startActivity(emailIntent)
            viewModel.onEventHandled()
        }
        SettingsEvent.License -> {
            isEulaOpened = true
            viewModel.onEventHandled()
        }
        null -> Unit
    }

    if (isEulaOpened) {
        BackHandler { isEulaOpened = false }
        EulaScreen(onNavigateUp = { isEulaOpened = false })
    } else {
        SettingsScreen(
            isDarkTheme = state.isDarkTheme,
            onThemeSwitched = viewModel::onThemeSwitched,
            onShareClick = viewModel::onShareClicked,
            onSupportClick = viewModel::onSupportClicked,
            onLicenseClick = viewModel::onLicenseClicked,
        )
    }
}

@Composable
fun SettingsScreen(
    isDarkTheme: Boolean,
    onThemeSwitched: (Boolean) -> Unit,
    onShareClick: () -> Unit,
    onSupportClick: () -> Unit,
    onLicenseClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rowHorizontal = dimensionResource(R.dimen.spacing_16)
    val rowVertical = 14.dp
    val titleBelowSpacing = 12.dp
    val textIconGap = dimensionResource(R.dimen.spacing_8)
    val iconSize = 24.dp

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.second_background)),
    ) {
        Text(
            text = stringResource(R.string.settings),
            style = settingsTitleStyle,
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(
                        start = rowHorizontal,
                        top = rowHorizontal,
                        end = rowHorizontal,
                        bottom = titleBelowSpacing,
                    ),
        )

        ThemeSwitchRow(
            isDarkTheme = isDarkTheme,
            onThemeSwitched = onThemeSwitched,
            rowHorizontal = rowHorizontal,
            rowVertical = rowVertical,
        )

        SettingsClickableRow(
            label = stringResource(R.string.share),
            iconRes = R.drawable.ic_share_24,
            rowHorizontal = rowHorizontal,
            rowVertical = rowVertical,
            textIconGap = textIconGap,
            iconSize = iconSize,
            onClick = onShareClick,
        )

        SettingsClickableRow(
            label = stringResource(R.string.support),
            iconRes = R.drawable.ic_support_24,
            rowHorizontal = rowHorizontal,
            rowVertical = rowVertical,
            textIconGap = textIconGap,
            iconSize = iconSize,
            onClick = onSupportClick,
        )

        SettingsClickableRow(
            label = stringResource(R.string.eula),
            iconRes = R.drawable.ic_arrow_24,
            rowHorizontal = rowHorizontal,
            rowVertical = rowVertical,
            textIconGap = textIconGap,
            iconSize = iconSize,
            onClick = onLicenseClick,
        )
    }
}

@Composable
private fun ThemeSwitchRow(
    isDarkTheme: Boolean,
    onThemeSwitched: (Boolean) -> Unit,
    rowHorizontal: Dp,
    rowVertical: Dp,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.second_background))
                .padding(horizontal = rowHorizontal, vertical = rowVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = stringResource(R.string.theme_text),
            style = settingsRowTextStyle,
            modifier = Modifier.weight(1f),
        )
        CompositionLocalProvider(LocalMinimumInteractiveComponentSize provides 0.dp) {
            Switch(
                modifier = Modifier.scale(0.9f),
                checked = isDarkTheme,
                onCheckedChange = onThemeSwitched,
                colors =
                    SwitchDefaults.colors(
                        checkedThumbColor = colorResource(R.color.white),
                        checkedTrackColor = colorResource(R.color.blue),
                        checkedBorderColor = Color.Transparent,
                        uncheckedThumbColor = colorResource(R.color.white),
                        uncheckedTrackColor = colorResource(R.color.switch_track_off),
                        uncheckedBorderColor = Color.Transparent,
                    ),
            )
        }
    }
}

@Composable
private fun SettingsClickableRow(
    label: String,
    iconRes: Int,
    rowHorizontal: Dp,
    rowVertical: Dp,
    textIconGap: Dp,
    iconSize: Dp,
    onClick: () -> Unit,
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(colorResource(R.color.second_background))
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(),
                    role = Role.Button,
                    onClick = onClick,
                )
                .padding(horizontal = rowHorizontal, vertical = rowVertical),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            style = settingsRowTextStyle,
            modifier = Modifier
                .weight(1f)
                .padding(end = textIconGap),
        )
        Icon(
            painter = painterResource(iconRes),
            contentDescription = null,
            modifier = Modifier.size(iconSize),
            tint = colorResource(R.color.icons_color),
        )
    }
}
