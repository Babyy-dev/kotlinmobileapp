package com.kappa.backend.services

import com.kappa.backend.data.CoinTransactions
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.models.CoinBalanceResponse
import com.kappa.backend.models.CoinTransactionResponse
import kotlin.math.abs
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class EconomyService {
    fun getCoinBalance(userId: UUID): CoinBalanceResponse {
        return transaction {
            val row = CoinWallets.select { CoinWallets.userId eq userId }.singleOrNull()
            val balance = row?.get(CoinWallets.balance) ?: 0L
            CoinBalanceResponse(
                userId = userId.toString(),
                balance = balance,
                currency = "coins"
            )
        }
    }

    fun creditCoins(userId: UUID, amount: Long): CoinBalanceResponse {
        require(amount > 0) { "Credit amount must be greater than 0" }
        return adjustBalance(userId, amount)
    }

    fun debitCoins(userId: UUID, amount: Long): CoinBalanceResponse {
        require(amount > 0) { "Debit amount must be greater than 0" }
        return adjustBalance(userId, -amount)
    }

    fun listTransactions(userId: UUID, limit: Int = 50): List<CoinTransactionResponse> {
        val resolvedLimit = limit.coerceIn(1, 200)
        return transaction {
            CoinTransactions
                .select { CoinTransactions.userId eq userId }
                .orderBy(CoinTransactions.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    CoinTransactionResponse(
                        id = row[CoinTransactions.id].toString(),
                        userId = row[CoinTransactions.userId].toString(),
                        type = row[CoinTransactions.type],
                        amount = row[CoinTransactions.amount],
                        balanceAfter = row[CoinTransactions.balanceAfter],
                        createdAt = row[CoinTransactions.createdAt]
                    )
                }
        }
    }

    private fun adjustBalance(userId: UUID, delta: Long): CoinBalanceResponse {
        return transaction {
            val now = System.currentTimeMillis()
            val row = CoinWallets.select { CoinWallets.userId eq userId }.singleOrNull()
            val currentBalance = row?.get(CoinWallets.balance) ?: 0L
            val newBalance = currentBalance + delta
            require(newBalance >= 0) { "Insufficient balance" }

            if (row == null) {
                CoinWallets.insert {
                    it[CoinWallets.userId] = userId
                    it[CoinWallets.balance] = newBalance
                    it[CoinWallets.updatedAt] = now
                }
            } else {
                CoinWallets.update({ CoinWallets.userId eq userId }) {
                    it[balance] = newBalance
                    it[updatedAt] = now
                }
            }

            val type = if (delta >= 0) "CREDIT" else "DEBIT"
            CoinTransactions.insert {
                it[id] = UUID.randomUUID()
                it[CoinTransactions.userId] = userId
                it[CoinTransactions.type] = type
                it[amount] = abs(delta)
                it[balanceAfter] = newBalance
                it[createdAt] = now
            }

            CoinBalanceResponse(
                userId = userId.toString(),
                balance = newBalance,
                currency = "coins"
            )
        }
    }
}
