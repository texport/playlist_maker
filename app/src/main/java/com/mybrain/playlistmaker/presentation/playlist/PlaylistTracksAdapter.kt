package com.mybrain.playlistmaker.presentation.playlist

import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import com.mybrain.playlistmaker.presentation.search.SearchTrackVH

class PlaylistTracksAdapter(
    private val tracks: MutableList<TrackUI>,
    private val onItemClick: (TrackUI) -> Unit,
    private val onItemLongClick: (TrackUI) -> Unit
) : RecyclerView.Adapter<SearchTrackVH>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchTrackVH {
        return SearchTrackVH(parent)
    }

    override fun onBindViewHolder(holder: SearchTrackVH, position: Int) {
        val item = tracks[position]
        holder.bind(item)
        holder.itemView.animate().cancel()
        holder.itemView.scaleX = 1f
        holder.itemView.scaleY = 1f
        holder.itemView.setOnClickListener { onItemClick(item) }
        holder.itemView.setOnLongClickListener {
            holder.itemView.animate()
                .scaleX(PRESS_SCALE)
                .scaleY(PRESS_SCALE)
                .setDuration(PRESS_ANIM_DURATION_MS)
                .setInterpolator(DecelerateInterpolator())
                .withEndAction {
                    holder.itemView.animate()
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(RELEASE_ANIM_DURATION_MS)
                        .setInterpolator(DecelerateInterpolator())
                        .start()
                }
                .start()
            holder.itemView.postDelayed({
                if (!holder.itemView.isAttachedToWindow) return@postDelayed
                onItemLongClick(item)
            }, LONG_PRESS_DIALOG_DELAY_MS)
            true
        }
    }

    override fun getItemCount(): Int = tracks.size

    fun updateData(newItems: List<TrackUI>) {
        tracks.clear()
        tracks.addAll(newItems)
        notifyDataSetChanged()
    }

    companion object {
        private const val PRESS_SCALE = 0.97f
        private const val PRESS_ANIM_DURATION_MS = 80L
        private const val RELEASE_ANIM_DURATION_MS = 120L
        private const val LONG_PRESS_DIALOG_DELAY_MS = 120L
    }
}
