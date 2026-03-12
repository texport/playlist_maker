package com.mybrain.playlistmaker.presentation.media

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.databinding.ItemPlaylistBinding
import com.mybrain.playlistmaker.presentation.entity.PlaylistUI
import java.io.File

class PlaylistsAdapter : ListAdapter<PlaylistUI, PlaylistsAdapter.PlaylistViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val binding = ItemPlaylistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlaylistViewHolder(binding)
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class PlaylistViewHolder(private val binding: ItemPlaylistBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(item: PlaylistUI) {
            binding.tvName.text = item.name
            binding.tvCount.text = binding.root.resources.getQuantityString(
                R.plurals.tracks_count,
                item.tracksCount,
                item.tracksCount
            )

            val radius = binding.root.resources.getDimensionPixelSize(R.dimen.corner_8)
            val coverFile = item.coverPath?.let { File(it) }

            Glide.with(binding.ivCover)
                .load(coverFile)
                .transform(CenterCrop(), RoundedCorners(radius))
                .placeholder(R.drawable.placeholder_track)
                .error(R.drawable.placeholder_track)
                .fallback(R.drawable.placeholder_track)
                .into(binding.ivCover)
        }
    }

    private class DiffCallback : DiffUtil.ItemCallback<PlaylistUI>() {
        override fun areItemsTheSame(oldItem: PlaylistUI, newItem: PlaylistUI): Boolean {
            return oldItem.playlistId == newItem.playlistId
        }

        override fun areContentsTheSame(oldItem: PlaylistUI, newItem: PlaylistUI): Boolean {
            return oldItem == newItem
        }
    }
}
