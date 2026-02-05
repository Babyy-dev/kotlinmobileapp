package com.kappa.app.main

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.kappa.app.R
import dagger.hilt.android.AndroidEntryPoint
import timber.log.Timber

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Setup navigation - FragmentContainerView creates NavHostFragment automatically
        setupNavigation()
    }

    private fun setupNavigation() {
        try {
            // Find the NavHostFragment
            // FragmentContainerView with app:navGraph creates NavHostFragment automatically
            val navHostFragment = supportFragmentManager
                .findFragmentById(R.id.nav_host_fragment_activity_main) as? NavHostFragment

            if (navHostFragment == null) {
                Timber.e("NavHostFragment not found! Retrying after view is laid out...")
                // Retry after view is laid out if FragmentContainerView hasn't created it yet
                window.decorView.post {
                    setupNavigation()
                }
                return
            }

            // Get NavController from NavHostFragment
            val navController = navHostFragment.navController

            Timber.d("NavController found")
            Timber.d("Graph ID: ${navController.graph.id}")
            Timber.d("Current destination: ${navController.currentDestination?.id}")
            Timber.d("Start destination: ${navController.graph.startDestinationId}")

            // Verify graph is loaded and log destinations
            val graph = navController.graph
            Timber.d("Navigation graph destinations:")
            graph.forEach { destination ->
                Timber.d("  - ${destination.id} (${destination.label})")
            }

            // Find the BottomNavigationView
            val navView: BottomNavigationView = findViewById(R.id.nav_view)

            // Verify menu items match graph destinations
            val menu = navView.menu
            Timber.d("Bottom nav menu items:")
            for (i in 0 until menu.size()) {
                val item = menu.getItem(i)
                val destination = graph.find { it.id == item.itemId }
                if (destination != null) {
                    Timber.d("  - ${item.itemId} matches graph destination: ${destination.label}")
                } else {
                    Timber.e("  - ${item.itemId} does NOT match any graph destination!")
                }
            }

            // Add navigation listener for debugging only
            navController.addOnDestinationChangedListener { _, destination, _ ->
                Timber.d("=== NAVIGATION EVENT ===")
                Timber.d("  Navigated to: ${destination.id} (${destination.label})")
                val hideBottomNav = destination.id in setOf(
                    R.id.navigation_splash,
                    R.id.navigation_login,
                    R.id.navigation_signup,
                    R.id.navigation_onboarding_country,
                    R.id.navigation_onboarding_profile,
                    R.id.navigation_room_detail
                )
                navView.visibility = if (hideBottomNav) android.view.View.GONE else android.view.View.VISIBLE
            }

            // Use setupWithNavController - this handles everything automatically.
            navView.setupWithNavController(navController)
            navView.setOnItemReselectedListener { item ->
                // Reselecting the same tab pops to its root destination.
                navController.popBackStack(item.itemId, false)
            }

            Timber.d("Navigation setup complete!")
            Timber.d("Using NavigationUI.setupWithNavController() - this handles all navigation automatically")
        } catch (e: Exception) {
            Timber.e(e, "Error setting up navigation: ${e.message}")
            e.printStackTrace()
        }
    }
}
