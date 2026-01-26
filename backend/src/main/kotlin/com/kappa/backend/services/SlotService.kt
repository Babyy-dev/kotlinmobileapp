package com.kappa.backend.services

import com.kappa.backend.data.CoinTransactions
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.SlotPlays
import com.kappa.backend.models.SlotPlayResponse
import kotlin.math.abs
import kotlin.random.Random
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class SlotService {
    fun play(userId: UUID, betCoins: Long): SlotPlayResponse {
        require(betCoins > 0) { "Bet must be greater than 0" }
        return transaction {
            val now = System.currentTimeMillis()
            val wallet = CoinWallets.select { CoinWallets.userId eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("Coin wallet not found")
            val currentBalance = wallet[CoinWallets.balance]
            val newBalance = currentBalance - betCoins
            require(newBalance >= 0) { "Insufficient balance" }

            CoinWallets.update({ CoinWallets.userId eq userId }) {
                it[balance] = newBalance
                it[updatedAt] = now
            }
            CoinTransactions.insert {
                it[id] = UUID.randomUUID()
                it[CoinTransactions.userId] = userId
                it[CoinTransactions.type] = "SLOT_BET"
                it[amount] = abs(betCoins)
                it[balanceAfter] = newBalance
                it[createdAt] = now
            }

            val roll = Random.nextInt(100)
            val multiplier = when {
                roll < 10 -> 5
                roll < 35 -> 2
                else -> 0
            }
            val winCoins = betCoins * multiplier
            val finalBalance = if (winCoins > 0) {
                val updatedBalance = newBalance + winCoins
                CoinWallets.update({ CoinWallets.userId eq userId }) {
                    it[balance] = updatedBalance
                    it[updatedAt] = now
                }
                CoinTransactions.insert {
                    it[id] = UUID.randomUUID()
                    it[CoinTransactions.userId] = userId
                    it[CoinTransactions.type] = "SLOT_WIN"
                    it[amount] = winCoins
                    it[balanceAfter] = updatedBalance
                    it[createdAt] = now
                }
                updatedBalance
            } else {
                newBalance
            }

            val playId = UUID.randomUUID()
            SlotPlays.insert {
                it[id] = playId
                it[SlotPlays.userId] = userId
                it[SlotPlays.betCoins] = betCoins
                it[SlotPlays.winCoins] = winCoins
                it[SlotPlays.balanceAfter] = finalBalance
                it[createdAt] = now
            }

            SlotPlayResponse(
                id = playId.toString(),
                betCoins = betCoins,
                winCoins = winCoins,
                balanceAfter = finalBalance,
                createdAt = now
            )
        }
    }

    fun history(userId: UUID, limit: Int = 50): List<SlotPlayResponse> {
        val resolvedLimit = limit.coerceIn(1, 200)
        return transaction {
            SlotPlays.select { SlotPlays.userId eq userId }
                .orderBy(SlotPlays.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    SlotPlayResponse(
                        id = row[SlotPlays.id].toString(),
                        betCoins = row[SlotPlays.betCoins],
                        winCoins = row[SlotPlays.winCoins],
                        balanceAfter = row[SlotPlays.balanceAfter],
                        createdAt = row[SlotPlays.createdAt]
                    )
                }
        }
    }
}
