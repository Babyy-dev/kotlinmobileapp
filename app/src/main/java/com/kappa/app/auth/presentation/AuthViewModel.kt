package com.kappa.app.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.auth.domain.usecase.LoginUseCase
import com.kappa.app.auth.domain.usecase.SignupUseCase
import com.kappa.app.core.base.ViewState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Authentication ViewState.
 */
data class AuthViewState(
    val isLoading: Boolean = false,
    val isLoggedIn: Boolean = false,
    val error: String? = null
) : ViewState

/**
 * Authentication ViewModel.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _viewState = MutableStateFlow(AuthViewState())
    val viewState: StateFlow<AuthViewState> = _viewState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            
            loginUseCase(username, password)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
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

    fun signup(username: String, email: String, password: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            signupUseCase(username, email, password)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        isLoggedIn = true
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
    
    fun clearError() {
        _viewState.value = _viewState.value.copy(error = null)
    }

    fun logout() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            authRepository.logout()
            _viewState.value = _viewState.value.copy(isLoading = false, isLoggedIn = false)
        }
    }
}
