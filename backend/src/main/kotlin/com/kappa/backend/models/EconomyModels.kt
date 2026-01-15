package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class CoinBalanceResponse(
    val userId: String,
    val balance: Long,
    val currency: String = "coins"
)

@Serializable
data class CoinMutationRequest(
    val userId: String,
    val amount: Long
)

@Serializable
data class CoinMutationResponse(
    val userId: String,
    val balance: Long,
    val currency: String = "coins"
)
