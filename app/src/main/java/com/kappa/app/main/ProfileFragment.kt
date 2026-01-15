package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.button.MaterialButton
import com.kappa.app.R
import com.kappa.app.auth.presentation.AuthViewModel
import com.kappa.app.domain.user.toDisplayName
import com.kappa.app.user.presentation.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameText = view.findViewById<TextView>(R.id.text_profile_username)
        val emailText = view.findViewById<TextView>(R.id.text_profile_email)
        val roleText = view.findViewById<TextView>(R.id.text_profile_role)
        val logoutButton = view.findViewById<MaterialButton>(R.id.button_logout)

        userViewModel.loadUser("me")

        logoutButton.setOnClickListener {
            authViewModel.logout()
            findNavController().navigate(
                R.id.navigation_login,
                null,
                navOptions {
                    popUpTo(R.id.navigation_home) { inclusive = true }
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.viewState.collect { state ->
                    val user = state.user
                    usernameText.text = "Username: ${user?.username ?: "-"}"
                    emailText.text = "Email: ${user?.email ?: "-"}"
                    roleText.text = "Role: ${user?.role?.toDisplayName() ?: "-"}"
                }
            }
        }
    }
}
