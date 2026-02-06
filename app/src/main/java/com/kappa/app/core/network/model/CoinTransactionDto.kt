package com.kappa.app.core.network.model

data class CoinTransactionDto(
    val id: String,
    val userId: String,
    val type: String,
    val amount: Long,
    val balanceAfter: Long,
    val createdAt: Long
)
