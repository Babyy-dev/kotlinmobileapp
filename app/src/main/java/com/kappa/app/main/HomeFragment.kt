package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.kappa.app.R
import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.core.config.AppConfig
import com.kappa.app.core.network.NetworkMonitor
import com.kappa.app.domain.user.toDisplayName
import com.kappa.app.economy.presentation.EconomyViewModel
import com.kappa.app.user.presentation.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Home screen fragment with technical dashboard.
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val userViewModel: UserViewModel by viewModels()
    private val economyViewModel: EconomyViewModel by viewModels()

    private var loadedUserId: String? = null
    private var hasLoggedOut = false
    private var forceReloadCoins = false
    private var shouldRefreshOnReconnect = false
    private var isUserLoading = false
    private var isEconomyLoading = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Display app configuration
        displayAppInfo()

        val progressBar = view.findViewById<android.widget.ProgressBar>(R.id.progress_home)
        val errorText = view.findViewById<android.widget.TextView>(R.id.text_home_error)
        val refreshButton = view.findViewById<android.view.View>(R.id.button_refresh_home)

        refreshButton.setOnClickListener {
            refreshHomeData()
        }

        // Load current user and balance from backend
        userViewModel.loadUser("me")

        // Observe view states
        observeUserState(progressBar, errorText)
        observeEconomyState(progressBar, errorText)
        observeNetworkState()

        view.findViewById<android.view.View>(R.id.button_open_rooms).setOnClickListener {
            findNavController().navigate(R.id.navigation_rooms)
        }
    }

    private fun displayAppInfo() {
        view?.findViewById<android.widget.TextView>(R.id.text_app_version)?.text =
            "Version: ${AppConfig.appVersion}"
        view?.findViewById<android.widget.TextView>(R.id.text_build_type)?.text =
            "Build: ${AppConfig.buildType}"
        view?.findViewById<android.widget.TextView>(R.id.text_environment)?.text =
            "Environment: ${AppConfig.currentEnvironment.name}"
    }

    private fun observeUserState(
        progressBar: android.widget.ProgressBar,
        errorText: android.widget.TextView
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.viewState.collect { state ->
                    // Update UI with user data
                    val user = state.user
                    updateUserDisplay(user?.username ?: "-", user?.role?.toDisplayName() ?: "Unknown")
                    if (user != null && (loadedUserId != user.id || forceReloadCoins)) {
                        loadedUserId = user.id
                        economyViewModel.loadCoinBalance(user.id)
                        forceReloadCoins = false
                    }
                    val error = state.error
                    isUserLoading = state.isLoading
                    updateLoading(progressBar)
                    if (!hasLoggedOut && !error.isNullOrBlank() && error.contains("Session expired")) {
                        hasLoggedOut = true
                        authRepository.logout()
                        findNavController().navigate(
                            R.id.navigation_login,
                            null,
                            navOptions {
                                popUpTo(R.id.navigation_home) { inclusive = true }
                            }
                        )
                    }

                    updateErrorState(error, errorText)
                }
            }
        }
    }

    private fun observeEconomyState(
        progressBar: android.widget.ProgressBar,
        errorText: android.widget.TextView
    ) {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                economyViewModel.viewState.collect { state ->
                    // Update UI with coin balance
                    updateCoinBalanceDisplay(state.coinBalance?.balance ?: 0L)
                    isEconomyLoading = state.isLoading
                    updateLoading(progressBar)
                    updateErrorState(state.error, errorText)
                }
            }
        }
    }

    private fun observeNetworkState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline.collect { isOnline ->
                    if (isOnline && shouldRefreshOnReconnect) {
                        shouldRefreshOnReconnect = false
                        refreshHomeData()
                    }
                }
            }
        }
    }

    private fun refreshHomeData() {
        forceReloadCoins = true
        userViewModel.loadUser("me")
        loadedUserId?.let { economyViewModel.loadCoinBalance(it) }
    }

    private fun updateLoading(progressBar: android.widget.ProgressBar) {
        progressBar.visibility = if (isUserLoading || isEconomyLoading) View.VISIBLE else View.GONE
    }

    private fun updateErrorState(
        error: String?,
        errorText: android.widget.TextView
    ) {
        if (error.isNullOrBlank()) {
            if (!shouldRefreshOnReconnect) {
                errorText.visibility = View.GONE
            }
            return
        }
        errorText.text = error
        errorText.visibility = View.VISIBLE
        if (error.contains("No internet", ignoreCase = true) ||
            error.contains("timeout", ignoreCase = true)
        ) {
            shouldRefreshOnReconnect = true
        }
    }

    private fun updateUserDisplay(username: String, role: String) {
        view?.findViewById<android.widget.TextView>(R.id.text_user_name)?.text = "User: $username"
        view?.findViewById<android.widget.TextView>(R.id.text_user_role)?.text = "Role: $role"
    }

    private fun updateCoinBalanceDisplay(balance: Long) {
        view?.findViewById<android.widget.TextView>(R.id.text_coin_balance)?.text = "Coins: $balance"
    }
}
