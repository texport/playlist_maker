package com.mybrain.playlistmaker.presentation.search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.TrackUI

@Composable
fun SearchTrackRow(
    track: TrackUI,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val trackNameFont = FontFamily(Font(R.font.ys_regular))
    val artistFont = FontFamily(Font(R.font.ys_regular))

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(61.dp)
            .background(colorResource(R.color.second_background))
            .clickable(onClick = onClick),
        verticalAlignment = Alignment.Top,
    ) {
        Spacer(modifier = Modifier.width(13.dp))
        Box(
            modifier = Modifier
                .padding(top = 8.dp)
                .size(45.dp),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(track.artworkUrl100)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(2.dp)),
                contentScale = ContentScale.Crop,
                placeholder = painterResource(R.drawable.placeholder_track),
                error = painterResource(R.drawable.placeholder_track),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(start = 8.dp, top = 14.dp, bottom = 14.dp),
        ) {
            Text(
                text = track.trackName,
                modifier = Modifier.height(19.dp),
                style = TextStyle(
                    fontFamily = trackNameFont,
                    fontSize = 16.sp,
                    lineHeight = 19.sp,
                    color = colorResource(R.color.search_item_track_name_color),
                    platformStyle = PlatformTextStyle(includeFontPadding = false),
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(1.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(13.dp),
                horizontalArrangement = Arrangement.Start,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = track.artistName,
                    modifier = Modifier.weight(1f, fill = false),
                    style = TextStyle(
                        fontFamily = artistFont,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        color = colorResource(R.color.search_item_artist_name_color),
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Icon(
                    painter = painterResource(R.drawable.ic_dot_13),
                    contentDescription = null,
                    tint = colorResource(R.color.search_item_spacer_color),
                    modifier = Modifier.size(13.dp),
                )
                Text(
                    text = Utils.formatTime(track.trackTime.toInt()),
                    style = TextStyle(
                        fontFamily = artistFont,
                        fontSize = 11.sp,
                        lineHeight = 13.sp,
                        color = colorResource(R.color.search_item_artist_name_color),
                        platformStyle = PlatformTextStyle(includeFontPadding = false),
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Clip,
                )
            }
        }
        Icon(
            painter = painterResource(R.drawable.ic_arrow_24),
            contentDescription = null,
            tint = colorResource(R.color.main_text_color),
            modifier = Modifier
                .align(Alignment.CenterVertically)
                .padding(start = 1.dp, end = 12.dp)
                .size(24.dp),
        )
    }
}
