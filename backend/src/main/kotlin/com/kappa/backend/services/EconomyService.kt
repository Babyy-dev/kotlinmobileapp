package com.kappa.backend.services

import com.kappa.backend.data.CoinWallets
import com.kappa.backend.models.CoinBalanceResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
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

            CoinBalanceResponse(
                userId = userId.toString(),
                balance = newBalance,
                currency = "coins"
            )
        }
    }
}
