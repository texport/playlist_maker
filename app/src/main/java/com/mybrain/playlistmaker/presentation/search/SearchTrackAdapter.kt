package com.mybrain.playlistmaker.presentation.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mybrain.playlistmaker.presentation.entity.TrackUI

class SearchTrackAdapter(
    private val searchTrack: MutableList<TrackUI>,
    private val onItemClick: (TrackUI) -> Unit
) : RecyclerView.Adapter<SearchTrackVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTrackVH = SearchTrackVH(parent)

    override fun onBindViewHolder(holder: SearchTrackVH, position: Int) {
        holder.bind(searchTrack[position])
        holder.itemView.setOnClickListener { onItemClick(searchTrack[position]) }
    }

    override fun getItemCount(): Int = searchTrack.size

    fun updateData(newItems: List<TrackUI>) {
        searchTrack.clear()
        searchTrack.addAll(newItems)
        notifyDataSetChanged()
    }
}