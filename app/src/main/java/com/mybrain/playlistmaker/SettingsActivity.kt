package com.mybrain.playlistmaker

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.core.net.toUri

class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings_activity)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        val shareContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container_share)
        val supportContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container_support)
        val licenseContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container_license)
        val themeContainer = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.container_theme)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        shareContainer.setOnClickListener {
            shareApp()
        }

        supportContainer.setOnClickListener {
            writeToSupport()
        }

        licenseContainer.setOnClickListener {
            openLicenseAgreement()
        }
    }

    private fun shareApp() {
        val shareIntent = Intent(Intent.ACTION_SEND)
        shareIntent.type = "text/plain"
        shareIntent.putExtra(Intent.EXTRA_TEXT, getString(R.string.android_course_url))
        startActivity(Intent.createChooser(shareIntent, getString(R.string.share)))
    }

    private fun writeToSupport() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        val email = getString(R.string.developer_email)
        val subject = getString(R.string.support_email_subject)
        val emailBody = getString(R.string.email_body)

        emailIntent.data = "mailto:".toUri()
        emailIntent.putExtra(Intent.EXTRA_EMAIL, email)
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject)
        emailIntent.putExtra(Intent.EXTRA_TEXT, emailBody)

        startActivity(emailIntent)
    }

    private fun openLicenseAgreement() {
        val url = getString(R.string.license_agreement_url)
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}