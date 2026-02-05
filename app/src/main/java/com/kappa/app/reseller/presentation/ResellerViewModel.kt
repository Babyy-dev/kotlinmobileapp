package com.kappa.app.reseller.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.reseller.domain.model.ResellerPaymentProof
import com.kappa.app.reseller.domain.model.ResellerSale
import com.kappa.app.reseller.domain.model.ResellerSeller
import com.kappa.app.reseller.domain.model.ResellerSellerLimit
import com.kappa.app.reseller.domain.repository.ResellerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ResellerState(
    val sellers: List<ResellerSeller> = emptyList(),
    val limits: List<ResellerSellerLimit> = emptyList(),
    val sales: List<ResellerSale> = emptyList(),
    val proofs: List<ResellerPaymentProof> = emptyList(),
    val message: String? = null,
    val isLoading: Boolean = false
) 

@HiltViewModel
class ResellerViewModel @Inject constructor(
    private val repository: ResellerRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(ResellerState(isLoading = true))
    val viewState: StateFlow<ResellerState> = _viewState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _viewState.update { it.copy(isLoading = true, message = null) }
            val sellers = repository.listSellers().getOrElse {
                emitMessage(it.message)
                emptyList()
            }
            val limits = sellers.mapNotNull { seller ->
                repository.getSellerLimit(seller.sellerId).getOrNull()
            }
            val sales = repository.listSales().getOrElse {
                emitMessage(it.message)
                emptyList()
            }
            val proofs = repository.listProofs().getOrElse {
                emitMessage(it.message)
                emptyList()
            }
            _viewState.update {
                it.copy(
                    sellers = sellers,
                    limits = limits,
                    sales = sales,
                    proofs = proofs,
                    isLoading = false
                )
            }
        }
    }

    fun addSeller(sellerId: String) {
        viewModelScope.launch {
            repository.addSeller(sellerId)
                .onSuccess { seller ->
                    _viewState.update { it.copy(sellers = it.sellers + seller, message = "Seller added") }
                }
                .onFailure { emitMessage(it.message) }
            refreshAll()
        }
    }

    fun setSellerLimits(sellerId: String, totalLimit: Long, dailyLimit: Long) {
        viewModelScope.launch {
            repository.setSellerLimit(sellerId, totalLimit, dailyLimit)
                .onSuccess { limit ->
                    _viewState.update {
                        it.copy(
                            limits = it.limits.filterNot { item -> item.sellerId == sellerId } + limit,
                            message = "Limits updated"
                        )
                    }
                }
                .onFailure { emitMessage(it.message) }
            refreshAll()
        }
    }

    fun addSale(
        saleId: String,
        sellerId: String,
        buyerId: String,
        amount: Long,
        currency: String,
        destinationAccount: String
    ) {
        viewModelScope.launch {
            repository.createSale(saleId, sellerId, buyerId, amount, currency, destinationAccount)
                .onSuccess { sale ->
                    _viewState.update { it.copy(sales = it.sales + sale, message = "Sale recorded") }
                }
                .onFailure { emitMessage(it.message) }
            refreshAll()
        }
    }

    fun addPaymentProof(uri: String, amount: Long, date: String, beneficiary: String, note: String?) {
        viewModelScope.launch {
            repository.createProof(uri, amount, date, beneficiary, note)
                .onSuccess { proof ->
                    _viewState.update { it.copy(proofs = it.proofs + proof, message = "Payment proof uploaded") }
                }
                .onFailure { emitMessage(it.message) }
            refreshAll()
        }
    }

    fun sendCoins(recipient: String, amount: Long) {
        viewModelScope.launch {
            repository.sendCoins(recipient, amount, null)
                .onSuccess { result ->
                    _viewState.update { it.copy(message = "Coins sent. Balance ${result.balance}") }
                }
                .onFailure { emitMessage(it.message) }
        }
    }

    private fun emitMessage(message: String?) {
        if (message.isNullOrBlank()) return
        _viewState.update { it.copy(message = message, isLoading = false) }
    }
}
