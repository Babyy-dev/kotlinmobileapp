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
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.kappa.app.R
import com.kappa.app.auth.presentation.AuthViewModel
import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.core.storage.PreferencesManager
import com.kappa.app.domain.user.Role
import com.google.i18n.phonenumbers.PhoneNumberUtil
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Locale
import javax.inject.Inject

/**
 * Login screen fragment.
 */
@AndroidEntryPoint
class LoginFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager
    @Inject
    lateinit var authRepository: AuthRepository

    private var hasNavigated = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val countryCodeInput = view.findViewById<MaterialAutoCompleteTextView>(R.id.input_country_code)
        val usernameInput = view.findViewById<TextInputEditText>(R.id.input_username)
        val passwordInput = view.findViewById<TextInputEditText>(R.id.input_password)
        val loginButton = view.findViewById<MaterialButton>(R.id.button_login)
        val signupButton = view.findViewById<MaterialButton>(R.id.button_go_signup)
        val toggleButton = view.findViewById<MaterialButton>(R.id.button_toggle_login_method)
        val passwordLayout = view.findViewById<View>(R.id.layout_login_password)
        val phoneLayout = view.findViewById<View>(R.id.layout_login_phone)
        val phoneInput = view.findViewById<TextInputEditText>(R.id.input_phone)
        val otpInput = view.findViewById<TextInputEditText>(R.id.input_otp)
        val sendOtpButton = view.findViewById<MaterialButton>(R.id.button_send_otp)
        val verifyOtpButton = view.findViewById<MaterialButton>(R.id.button_verify_otp)
        val guestLoginButton = view.findViewById<MaterialButton>(R.id.button_guest_login)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_login)
        val errorText = view.findViewById<TextView>(R.id.text_login_error)
        val messageText = view.findViewById<TextView>(R.id.text_login_message)

        val codeOptions = buildCountryCodes()
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            R.layout.item_country,
            codeOptions.map { it.displayName }
        )
        countryCodeInput.setAdapter(adapter)
        countryCodeInput.setText(codeOptions.firstOrNull()?.displayName.orEmpty(), false)
        countryCodeInput.setOnClickListener { countryCodeInput.showDropDown() }

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

        toggleButton.setOnClickListener {
            val showingPhone = phoneLayout.visibility == View.VISIBLE
            phoneLayout.visibility = if (showingPhone) View.GONE else View.VISIBLE
            passwordLayout.visibility = if (showingPhone) View.VISIBLE else View.GONE
            toggleButton.text = if (showingPhone) "Use phone no. instead" else "Use password instead"
            errorText.visibility = View.GONE
            messageText.visibility = View.GONE
            authViewModel.clearError()
        }

        signupButton.setOnClickListener {
            findNavController().navigate(R.id.navigation_signup)
        }

        sendOtpButton.setOnClickListener {
            val codeText = countryCodeInput.text?.toString()?.trim().orEmpty()
            val phone = phoneInput.text?.toString()?.trim().orEmpty()
            val dialCode = codeOptions.firstOrNull { it.displayName == codeText }?.dialCode ?: "+1"
            val e164 = when {
                phone.isBlank() -> ""
                phone.startsWith("+") -> phone
                phone.any { it.isLetter() } -> phone
                else -> "$dialCode$phone"
            }
            if (e164.isNotEmpty()) {
                authViewModel.requestOtp(e164)
            } else {
                errorText.text = "Please enter phone number"
                errorText.visibility = View.VISIBLE
            }
        }

        verifyOtpButton.setOnClickListener {
            val codeText = countryCodeInput.text?.toString()?.trim().orEmpty()
            val phone = phoneInput.text?.toString()?.trim().orEmpty()
            val code = otpInput.text?.toString()?.trim().orEmpty()
            val dialCode = codeOptions.firstOrNull { it.displayName == codeText }?.dialCode ?: "+1"
            val e164 = when {
                phone.isBlank() -> ""
                phone.startsWith("+") -> phone
                phone.any { it.isLetter() } -> phone
                else -> "$dialCode$phone"
            }
            if (e164.isNotEmpty() && code.isNotEmpty()) {
                authViewModel.verifyOtp(e164, code)
            } else {
                errorText.text = "Please enter phone and OTP"
                errorText.visibility = View.VISIBLE
            }
        }

        guestLoginButton.setOnClickListener {
            authViewModel.guestLogin()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                authViewModel.viewState.collect { state ->
                    progressBar.visibility = if (state.isLoading) View.VISIBLE else View.GONE
                    loginButton.isEnabled = !state.isLoading
                    signupButton.isEnabled = !state.isLoading
                    sendOtpButton.isEnabled = !state.isLoading
                    verifyOtpButton.isEnabled = !state.isLoading
                    guestLoginButton.isEnabled = !state.isLoading
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
                        if (!hasNavigated) {
                            hasNavigated = true
                            val currentUser = authRepository.getCurrentUser()
                            val destination = when (currentUser?.role) {
                                Role.ADMIN -> R.id.navigation_admin_dashboard
                                Role.AGENCY -> R.id.navigation_agency_tools
                                Role.RESELLER -> R.id.navigation_reseller_tools
                                else -> {
                                    val userId = preferencesManager.getUserIdOnce()
                                    if (!userId.isNullOrBlank() &&
                                        preferencesManager.isOnboardingComplete(userId)
                                    ) {
                                        R.id.navigation_inbox
                                    } else {
                                        R.id.navigation_onboarding_country
                                    }
                                }
                            }
                            findNavController().navigate(
                                destination,
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

    private fun buildCountryCodes(): List<CountryCodeOption> {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val options = mutableListOf<CountryCodeOption>()
        Locale.getISOCountries().forEach { iso ->
            val code = phoneUtil.getCountryCodeForRegion(iso)
            if (code > 0) {
                val dial = "+$code"
                val display = "${flagEmoji(iso)} $dial"
                options.add(CountryCodeOption(iso, dial, display))
            }
        }
        return options.sortedBy { it.displayName }
    }

    private fun flagEmoji(countryCode: String): String {
        val code = countryCode.uppercase(Locale.ENGLISH)
        if (code.length != 2) return ""
        val first = Character.codePointAt(code, 0) - 0x41 + 0x1F1E6
        val second = Character.codePointAt(code, 1) - 0x41 + 0x1F1E6
        return String(Character.toChars(first)) + String(Character.toChars(second))
    }

    private data class CountryCodeOption(
        val code: String,
        val dialCode: String,
        val displayName: String
    )
}
