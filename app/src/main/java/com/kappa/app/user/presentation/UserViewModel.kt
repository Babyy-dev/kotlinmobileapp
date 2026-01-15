package com.kappa.app.user.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.core.base.ViewState
import com.kappa.app.domain.user.User
import com.kappa.app.user.domain.usecase.GetUserUseCase
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
    val error: String? = null
) : ViewState

/**
 * User ViewModel.
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val getUserUseCase: GetUserUseCase
) : ViewModel() {
    
    private val _viewState = MutableStateFlow(UserViewState())
    val viewState: StateFlow<UserViewState> = _viewState.asStateFlow()
    
    fun loadUser(userId: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            
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
}
