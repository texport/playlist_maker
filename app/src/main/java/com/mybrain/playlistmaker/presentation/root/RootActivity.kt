package com.mybrain.playlistmaker.presentation.root

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.mybrain.playlistmaker.R

class RootActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_root)

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.rootFragmentContainerView) as NavHostFragment
        val navController = navHostFragment.navController

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigationView)
        val bottomNavBorder = findViewById<View>(R.id.bottom_nav_border)
        bottomNavigationView.setupWithNavController(navController)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            val isTopLevel = bottomNavigationView.menu.findItem(destination.id) != null
            if (isTopLevel) {
                bottomNavigationView.visibility = View.VISIBLE
                bottomNavBorder.visibility = View.VISIBLE
            } else {
                bottomNavigationView.visibility = View.GONE
                bottomNavBorder.visibility = View.GONE
            }
        }

    }
}
