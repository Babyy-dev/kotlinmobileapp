package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.kappa.app.R
import com.kappa.app.auth.presentation.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Signup screen fragment.
 */
@AndroidEntryPoint
class SignupFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_signup, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.input_signup_username)
        val emailInput = view.findViewById<TextInputEditText>(R.id.input_signup_email)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.input_signup_password)
        val signupButton = view.findViewById<MaterialButton>(R.id.button_signup)
        val loginButton = view.findViewById<MaterialButton>(R.id.button_go_login)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_signup)
        val errorText = view.findViewById<TextView>(R.id.text_signup_error)

        signupButton.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim().orEmpty()
            val email = emailInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString()?.trim().orEmpty()

            if (username.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.signup(username, email, password)
            } else {
                errorText.text = "Please enter username, email, and password"
                errorText.visibility = View.VISIBLE
            }
        }

        loginButton.setOnClickListener {
            findNavController().navigate(
                R.id.navigation_login,
                null,
                navOptions {
                    popUpTo(R.id.navigation_signup) { inclusive = true }
                }
            )
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.viewState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    signupButton.isEnabled = !state.isLoading

                    if (state.error != null) {
                        errorText.text = state.error
                        errorText.visibility = View.VISIBLE
                    } else {
                        errorText.visibility = View.GONE
                    }

                    if (state.isLoggedIn) {
                        findNavController().navigate(
                            R.id.navigation_home,
                            null,
                            navOptions {
                                popUpTo(R.id.navigation_signup) { inclusive = true }
                            }
                        )
                    }
                }
            }
        }
    }
}
