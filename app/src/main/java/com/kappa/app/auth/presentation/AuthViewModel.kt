package com.kappa.app.auth.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.auth.domain.usecase.GuestLoginUseCase
import com.kappa.app.auth.domain.usecase.LoginUseCase
import com.kappa.app.auth.domain.usecase.RequestOtpUseCase
import com.kappa.app.auth.domain.usecase.SignupUseCase
import com.kappa.app.auth.domain.usecase.VerifyOtpUseCase
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
    val error: String? = null,
    val message: String? = null
) : ViewState

/**
 * Authentication ViewModel.
 */
@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val signupUseCase: SignupUseCase,
    private val requestOtpUseCase: RequestOtpUseCase,
    private val verifyOtpUseCase: VerifyOtpUseCase,
    private val guestLoginUseCase: GuestLoginUseCase,
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _viewState = MutableStateFlow(AuthViewState())
    val viewState: StateFlow<AuthViewState> = _viewState.asStateFlow()
    
    fun login(username: String, password: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null, message = null)
            
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
            _viewState.value = _viewState.value.copy(isLoading = true, error = null, message = null)
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

    fun requestOtp(phone: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null, message = null)
            requestOtpUseCase(phone)
                .onSuccess { info ->
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        message = "OTP: ${info.code} (dev only)"
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

    fun verifyOtp(phone: String, code: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null, message = null)
            verifyOtpUseCase(phone, code)
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

    fun guestLogin() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null, message = null)
            guestLoginUseCase()
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
        _viewState.value = _viewState.value.copy(error = null, message = null)
    }

    fun logout() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            authRepository.logout()
            _viewState.value = _viewState.value.copy(isLoading = false, isLoggedIn = false)
        }
    }
}
