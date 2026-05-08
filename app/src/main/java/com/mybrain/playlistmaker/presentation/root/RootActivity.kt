package com.mybrain.playlistmaker.presentation.root

import android.content.res.Configuration
import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.compose.rememberNavController
import com.mybrain.playlistmaker.presentation.navigation.AppNavHost
import com.mybrain.playlistmaker.presentation.navigation.NavigationEvents
import org.koin.android.ext.android.getKoin

class RootActivity : AppCompatActivity() {

    private val snackbarHidesBottomBar = mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        applyEdgeToEdgeSystemBarsForCurrentTheme()

        super.onCreate(savedInstanceState)

        val navigationEvents = getKoin().get<NavigationEvents>()
        setContent {
            val appBlue = Color(0xFF3772E7)
            val colorScheme =
                if (isSystemInDarkTheme()) {
                    darkColorScheme(primary = appBlue)
                } else {
                    lightColorScheme(primary = appBlue)
                }
            MaterialTheme(colorScheme = colorScheme) {
                Surface(modifier = Modifier.fillMaxSize()) {
                    val navController = rememberNavController()
                    AppNavHost(
                        navController = navController,
                        snackbarHidesBottomBar = snackbarHidesBottomBar,
                        navigationEvents = navigationEvents,
                        modifier = Modifier.fillMaxSize(),
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        applyEdgeToEdgeSystemBarsForCurrentTheme()
    }

    private fun applyEdgeToEdgeSystemBarsForCurrentTheme() {
        val night =
            (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
        val scrimDark = AndroidColor.parseColor("#FF1A1B22")
        val scrimLight = AndroidColor.parseColor("#FFFFFFFF")
        enableEdgeToEdge(
            statusBarStyle =
                if (night) {
                    SystemBarStyle.dark(scrimDark)
                } else {
                    SystemBarStyle.light(scrimLight, scrimDark)
                },
            navigationBarStyle =
                if (night) {
                    SystemBarStyle.dark(scrimDark)
                } else {
                    SystemBarStyle.light(scrimLight, scrimDark)
                },
        )
    }

    fun setBottomNavVisible(isVisible: Boolean) {
        snackbarHidesBottomBar.value = !isVisible
    }
}
