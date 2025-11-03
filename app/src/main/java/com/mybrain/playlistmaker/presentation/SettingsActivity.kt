package com.mybrain.playlistmaker.presentation

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mybrain.playlistmaker.R
import com.mybrain.playlistmaker.Creator
import com.mybrain.playlistmaker.presentation.utils.ThemeManagerUI

class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var shareContainer: View
    private lateinit var supportContainer: View
    private lateinit var licenseContainer: View
    private lateinit var themeContainer: SwitchMaterial

    private val settings by lazy { Creator.settingsInteractor(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings_activity)

        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        shareContainer = findViewById(R.id.container_share)
        supportContainer = findViewById(R.id.container_support)
        licenseContainer = findViewById(R.id.container_license)
        themeContainer = findViewById(R.id.switch_theme)

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

        val isDark = settings.isDarkTheme()
        themeContainer.isChecked = isDark
        ThemeManagerUI.apply(isDark)

        themeContainer.setOnCheckedChangeListener { switcher, checked ->
            settings.setDarkTheme(checked)
            ThemeManagerUI.apply(checked)
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
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
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