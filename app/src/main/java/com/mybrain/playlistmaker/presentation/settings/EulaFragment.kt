package com.mybrain.playlistmaker.presentation.settings

import android.content.res.Configuration
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.fragment.app.Fragment
import com.google.android.material.appbar.MaterialToolbar
import com.mybrain.playlistmaker.R
import androidx.navigation.fragment.findNavController
import android.webkit.WebSettings

class EulaFragment : Fragment() {

    private lateinit var toolbar: MaterialToolbar
    private lateinit var webView: WebView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_eula, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        toolbar = view.findViewById(R.id.toolbar_eula)
        webView = view.findViewById(R.id.webview_eula)

        toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        val url = getString(R.string.license_agreement_url)
        webView.loadUrl(url)

        setupWebViewTheme()
    }

    private fun setupWebViewTheme() {
        val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
                webView.settings.forceDark = WebSettings.FORCE_DARK_ON
            }
        }
    }
}