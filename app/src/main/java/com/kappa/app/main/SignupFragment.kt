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
        val toggleButton = view.findViewById<MaterialButton>(R.id.button_toggle_signup_method)
        val passwordLayout = view.findViewById<View>(R.id.layout_signup_password)
        val phoneLayout = view.findViewById<View>(R.id.layout_signup_phone)
        val phoneInput = view.findViewById<TextInputEditText>(R.id.input_signup_phone)
        val otpInput = view.findViewById<TextInputEditText>(R.id.input_signup_otp)
        val sendOtpButton = view.findViewById<MaterialButton>(R.id.button_signup_send_otp)
        val verifyOtpButton = view.findViewById<MaterialButton>(R.id.button_signup_verify_otp)
        val loginButton = view.findViewById<MaterialButton>(R.id.button_go_login)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_signup)
        val errorText = view.findViewById<TextView>(R.id.text_signup_error)
        val messageText = view.findViewById<TextView>(R.id.text_signup_message)

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

        toggleButton.setOnClickListener {
            val showingPhone = phoneLayout.visibility == View.VISIBLE
            phoneLayout.visibility = if (showingPhone) View.GONE else View.VISIBLE
            passwordLayout.visibility = if (showingPhone) View.VISIBLE else View.GONE
            toggleButton.text = if (showingPhone) "Use phone instead" else "Use password instead"
            errorText.visibility = View.GONE
            messageText.visibility = View.GONE
            authViewModel.clearError()
        }

        sendOtpButton.setOnClickListener {
            val phone = phoneInput.text?.toString()?.trim().orEmpty()
            if (phone.isNotEmpty()) {
                authViewModel.requestOtp(phone)
            } else {
                errorText.text = "Please enter phone number"
                errorText.visibility = View.VISIBLE
            }
        }

        verifyOtpButton.setOnClickListener {
            val phone = phoneInput.text?.toString()?.trim().orEmpty()
            val code = otpInput.text?.toString()?.trim().orEmpty()
            if (phone.isNotEmpty() && code.isNotEmpty()) {
                authViewModel.verifyOtp(phone, code)
            } else {
                errorText.text = "Please enter phone and OTP"
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
                    sendOtpButton.isEnabled = !state.isLoading
                    verifyOtpButton.isEnabled = !state.isLoading
                    loginButton.isEnabled = !state.isLoading
                    toggleButton.isEnabled = !state.isLoading

                    if (state.error != null) {
                        errorText.text = state.error
                        errorText.visibility = View.VISIBLE
                    } else {
                        errorText.visibility = View.GONE
                    }

                    if (state.message != null) {
                        messageText.text = state.message
                        messageText.visibility = View.VISIBLE
                    } else {
                        messageText.visibility = View.GONE
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
