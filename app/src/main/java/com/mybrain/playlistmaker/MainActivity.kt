package com.mybrain.playlistmaker

import android.annotation.SuppressLint
import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import com.mybrain.playlistmaker.search.SearchActivity

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