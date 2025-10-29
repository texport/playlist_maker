package com.mybrain.playlistmaker.player

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import com.mybrain.playlistmaker.models.Track

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
    private var mediaPlayer = MediaPlayer()
    private val handler by lazy { Handler(Looper.getMainLooper()) }
    private enum class PlayerState { STATE_DEFAULT, STATE_PLAYING, STATE_PAUSED, STATE_PREPARED }
    private var playerState = PlayerState.STATE_DEFAULT
    private var shouldPlayWhenPrepared = false
    private var isReleased = false

    private val progressRunnable = object : Runnable {
        override fun run() {
            if (playerState == PlayerState.STATE_PLAYING) {
                val positionMs = mediaPlayer.currentPosition
                tvProgress.text = Utils.formatTime(positionMs)
                handler.postDelayed(this, 500L)
            }
        }
    }
    private lateinit var track: Track

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        toolbar = findViewById(R.id.toolbar)
        ivArtwork = findViewById(R.id.ivArtwork)
        tvTitle = findViewById(R.id.tvTitle)
        tvAuthor = findViewById(R.id.tvAuthor)

        tvProgress = findViewById(R.id.tvPreviewTime)
        btnPlayPause = findViewById(R.id.btnPlay)

        renderTrackInfoBlock()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        toolbar.setNavigationOnClickListener {
            stopAndRelease()
            finish()
        }

        track = intent.getParcelableExtra(EXTRA_TRACK_ID) ?: error("Track not found")

        bindTrack(track)

        tvProgress.text = "00:00"
        showPlayButton()
        preparePlayer(track.previewUrl)

        btnPlayPause.setOnClickListener {
            when (playerState) {
                PlayerState.STATE_DEFAULT -> {
                    shouldPlayWhenPrepared = true
                    showPauseButton()
                }
                PlayerState.STATE_PREPARED -> {
                    startPlayback()
                }
                PlayerState.STATE_PLAYING -> {
                    pausePlayback()
                }
                PlayerState.STATE_PAUSED -> {
                    resumePlayback()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()
        if (playerState == PlayerState.STATE_PLAYING) {
            pausePlayback()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAndRelease()
    }

    private fun renderTrackInfoBlock() {
        itemDuration = findViewById(R.id.itemDuration)
        itemDuration.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_duration)

        itemAlbum = findViewById(R.id.itemAlbum)
        itemAlbum.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_album)

        itemYear = findViewById(R.id.itemYear)
        itemYear.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_year)

        itemGenre = findViewById(R.id.itemGenre)
        itemGenre.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_genre)

        itemCountry = findViewById(R.id.itemCountry)
        itemCountry.findViewById<TextView>(R.id.tvLabel).text = getString(R.string.label_country)
    }

    private fun bindTrack(track: Track) {
        tvTitle.text = track.trackName
        tvAuthor.text = track.artistName
        val bigArtworkUrl = track.artworkUrl100.replace("100x100bb", "512x512bb")

        val radius = this.resources.getDimensionPixelSize(R.dimen.corner_8)

        Glide.with(this)
            .load(bigArtworkUrl)
            .transform(CenterCrop(), RoundedCorners(radius))
            .placeholder(R.drawable.placeholder_track)
            .error(R.drawable.placeholder_track)
            .fallback(R.drawable.placeholder_track)
            .into(ivArtwork)

        bindInfoRow(
            rowView = itemDuration,
            value = track.trackTime
        )

        bindInfoRow(
            rowView = itemAlbum,
            value = track.collectionName
        )

        bindInfoRow(
            rowView = itemYear,
            value = extractYear(track.releaseDate)
        )

        bindInfoRow(
            rowView = itemGenre,
            value = track.primaryGenreName
        )

        bindInfoRow(
            rowView = itemCountry,
            value = track.country
        )
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

    private fun preparePlayer(previewUrl: String?) {
        if (previewUrl.isNullOrBlank()) {
            btnPlayPause.isEnabled = false
            return
        }

        mediaPlayer.setDataSource(previewUrl)

        mediaPlayer.setOnPreparedListener {
            playerState = PlayerState.STATE_PREPARED

            if (shouldPlayWhenPrepared) {
                shouldPlayWhenPrepared = false
                startPlayback()
            } else {
                showPlayButton()
            }
        }

        mediaPlayer.setOnCompletionListener {
            playerState = PlayerState.STATE_PREPARED
            stopProgressUpdates()
            tvProgress.text = "00:00"
            showPlayButton()
        }

        mediaPlayer.prepareAsync()
    }

    private fun startPlayback() {
        mediaPlayer.start()
        playerState = PlayerState.STATE_PLAYING
        showPauseButton()
        startProgressUpdates()
    }

    private fun resumePlayback() {
        mediaPlayer.start()
        playerState = PlayerState.STATE_PLAYING
        showPauseButton()
        startProgressUpdates()
    }

    private fun pausePlayback() {
        mediaPlayer.pause()
        playerState = PlayerState.STATE_PAUSED
        showPlayButton()
        stopProgressUpdates()
    }

    private fun stopAndRelease() {
        if (isReleased) return

        isReleased = true
        stopProgressUpdates()

        runCatching { mediaPlayer.stop() }
        runCatching { mediaPlayer.reset() }
        runCatching { mediaPlayer.release() }

        playerState = PlayerState.STATE_DEFAULT
    }

    private fun showPlayButton() {
        btnPlayPause.setBackgroundResource(R.drawable.ic_play_button_100)
    }

    private fun showPauseButton() {
        btnPlayPause.setBackgroundResource(R.drawable.ic_pause_button_100)
    }

    private fun startProgressUpdates() {
        handler.post(progressRunnable)
    }

    private fun stopProgressUpdates() {
        handler.removeCallbacks(progressRunnable)
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