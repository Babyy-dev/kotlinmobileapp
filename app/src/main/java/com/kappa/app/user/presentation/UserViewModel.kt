package com.kappa.app.user.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.core.base.ViewState
import com.kappa.app.domain.user.User
import com.kappa.app.user.domain.usecase.GetUserUseCase
import com.kappa.app.user.domain.usecase.UpdateUserUseCase
import com.kappa.app.user.domain.usecase.UploadAvatarUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * User ViewState.
 */
data class UserViewState(
    val user: User? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null,
    val message: String? = null
) : ViewState

/**
 * User ViewModel.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase,
    private val updateUserUseCase: UpdateUserUseCase,
    private val uploadAvatarUseCase: UploadAvatarUseCase
) : ViewModel() {
    
    private val _viewState = MutableStateFlow(UserViewState())
    val viewState: StateFlow<UserViewState> = _viewState.asStateFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null, message = null)
            
            getUserUseCase(userId)
                .onSuccess { user ->
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        user = user
                    )
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun updateProfile(nickname: String? = null, avatarUrl: String? = null, country: String? = null, language: String? = null) {
        val current = _viewState.value.user ?: return
        val updatedUser = current.copy(
            nickname = nickname ?: current.nickname,
            avatarUrl = avatarUrl ?: current.avatarUrl,
            country = country ?: current.country,
            language = language ?: current.language
        )
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isSaving = true, error = null, message = null)
            updateUserUseCase(updatedUser)
                .onSuccess { user ->
                    _viewState.value = _viewState.value.copy(
                        isSaving = false,
                        user = user,
                        message = "Profile updated"
                    )
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isSaving = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isSaving = true, error = null, message = null)
            uploadAvatarUseCase(bytes, fileName, mimeType)
                .onSuccess { user ->
                    _viewState.value = _viewState.value.copy(
                        isSaving = false,
                        user = user,
                        message = "Avatar uploaded"
                    )
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isSaving = false,
                        error = throwable.message
                    )
                }
        }
    }
}
