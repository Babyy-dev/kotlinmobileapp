package com.kappa.app.core.network.model

data class CoinBalanceDto(
    val userId: String,
    val balance: Long,
    val currency: String
)
