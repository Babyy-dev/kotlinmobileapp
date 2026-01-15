package com.kappa.app.economy.domain.repository

import com.kappa.app.domain.economy.CoinBalance
import com.kappa.app.domain.economy.Transaction

/**
 * Economy repository interface.
 */
interface EconomyRepository {
    suspend fun getCoinBalance(userId: String): Result<CoinBalance>
    suspend fun getTransactions(userId: String): Result<List<Transaction>>
}
