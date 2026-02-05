package com.kappa.app.economy.data.repository

import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.toDomain
import com.kappa.app.domain.economy.CoinBalance
import com.kappa.app.domain.economy.Transaction
import com.kappa.app.domain.economy.TransactionType
import com.kappa.app.economy.domain.repository.EconomyRepository
import javax.inject.Inject

class RemoteEconomyRepository @Inject constructor(
    private val apiService: ApiService,
    private val errorMapper: ErrorMapper
) : EconomyRepository {
    override suspend fun getCoinBalance(userId: String): Result<CoinBalance> {
        return try {
            val response = apiService.getCoinBalance()
            val balance = response.data
            if (!response.success || balance == null) {
                Result.failure(Exception(response.error ?: "Failed to load balance"))
            } else {
                Result.success(balance.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun getTransactions(userId: String): Result<List<Transaction>> {
        return try {
            val now = System.currentTimeMillis()
            val list = listOf(
                Transaction("txn_1", userId, 50000, TransactionType.DEPOSIT, now - 86_400_000L, "Initial credit"),
                Transaction("txn_2", userId, -1200, TransactionType.PURCHASE, now - 36_000_000L, "Room gift purchase"),
                Transaction("txn_3", userId, 2500, TransactionType.REWARD, now - 7_200_000L, "Mini-game reward")
            )
            Result.success(list)
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }
}
