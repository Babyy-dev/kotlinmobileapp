package com.kappa.app.reseller.presentation

import androidx.lifecycle.ViewModel
import com.kappa.app.core.base.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SellerLimit(
    val sellerId: String,
    val totalLimit: Long,
    val dailyLimit: Long
)

data class SellerSale(
    val saleId: String,
    val sellerId: String,
    val buyerId: String,
    val amount: Long,
    val currency: String,
    val timestamp: Long,
    val destinationAccount: String
)

data class PaymentProof(
    val uri: String,
    val amount: Long,
    val date: String,
    val beneficiary: String,
    val note: String?
)

data class ResellerState(
    val sellers: List<String> = emptyList(),
    val limits: List<SellerLimit> = emptyList(),
    val sales: List<SellerSale> = emptyList(),
    val proofs: List<PaymentProof> = emptyList(),
    val message: String? = null
) : ViewState

class ResellerViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(ResellerState())
    val viewState: StateFlow<ResellerState> = _viewState.asStateFlow()

    fun addSeller(sellerId: String) {
        _viewState.value = _viewState.value.copy(
            sellers = _viewState.value.sellers + sellerId,
            limits = _viewState.value.limits + SellerLimit(sellerId, 5000, 1000),
            message = "Seller added"
        )
    }

    fun setSellerLimits(sellerId: String, totalLimit: Long, dailyLimit: Long) {
        val updated = _viewState.value.limits.filterNot { it.sellerId == sellerId } +
            SellerLimit(sellerId, totalLimit, dailyLimit)
        _viewState.value = _viewState.value.copy(
            limits = updated,
            message = "Limits updated"
        )
    }

    fun addSale(
        saleId: String,
        sellerId: String,
        buyerId: String,
        amount: Long,
        currency: String,
        destinationAccount: String
    ) {
        _viewState.value = _viewState.value.copy(
            sales = _viewState.value.sales + SellerSale(
                saleId = saleId,
                sellerId = sellerId,
                buyerId = buyerId,
                amount = amount,
                currency = currency,
                timestamp = System.currentTimeMillis(),
                destinationAccount = destinationAccount
            ),
            message = "Sale recorded"
        )
    }

    fun addPaymentProof(uri: String, amount: Long, date: String, beneficiary: String, note: String?) {
        _viewState.value = _viewState.value.copy(
            proofs = _viewState.value.proofs + PaymentProof(uri, amount, date, beneficiary, note),
            message = "Payment proof uploaded"
        )
    }

    fun sendCoins(recipient: String, amount: Long) {
        val limit = _viewState.value.limits.find { it.sellerId == recipient }
        if (limit != null && amount > limit.totalLimit) {
            _viewState.value = _viewState.value.copy(message = "Amount exceeds seller limit")
            return
        }
        _viewState.value = _viewState.value.copy(message = "Coins sent to $recipient")
    }
}
