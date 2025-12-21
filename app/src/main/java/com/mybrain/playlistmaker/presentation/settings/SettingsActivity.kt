package com.mybrain.playlistmaker.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.net.toUri
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mybrain.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsActivity : AppCompatActivity() {
    private lateinit var toolbar: Toolbar
    private lateinit var shareContainer: View
    private lateinit var supportContainer: View
    private lateinit var licenseContainer: View
    private lateinit var themeContainer: SwitchMaterial
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.settings_activity)

        initViews()
        initToolbar()
        observeState()
        observeEvents()
        setupListeners()
    }

    private fun initViews() {
        toolbar = findViewById(R.id.toolbar)
        shareContainer = findViewById(R.id.container_share)
        supportContainer = findViewById(R.id.container_support)
        licenseContainer = findViewById(R.id.container_license)
        themeContainer = findViewById(R.id.switch_theme)
    }

    private fun initToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }
    }

    private fun observeState() {
        viewModel.state.observe(this) { state ->
            if (themeContainer.isChecked != state.isDarkTheme) {
                themeContainer.isChecked = state.isDarkTheme
            }
        }
    }

    private fun observeEvents() {
        viewModel.events.observe(this) { event ->
            when (event) {
                SettingsEvent.Share -> shareApp()
                SettingsEvent.Support -> writeToSupport()
                SettingsEvent.License -> openLicenseAgreement()
            }
        }
    }

    private fun setupListeners() {
        themeContainer.setOnCheckedChangeListener { _, checked ->
            viewModel.onThemeSwitched(checked)
        }

        shareContainer.setOnClickListener {
            viewModel.onShareClicked()
        }

        supportContainer.setOnClickListener {
            viewModel.onSupportClicked()
        }

        licenseContainer.setOnClickListener {
            viewModel.onLicenseClicked()
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