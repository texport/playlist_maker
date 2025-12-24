package com.mybrain.playlistmaker.presentation

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.presentation.media.MediaActivity
import com.mybrain.playlistmaker.presentation.search.SearchActivity
import com.mybrain.playlistmaker.presentation.settings.SettingsActivity

class MainActivity : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val searchButton = findViewById<Button>(R.id.searchButton)
        val mediaButton = findViewById<Button>(R.id.mediaButton)
        val settingsButton = findViewById<Button>(R.id.settingsButton)

        settingsButton.setOnClickListener { navigateTo(SettingsActivity::class.java) }
        mediaButton.setOnClickListener { navigateTo(MediaActivity::class.java) }
        searchButton.setOnClickListener { navigateTo(SearchActivity::class.java) }
    }

    private fun navigateTo(activityClass: Class<*>) {
        val intent = Intent(this, activityClass)
        startActivity(intent)
    }
}