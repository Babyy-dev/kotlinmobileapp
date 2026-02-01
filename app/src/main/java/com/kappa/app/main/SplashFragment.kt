package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.kappa.app.R
import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.core.storage.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Splash screen fragment.
 */
@AndroidEntryPoint
class SplashFragment : Fragment() {

    @Inject
    lateinit var authRepository: AuthRepository

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_splash, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            val isLoggedIn = authRepository.isLoggedIn()
            val destination = if (isLoggedIn) {
                val userId = preferencesManager.getUserIdOnce()
                if (!userId.isNullOrBlank() && preferencesManager.isOnboardingComplete(userId)) {
                    R.id.navigation_inbox
                } else {
                    R.id.navigation_onboarding_country
                }
            } else {
                R.id.navigation_login
            }
            findNavController().navigate(destination)
        }
    }
}
