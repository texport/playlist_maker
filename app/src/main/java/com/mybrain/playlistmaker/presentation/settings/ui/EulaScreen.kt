@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.mybrain.playlistmaker.presentation.settings.ui

import android.content.res.Configuration
import android.os.Build
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.mybrain.playlistmaker.R

@Composable
fun EulaScreen(
    onNavigateUp: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val url = stringResource(R.string.license_agreement_url)
    var isLoading by remember { mutableStateOf(true) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(colorResource(R.color.second_background)),
    ) {
        TopAppBar(
            title = {
                Text(
                    text = stringResource(R.string.eula),
                    color = colorResource(R.color.main_text_color),
                )
            },
            navigationIcon = {
                IconButton(onClick = onNavigateUp) {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_back_24),
                        contentDescription = null,
                        tint = colorResource(R.color.main_text_color),
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(
                containerColor = colorResource(R.color.second_background),
                titleContentColor = colorResource(R.color.main_text_color),
                navigationIconContentColor = colorResource(R.color.main_text_color),
            ),
        )
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
        ) {
            AndroidView(
                factory = { context ->
                    WebView(context).apply {
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                isLoading = false
                            }
                        }
                        settings.javaScriptEnabled = true
                        settings.domStorageEnabled = true
                        settings.loadsImagesAutomatically = true
                        loadUrl(url)
                        applyEulaWebViewForceDarkIfNeeded()
                    }
                },
                update = { webView ->
                    if (webView.url != url) {
                        isLoading = true
                        webView.loadUrl(url)
                    }
                },
                modifier = Modifier.fillMaxSize(),
            )

            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(16.dp),
                    color = colorResource(R.color.blue),
                )
            }
        }
    }
}

@Suppress("DEPRECATION")
private fun WebView.applyEulaWebViewForceDarkIfNeeded() {
    val nightModeFlags = resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    if (nightModeFlags == Configuration.UI_MODE_NIGHT_YES) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            settings.forceDark = WebSettings.FORCE_DARK_ON
        }
    }
}
