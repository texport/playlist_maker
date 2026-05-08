package com.mybrain.playlistmaker.presentation.search

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.TrackUI

fun bindSearchItemTrackView(itemView: View, track: TrackUI) {
    val image: ImageView = itemView.findViewById(R.id.ivArtwork)
    val name: TextView = itemView.findViewById(R.id.tvTitle)
    val author: TextView = itemView.findViewById(R.id.tvAuthor)
    val time: TextView = itemView.findViewById(R.id.tvTime)
    val radius = itemView.context.resources.getDimensionPixelSize(R.dimen.corner_2)

    Glide.with(itemView).clear(image)
    image.setImageResource(R.drawable.placeholder_track)
    Glide.with(itemView)
        .load(track.artworkUrl100)
        .transform(CenterCrop(), RoundedCorners(radius))
        .placeholder(R.drawable.placeholder_track)
        .error(R.drawable.placeholder_track)
        .fallback(R.drawable.placeholder_track)
        .thumbnail(0.25f)
        .transition(DrawableTransitionOptions.withCrossFade())
        .into(image)
    name.text = track.trackName
    author.text = track.artistName
    time.text = Utils.formatTime(track.trackTime.toInt())
}

class SearchTrackVH(
    parent: ViewGroup,
) : RecyclerView.ViewHolder(
    LayoutInflater.from(parent.context).inflate(R.layout.search_item_track, parent, false),
) {
    fun bind(track: TrackUI) {
        bindSearchItemTrackView(itemView, track)
    }
}
