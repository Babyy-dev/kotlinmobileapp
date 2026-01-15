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
 * Login screen fragment.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val usernameInput = view.findViewById<TextInputEditText>(R.id.input_username)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.input_password)
        val loginButton = view.findViewById<MaterialButton>(R.id.button_login)
        val signupButton = view.findViewById<MaterialButton>(R.id.button_go_signup)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_login)
        val errorText = view.findViewById<TextView>(R.id.text_login_error)

        loginButton.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString()?.trim().orEmpty()
            if (username.isNotEmpty() && password.isNotEmpty()) {
                authViewModel.login(username, password)
            } else {
                errorText.text = "Please enter username and password"
                errorText.visibility = View.VISIBLE
            }
        }

        signupButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_signup)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.viewState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    loginButton.isEnabled = !state.isLoading
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
                                popUpTo(R.id.navigation_login) { inclusive = true }
                            }
                        )
                    }
                }
            }
        }
    }
}
