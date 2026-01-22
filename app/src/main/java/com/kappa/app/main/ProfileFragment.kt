package com.kappa.app.main

import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
import com.kappa.app.auth.presentation.AuthViewModel
import com.kappa.app.domain.user.Role
import com.kappa.app.domain.user.toDisplayName
import com.kappa.app.user.presentation.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private val authViewModel: AuthViewModel by viewModels()
    private val userViewModel: UserViewModel by viewModels()
    private var selectedAvatarBytes: ByteArray? = null
    private var selectedAvatarName: String = "avatar.png"
    private var selectedAvatarMime: String = "image/png"
    private var avatarNameText: TextView? = null

    private val avatarPicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri == null) {
            return@registerForActivityResult
        }
        val context = context ?: return@registerForActivityResult
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
        if (bytes != null) {
            selectedAvatarBytes = bytes
            selectedAvatarName = resolveDisplayName(uri) ?: "avatar.png"
            selectedAvatarMime = context.contentResolver.getType(uri) ?: "image/png"
            avatarNameText?.text = selectedAvatarName
        }
    }

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
        val phoneText = view.findViewById<TextView>(R.id.text_profile_phone)
        val nicknameText = view.findViewById<TextView>(R.id.text_profile_nickname)
        val countryText = view.findViewById<TextView>(R.id.text_profile_country)
        val languageText = view.findViewById<TextView>(R.id.text_profile_language)
        val guestText = view.findViewById<TextView>(R.id.text_profile_guest)
        val nicknameInput = view.findViewById<TextInputEditText>(R.id.input_profile_nickname)
        val countryInput = view.findViewById<TextInputEditText>(R.id.input_profile_country)
        val languageInput = view.findViewById<TextInputEditText>(R.id.input_profile_language)
        val avatarButton = view.findViewById<MaterialButton>(R.id.button_profile_avatar)
        val saveButton = view.findViewById<MaterialButton>(R.id.button_profile_save)
        val logoutButton = view.findViewById<MaterialButton>(R.id.button_logout)
        val messageText = view.findViewById<TextView>(R.id.text_profile_message)
        val roleToolsLayout = view.findViewById<View>(R.id.layout_profile_role_tools)
        val roleToolsTitle = view.findViewById<TextView>(R.id.text_profile_role_tools_title)
        avatarNameText = view.findViewById(R.id.text_profile_avatar_name)

        userViewModel.loadUser("me")

        avatarButton.setOnClickListener {
            avatarPicker.launch("image/*")
        }

        saveButton.setOnClickListener {
            val nickname = nicknameInput.text?.toString()?.trim()
            val country = countryInput.text?.toString()?.trim()
            val language = languageInput.text?.toString()?.trim()
            val shouldUpdate = !nickname.isNullOrBlank() || !country.isNullOrBlank() || !language.isNullOrBlank()
            if (shouldUpdate) {
                userViewModel.updateProfile(
                    nickname = nickname?.ifBlank { null },
                    country = country?.ifBlank { null },
                    language = language?.ifBlank { null }
                )
            }
            val avatarBytes = selectedAvatarBytes
            if (avatarBytes != null) {
                userViewModel.uploadAvatar(avatarBytes, selectedAvatarName, selectedAvatarMime)
            }
        }

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
                    phoneText.text = "Phone: ${user?.phone ?: "-"}"
                    nicknameText.text = "Nickname: ${user?.nickname ?: "-"}"
                    countryText.text = "Country: ${user?.country ?: "-"}"
                    languageText.text = "Language: ${user?.language ?: "-"}"
                    guestText.text = "Guest: ${if (user?.isGuest == true) "Yes" else "No"}"
                    if (nicknameInput.text.isNullOrBlank()) {
                        nicknameInput.setText(user?.nickname.orEmpty())
                    }
                    if (countryInput.text.isNullOrBlank()) {
                        countryInput.setText(user?.country.orEmpty())
                    }
                    if (languageInput.text.isNullOrBlank()) {
                        languageInput.setText(user?.language.orEmpty())
                    }
                    val showRoleTools = user?.role != null && user.role != Role.USER
                    roleToolsLayout.visibility = if (showRoleTools) View.VISIBLE else View.GONE
                    if (showRoleTools) {
                        roleToolsTitle.text = "Role tools (${user?.role?.toDisplayName()})"
                    }
                    saveButton.isEnabled = !state.isSaving
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
                }
            }
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
