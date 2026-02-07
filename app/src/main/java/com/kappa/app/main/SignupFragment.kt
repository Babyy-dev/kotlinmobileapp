package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.util.Patterns
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.kappa.app.R
import com.kappa.app.auth.presentation.AuthViewModel
import com.kappa.app.core.storage.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.NumberParseException
import androidx.core.widget.addTextChangedListener
import java.util.Locale
import javax.inject.Inject

/**
 * Signup screen fragment.
 */
@AndroidEntryPoint
class SignupFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var hasNavigated = false

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
        val countryCodeInput = view.findViewById<MaterialAutoCompleteTextView>(R.id.input_signup_country_code)
        val phoneInput = view.findViewById<TextInputEditText>(R.id.input_signup_phone)
        val otpLayout = view.findViewById<View>(R.id.layout_signup_otp)
        val otpInput = view.findViewById<TextInputEditText>(R.id.input_signup_otp)
        val sendOtpButton = view.findViewById<MaterialButton>(R.id.button_signup_send_otp)
        val verifyOtpButton = view.findViewById<MaterialButton>(R.id.button_signup_verify_otp)
        val loginButton = view.findViewById<MaterialButton>(R.id.button_go_login)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_signup)
        val errorText = view.findViewById<TextView>(R.id.text_signup_error)
        val messageText = view.findViewById<TextView>(R.id.text_signup_message)

        val phoneUtil = PhoneNumberUtil.getInstance()
        val codeOptions = buildCountryCodes(phoneUtil)
        val adapter = android.widget.ArrayAdapter(
            requireContext(),
            R.layout.item_country,
            codeOptions.map { it.displayName }
        )
        countryCodeInput.setAdapter(adapter)
        val defaultCode = Locale.getDefault().country
        val defaultOption = codeOptions.firstOrNull { it.code == defaultCode } ?: codeOptions.first()
        countryCodeInput.setText(defaultOption.displayName, false)
        countryCodeInput.setOnClickListener { countryCodeInput.showDropDown() }
        var isUpdatingCode = false
        countryCodeInput.addTextChangedListener { editable ->
            if (isUpdatingCode) return@addTextChangedListener
            val option = resolveCountryOption(editable?.toString().orEmpty(), codeOptions, phoneUtil)
            val display = option.displayName
            if (display.isNotEmpty() && editable?.toString() != display) {
                isUpdatingCode = true
                countryCodeInput.setText(display, false)
                isUpdatingCode = false
            }
        }

        signupButton.setOnClickListener {
            val username = usernameInput.text?.toString()?.trim().orEmpty()
            val email = emailInput.text?.toString()?.trim().orEmpty()
            val password = passwordInput.text?.toString()?.trim().orEmpty()
            val phone = phoneInput.text?.toString()?.trim().orEmpty()
            val option = resolveCountryOption(countryCodeInput.text?.toString().orEmpty(), codeOptions, phoneUtil)
            val e164 = validatePhone(phoneUtil, option, phone)

            if (username.isBlank()) {
                errorText.text = "Username is required"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (email.isBlank() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                errorText.text = "Enter a valid email address"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (password.length < 6) {
                errorText.text = "Password must be at least 6 characters"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }
            if (e164 == null) {
                errorText.text = "Enter a valid phone number"
                errorText.visibility = View.VISIBLE
                return@setOnClickListener
            }

            authViewModel.signup(username, email, password, e164)
        }

        sendOtpButton.setOnClickListener {
            val phone = phoneInput.text?.toString()?.trim().orEmpty()
            val option = resolveCountryOption(countryCodeInput.text?.toString().orEmpty(), codeOptions, phoneUtil)
            val e164 = validatePhone(phoneUtil, option, phone)
            if (e164 != null) {
                authViewModel.requestOtp(e164)
            } else {
                errorText.text = "Enter a valid phone number"
                errorText.visibility = View.VISIBLE
            }
        }

        verifyOtpButton.setOnClickListener {
            val phone = phoneInput.text?.toString()?.trim().orEmpty()
            val code = otpInput.text?.toString()?.trim().orEmpty()
            val option = resolveCountryOption(countryCodeInput.text?.toString().orEmpty(), codeOptions, phoneUtil)
            val e164 = validatePhone(phoneUtil, option, phone)
            if (e164 != null && code.isNotEmpty()) {
                authViewModel.verifyOtp(e164, code)
            } else {
                errorText.text = "Enter a valid phone and OTP"
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
                    signupButton.isEnabled = !state.isLoading && !state.otpRequired
                    sendOtpButton.isEnabled = !state.isLoading
                    verifyOtpButton.isEnabled = !state.isLoading
                    loginButton.isEnabled = !state.isLoading
                    otpLayout.visibility = if (state.otpRequired) View.VISIBLE else View.GONE

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
                            val userId = preferencesManager.getUserIdOnce()
                            val destination = if (!userId.isNullOrBlank() &&
                                preferencesManager.isOnboardingComplete(userId)
                            ) {
                                R.id.navigation_inbox
                            } else {
                                R.id.navigation_onboarding_country
                            }
                            findNavController().navigate(
                                destination,
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

    private fun buildCountryCodes(phoneUtil: PhoneNumberUtil): List<CountryCodeOption> {
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

    private fun resolveCountryOption(
        input: String,
        options: List<CountryCodeOption>,
        phoneUtil: PhoneNumberUtil
    ): CountryCodeOption {
        val normalized = input.replace(Regex("[^+0-9]"), "")
        if (normalized.isNotBlank()) {
            options.firstOrNull { it.dialCode == normalized }?.let { return it }
            val codeDigits = normalized.replace("+", "").toIntOrNull()
            if (codeDigits != null) {
                val region = phoneUtil.getRegionCodeForCountryCode(codeDigits)
                options.firstOrNull { it.code == region }?.let { return it }
            }
        }
        return options.first()
    }

    private fun validatePhone(
        phoneUtil: PhoneNumberUtil,
        option: CountryCodeOption,
        rawPhone: String
    ): String? {
        if (rawPhone.isBlank()) return null
        return try {
            val parsed = phoneUtil.parse(rawPhone, option.code)
            val matchesRegion = phoneUtil.getRegionCodeForNumber(parsed) == option.code
            if (!phoneUtil.isValidNumber(parsed) || !matchesRegion) {
                null
            } else {
                phoneUtil.format(parsed, PhoneNumberUtil.PhoneNumberFormat.E164)
            }
        } catch (_: NumberParseException) {
            null
        }
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
