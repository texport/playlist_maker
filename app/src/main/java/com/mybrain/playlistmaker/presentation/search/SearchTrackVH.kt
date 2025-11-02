package com.mybrain.playlistmaker.presentation.search

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.TrackUI

class SearchTrackVH(
    parent: ViewGroup,
): RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.search_item_track, parent, false)) {
    private val image: ImageView = itemView.findViewById(R.id.ivArtwork)
    private val name: TextView = itemView.findViewById(R.id.tvTitle)
    private val author: TextView = itemView.findViewById(R.id.tvAuthor)
    private val time: TextView = itemView.findViewById(R.id.tvTime)

    fun bind(track: TrackUI) {
        val radius = itemView.context.resources.getDimensionPixelSize(R.dimen.corner_2)

        Glide.with(itemView)
            .load(track.artworkUrl100)
            .transform(CenterCrop(), RoundedCorners(radius))
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.placeholder_track)
            .fallback(R.drawable.placeholder_track)
            .into(image)
        name.text = track.trackName
        author.text = track.artistName
        time.text = Utils.formatTime((track.trackTime.toInt()))
    }
}