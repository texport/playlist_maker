package com.mybrain.playlistmaker.presentation.player

import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Utils
import com.mybrain.playlistmaker.presentation.entity.TrackUI
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.core.parameter.parametersOf

class PlayerActivity : AppCompatActivity() {
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

    private val track: TrackUI by lazy {
        intent.getParcelableExtra(EXTRA_TRACK_ID) ?: error("Track not found")
    }

    private val viewModel: PlayerViewModel by viewModel {
        parametersOf(track)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        initViews()
        initToolbar()
        bindStaticTrackInfo(track)

        observeViewModel()

        btnPlayPause.setOnClickListener {
            viewModel.onPlayPauseClicked()
        }
    }

    override fun onPause() {
        super.onPause()
        viewModel.onScreenPaused()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        ivArtwork = findViewById(R.id.ivArtwork)
        tvTitle = findViewById(R.id.tvTitle)
        tvAuthor = findViewById(R.id.tvAuthor)
        tvProgress = findViewById(R.id.tvPreviewTime)
        btnPlayPause = findViewById(R.id.btnPlay)

        itemDuration = findViewById(R.id.itemDuration)
        itemAlbum = findViewById(R.id.itemAlbum)
        itemYear = findViewById(R.id.itemYear)
        itemGenre = findViewById(R.id.itemGenre)
        itemCountry = findViewById(R.id.itemCountry)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun observeViewModel() {
        viewModel.uiState.observe(this) { state ->
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

        Glide.with(this)
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

    companion object {
        const val EXTRA_TRACK_ID = "extra_track_id"
    }
}