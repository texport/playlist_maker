package com.mybrain.playlistmaker.presentation.player

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivArtwork: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var tvProgress: TextView
    private lateinit var btnPlayPause: ImageButton
    private lateinit var itemDuration: View
    private lateinit var itemAlbum: View
    private lateinit var itemYear: View
    private lateinit var itemGenre: View
    private lateinit var itemCountry: View

    private val args by navArgs<PlayerFragmentArgs>()

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(args.track)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_player, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        initToolbar()
        bindStaticTrackInfo(args.track)
        observeViewModel()

        btnPlayPause.setOnClickListener {
            viewModel.onPlayPauseClicked()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onScreenPaused()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.releasePlayer()
    }

    private fun initViews(view: View) {
        toolbar = view.findViewById(R.id.toolbar)
        ivArtwork = view.findViewById(R.id.ivArtwork)
        tvTitle = view.findViewById(R.id.tvTitle)
        tvAuthor = view.findViewById(R.id.tvAuthor)
        tvProgress = view.findViewById(R.id.tvPreviewTime)
        btnPlayPause = view.findViewById(R.id.btnPlay)

        itemDuration = view.findViewById(R.id.itemDuration)
        itemAlbum = view.findViewById(R.id.itemAlbum)
        itemYear = view.findViewById(R.id.itemYear)
        itemGenre = view.findViewById(R.id.itemGenre)
        itemCountry = view.findViewById(R.id.itemCountry)
    }

    private fun initToolbar() {
        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(viewLifecycleOwner) { state ->
            tvProgress.text = state.progress

            btnPlayPause.isEnabled = state.isPlayButtonEnabled
            btnPlayPause.setBackgroundResource(
                if (state.isPlaying) R.drawable.ic_pause_button_100
                else R.drawable.ic_play_button_100
            )
        }
    }

    private fun bindStaticTrackInfo(track: TrackUI) {
        tvTitle.text = track.trackName
        tvAuthor.text = track.artistName

        val bigArtworkUrl = track.artworkUrl100.replace("100x100bb", "512x512bb")
        val radius = resources.getDimensionPixelSize(R.dimen.corner_8)

        Glide.with(requireContext())
            .load(bigArtworkUrl)
            .transform(CenterCrop(), RoundedCorners(radius))
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.placeholder_track)
            .fallback(R.drawable.placeholder_track)
            .into(ivArtwork)

        itemDuration.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_duration)
        itemAlbum.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_album)
        itemYear.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_year)
        itemGenre.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_genre)
        itemCountry.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_country)

        bindInfoRow(
            rowView = itemDuration,
            value = Utils.formatTime(track.trackTime.toInt())
        )
        bindInfoRow(itemAlbum, track.collectionName)
        bindInfoRow(itemYear, extractYear(track.releaseDate))
        bindInfoRow(itemGenre, track.primaryGenreName)
        bindInfoRow(itemCountry, track.country)
    }

    private fun bindInfoRow(rowView: View, value: String?) {
        val tvValue = rowView.findViewById<TextView>(R.id.tvValue)
        if (value.isNullOrBlank()) {
            rowView.visibility = View.GONE
        } else {
            rowView.visibility = View.VISIBLE
            tvValue.text = value
        }
    }

    private fun extractYear(releaseDate: String?): String? {
        if (releaseDate.isNullOrBlank()) return null
        return if (releaseDate.length >= 4 && releaseDate[0].isDigit()) {
            releaseDate.substring(0, 4)
        } else {
            null
        }
    }
}