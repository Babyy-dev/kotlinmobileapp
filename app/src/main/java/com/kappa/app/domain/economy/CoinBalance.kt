package com.kappa.app.domain.economy

/**
 * Coin balance domain model.
 */
data class CoinBalance(
    val userId: String,
    val balance: Long,
    val currency: String = "coins"
)

/**
 * Transaction domain model.
 */
data class Transaction(
    val id: String,
    val userId: String,
    val amount: Long,
    val type: TransactionType,
    val timestamp: Long,
    val description: String? = null
)

/**
 * Transaction types.
 */
enum class TransactionType {
    DEPOSIT,
    WITHDRAWAL,
    REWARD,
    PURCHASE
}

/**
 * Reward request domain model.
 */
data class RewardRequest(
    val userId: String,
    val amount: Long,
    val reason: String? = null
)
