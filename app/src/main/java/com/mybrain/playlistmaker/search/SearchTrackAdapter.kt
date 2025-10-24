package com.mybrain.playlistmaker.search

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mybrain.playlistmaker.models.Track

class SearchTrackAdapter(
    private val searchTrack: MutableList<Track>,
    private val onItemClick: (Track) -> Unit
) : RecyclerView.Adapter<SearchTrackVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTrackVH = SearchTrackVH(parent)

    override fun onBindViewHolder(holder: SearchTrackVH, position: Int) {
        holder.bind(searchTrack[position])
        holder.itemView.setOnClickListener { onItemClick(searchTrack[position]) }
    }

    override fun getItemCount(): Int = searchTrack.size

    fun updateData(newItems: List<Track>) {
        searchTrack.clear()
        searchTrack.addAll(newItems)
        notifyDataSetChanged()
    }
}