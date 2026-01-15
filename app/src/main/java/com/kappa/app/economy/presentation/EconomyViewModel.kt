package com.kappa.app.economy.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.core.base.ViewState
import com.kappa.app.domain.economy.CoinBalance
import com.kappa.app.economy.domain.usecase.GetCoinBalanceUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Economy ViewState.
 */
data class EconomyViewState(
    val coinBalance: CoinBalance? = null,
    val isLoading: Boolean = false,
    val error: String? = null
) : ViewState

/**
 * Economy ViewModel.
 */
@HiltViewModel
class EconomyViewModel @Inject constructor(
    private val getCoinBalanceUseCase: GetCoinBalanceUseCase
) : ViewModel() {
    
    private val _viewState = MutableStateFlow(EconomyViewState())
    val viewState: StateFlow<EconomyViewState> = _viewState.asStateFlow()
    
    fun loadCoinBalance(userId: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            
            getCoinBalanceUseCase(userId)
                .onSuccess { balance ->
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        coinBalance = balance
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
