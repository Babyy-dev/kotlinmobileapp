package com.kappa.app.main

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
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
import com.kappa.app.core.storage.PreferencesManager
import com.kappa.app.user.presentation.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class OnboardingProfileFragment : Fragment() {

    private val userViewModel: UserViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var selectedAvatarBytes: ByteArray? = null
    private var selectedAvatarName: String = "avatar.png"
    private var selectedAvatarMime: String = "image/png"
    private var avatarNameText: TextView? = null
    private var pendingComplete = false
    private var hasNavigated = false

    private val avatarPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            return@registerForActivityResult
        }
        val context = context ?: return@registerForActivityResult
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            if (bytes != null) {
                selectedAvatarBytes = bytes
                selectedAvatarName = resolveDisplayName(uri) ?: "avatar.png"
                selectedAvatarMime = context.contentResolver.getType(uri) ?: "image/png"
                withContext(Dispatchers.Main) {
                    avatarNameText?.text = selectedAvatarName
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_onboarding_profile, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val displayNameInput = view.findViewById<TextInputEditText>(R.id.input_onboarding_display_name)
        val countryInput = view.findViewById<TextInputEditText>(R.id.input_onboarding_country)
        val languageInput = view.findViewById<TextInputEditText>(R.id.input_onboarding_language)
        val avatarButton = view.findViewById<MaterialButton>(R.id.button_onboarding_avatar)
        val continueButton = view.findViewById<MaterialButton>(R.id.button_onboarding_profile_continue)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_onboarding_profile)
        val messageText = view.findViewById<TextView>(R.id.text_onboarding_profile_message)
        avatarNameText = view.findViewById(R.id.text_onboarding_avatar_name)

        val initialCountry = findNavController()
            .previousBackStackEntry
            ?.savedStateHandle
            ?.get<String>("selected_country")
            .orEmpty()
        if (initialCountry.isNotBlank()) {
            countryInput.setText(initialCountry)
        }

        userViewModel.loadUser("me")

        avatarButton.setOnClickListener {
            avatarPicker.launch("image/*")
        }

        continueButton.setOnClickListener {
            val displayName = displayNameInput.text?.toString()?.trim()
            val country = countryInput.text?.toString()?.trim()
            val language = languageInput.text?.toString()?.trim()

            val shouldUpdate = !displayName.isNullOrBlank() ||
                !country.isNullOrBlank() ||
                !language.isNullOrBlank()

            pendingComplete = true

            if (shouldUpdate) {
                userViewModel.updateProfile(
                    nickname = displayName?.ifBlank { null },
                    country = country?.ifBlank { null },
                    language = language?.ifBlank { null }
                )
            }

            val avatarBytes = selectedAvatarBytes
            if (avatarBytes != null) {
                userViewModel.uploadAvatar(avatarBytes, selectedAvatarName, selectedAvatarMime)
            }

            if (!shouldUpdate && avatarBytes == null) {
                completeOnboarding()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                userViewModel.viewState.collect { state ->
                    progressBar.visibility = if (state.isSaving) View.VISIBLE else View.GONE
                    continueButton.isEnabled = !state.isSaving
                    avatarButton.isEnabled = !state.isSaving
                    if (state.error != null) {
                        messageText.text = state.error
                        messageText.visibility = View.VISIBLE
                    } else if (state.message != null) {
                        messageText.text = state.message
                        messageText.visibility = View.VISIBLE
                    } else {
                        messageText.visibility = View.GONE
                    }

                    if (pendingComplete && !state.isSaving && state.error == null) {
                        completeOnboarding()
                    }
                }
            }
        }
    }

    private fun completeOnboarding() {
        if (hasNavigated) {
            return
        }
        hasNavigated = true
        viewLifecycleOwner.lifecycleScope.launch {
            val userId = preferencesManager.getUserIdOnce()
            if (!userId.isNullOrBlank()) {
                preferencesManager.setOnboardingComplete(userId)
            }
            findNavController().navigate(
                R.id.navigation_inbox,
                null,
                navOptions {
                    popUpTo(R.id.navigation_onboarding_country) { inclusive = true }
                }
            )
        }
    }

    private fun resolveDisplayName(uri: Uri): String? {
        val resolver = context?.contentResolver ?: return null
        val cursor = resolver.query(uri, null, null, null, null) ?: return null
        cursor.use {
            if (it.moveToFirst()) {
                val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) {
                    return it.getString(index)
                }
            }
        }
        return null
    }
}
