package com.mybrain.playlistmaker.presentation.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.switchmaterial.SwitchMaterial
import com.mybrain.playlistmaker.R
import org.koin.androidx.viewmodel.ext.android.viewModel

class SettingsFragment : Fragment() {

    private lateinit var shareContainer: View
    private lateinit var supportContainer: View
    private lateinit var licenseContainer: View
    private lateinit var themeContainer: SwitchMaterial
    private val viewModel: SettingsViewModel by viewModel()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        observeState()
        observeEvents()
        setupListeners()
    }

    private fun initViews(view: View) {
        shareContainer = view.findViewById(R.id.container_share)
        supportContainer = view.findViewById(R.id.container_support)
        licenseContainer = view.findViewById(R.id.container_license)
        themeContainer = view.findViewById(R.id.switch_theme)
    }

    private fun observeState() {
        viewModel.state.observe(viewLifecycleOwner) { state ->
            if (themeContainer.isChecked != state.isDarkTheme) {
                themeContainer.isChecked = state.isDarkTheme
            }
        }
    }

    private fun observeEvents() {
        viewModel.events.observe(viewLifecycleOwner) { event ->
            when (event) {
                SettingsEvent.Share -> shareApp()
                SettingsEvent.Support -> writeToSupport()
                SettingsEvent.License -> openLicenseAgreement()
                null -> return@observe
            }
            viewModel.onEventHandled()
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
        findNavController().navigate(R.id.action_settingsFragment_to_eulaFragment)
    }
}