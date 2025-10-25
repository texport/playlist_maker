package com.mybrain.playlistmaker.player

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.google.android.material.appbar.MaterialToolbar
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.models.Track

class PlayerActivity : AppCompatActivity() {
    private lateinit var toolbar: MaterialToolbar
    private lateinit var ivArtwork: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvAuthor: TextView
    private lateinit var itemDuration: View
    private lateinit var itemAlbum: View
    private lateinit var itemYear: View
    private lateinit var itemGenre: View
    private lateinit var itemCountry: View


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_player)

        toolbar = findViewById(R.id.toolbar)
        ivArtwork = findViewById(R.id.ivArtwork)
        tvTitle = findViewById(R.id.tvTitle)
        tvAuthor = findViewById(R.id.tvAuthor)

        renderTrackInfoBlock()

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = ""

        toolbar.setNavigationOnClickListener { finish() }

        val trackFromIntent = intent.getParcelableExtra<Track>(EXTRA_TRACK_ID) ?: error("Track not found")

        //val track = findTrack(trackIdFromIntent)
        bindTrack(trackFromIntent)
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