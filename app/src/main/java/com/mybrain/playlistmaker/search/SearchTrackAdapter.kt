package com.mybrain.playlistmaker.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.models.Track

class SearchTrackAdapter(
    private val searchTrack: List<Track>
) : RecyclerView.Adapter<SearchTrackVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTrackVH = SearchTrackVH(parent)

    override fun onBindViewHolder(holder: SearchTrackVH, position: Int) = holder.bind(searchTrack[position])

    override fun getItemCount(): Int = searchTrack.size
}