package com.kappa.backend.services

import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.ResellerAuditLogs
import com.kappa.backend.data.ResellerPaymentProofs
import com.kappa.backend.data.ResellerSales
import com.kappa.backend.data.ResellerSellerLimits
import com.kappa.backend.data.ResellerSellers
import com.kappa.backend.data.Users
import com.kappa.backend.models.ResellerLogResponse
import com.kappa.backend.models.ResellerProofRequest
import com.kappa.backend.models.ResellerProofResponse
import com.kappa.backend.models.ResellerSaleRequest
import com.kappa.backend.models.ResellerSaleResponse
import com.kappa.backend.models.ResellerSellerLimitRequest
import com.kappa.backend.models.ResellerSellerLimitResponse
import com.kappa.backend.models.ResellerSellerResponse
import com.kappa.backend.models.ResellerSendCoinsRequest
import com.kappa.backend.models.ResellerSendCoinsResponse
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class ResellerService(
    private val economyService: EconomyService
) {
    fun listSellers(resellerId: UUID): List<ResellerSellerResponse> {
        return transaction {
            ResellerSellers.select { ResellerSellers.resellerId eq resellerId }
                .orderBy(ResellerSellers.createdAt, SortOrder.DESC)
                .map { row ->
                    ResellerSellerResponse(
                        id = row[ResellerSellers.id].toString(),
                        sellerId = row[ResellerSellers.sellerId].toString(),
                        createdAt = row[ResellerSellers.createdAt]
                    )
                }
        }
    }

    fun addSeller(resellerId: UUID, sellerId: UUID): ResellerSellerResponse {
        return transaction {
            val exists = ResellerSellers.select {
                (ResellerSellers.resellerId eq resellerId) and (ResellerSellers.sellerId eq sellerId)
            }.singleOrNull()
            if (exists != null) {
                return@transaction ResellerSellerResponse(
                    id = exists[ResellerSellers.id].toString(),
                    sellerId = exists[ResellerSellers.sellerId].toString(),
                    createdAt = exists[ResellerSellers.createdAt]
                )
            }
            val now = System.currentTimeMillis()
            val id = UUID.randomUUID()
            ResellerSellers.insert {
                it[ResellerSellers.id] = id
                it[ResellerSellers.resellerId] = resellerId
                it[ResellerSellers.sellerId] = sellerId
                it[ResellerSellers.createdAt] = now
            }
            ResellerSellerResponse(
                id = id.toString(),
                sellerId = sellerId.toString(),
                createdAt = now
            )
        }
    }

    fun getLimits(resellerId: UUID, sellerId: UUID): ResellerSellerLimitResponse? {
        return transaction {
            ResellerSellerLimits.select {
                (ResellerSellerLimits.resellerId eq resellerId) and (ResellerSellerLimits.sellerId eq sellerId)
            }.singleOrNull()?.let { row ->
                ResellerSellerLimitResponse(
                    sellerId = row[ResellerSellerLimits.sellerId].toString(),
                    totalLimit = row[ResellerSellerLimits.totalLimit],
                    dailyLimit = row[ResellerSellerLimits.dailyLimit],
                    updatedAt = row[ResellerSellerLimits.updatedAt]
                )
            }
        }
    }

    fun setLimits(resellerId: UUID, sellerId: UUID, request: ResellerSellerLimitRequest): ResellerSellerLimitResponse {
        require(request.totalLimit >= 0 && request.dailyLimit >= 0) { "Invalid limits" }
        return transaction {
            val now = System.currentTimeMillis()
            val exists = ResellerSellerLimits.select {
                (ResellerSellerLimits.resellerId eq resellerId) and (ResellerSellerLimits.sellerId eq sellerId)
            }.any()
            if (exists) {
                ResellerSellerLimits.update({
                    (ResellerSellerLimits.resellerId eq resellerId) and (ResellerSellerLimits.sellerId eq sellerId)
                }) {
                    it[totalLimit] = request.totalLimit
                    it[dailyLimit] = request.dailyLimit
                    it[updatedAt] = now
                }
            } else {
                ResellerSellerLimits.insert {
                    it[ResellerSellerLimits.resellerId] = resellerId
                    it[ResellerSellerLimits.sellerId] = sellerId
                    it[totalLimit] = request.totalLimit
                    it[dailyLimit] = request.dailyLimit
                    it[updatedAt] = now
                }
            }
            ResellerSellerLimitResponse(
                sellerId = sellerId.toString(),
                totalLimit = request.totalLimit,
                dailyLimit = request.dailyLimit,
                updatedAt = now
            )
        }
    }

    fun listSales(resellerId: UUID): List<ResellerSaleResponse> {
        return transaction {
            ResellerSales.select { ResellerSales.resellerId eq resellerId }
                .orderBy(ResellerSales.createdAt, SortOrder.DESC)
                .map { row ->
                    ResellerSaleResponse(
                        id = row[ResellerSales.id].toString(),
                        saleId = row[ResellerSales.externalSaleId],
                        sellerId = row[ResellerSales.sellerId].toString(),
                        buyerId = row[ResellerSales.buyerId].toString(),
                        amount = row[ResellerSales.amount],
                        currency = row[ResellerSales.currency],
                        destinationAccount = row[ResellerSales.destinationAccount],
                        createdAt = row[ResellerSales.createdAt]
                    )
                }
        }
    }

    fun createSale(resellerId: UUID, request: ResellerSaleRequest): ResellerSaleResponse {
        require(request.amount > 0) { "Amount must be greater than 0" }
        val sellerResolved = resolveUserId(request.sellerId)
            ?: throw IllegalArgumentException("Seller not found")
        val buyerResolved = resolveUserId(request.buyerId)
            ?: throw IllegalArgumentException("Buyer not found")
        return transaction {
            val now = System.currentTimeMillis()
            val id = UUID.randomUUID()
            ResellerSales.insert {
                it[ResellerSales.id] = id
                it[ResellerSales.resellerId] = resellerId
                it[ResellerSales.sellerId] = sellerResolved
                it[ResellerSales.buyerId] = buyerResolved
                it[ResellerSales.externalSaleId] = request.saleId.trim().ifBlank { null }
                it[ResellerSales.amount] = request.amount
                it[ResellerSales.currency] = request.currency.trim()
                it[ResellerSales.destinationAccount] = request.destinationAccount.trim()
                it[ResellerSales.createdAt] = now
            }
            ResellerSaleResponse(
                id = id.toString(),
                saleId = request.saleId.trim().ifBlank { null },
                sellerId = sellerResolved.toString(),
                buyerId = buyerResolved.toString(),
                amount = request.amount,
                currency = request.currency.trim(),
                destinationAccount = request.destinationAccount.trim(),
                createdAt = now
            )
        }
    }

    fun listProofs(resellerId: UUID): List<ResellerProofResponse> {
        return transaction {
            ResellerPaymentProofs.select { ResellerPaymentProofs.resellerId eq resellerId }
                .orderBy(ResellerPaymentProofs.createdAt, SortOrder.DESC)
                .map { row ->
                    ResellerProofResponse(
                        id = row[ResellerPaymentProofs.id].toString(),
                        uri = row[ResellerPaymentProofs.uri],
                        amount = row[ResellerPaymentProofs.amount],
                        date = row[ResellerPaymentProofs.date],
                        beneficiary = row[ResellerPaymentProofs.beneficiary],
                        note = row[ResellerPaymentProofs.note],
                        createdAt = row[ResellerPaymentProofs.createdAt]
                    )
                }
        }
    }

    fun createProof(resellerId: UUID, request: ResellerProofRequest): ResellerProofResponse {
        require(request.amount > 0) { "Amount must be greater than 0" }
        require(request.uri.isNotBlank()) { "Proof uri required" }
        return transaction {
            val now = System.currentTimeMillis()
            val id = UUID.randomUUID()
            ResellerPaymentProofs.insert {
                it[ResellerPaymentProofs.id] = id
                it[ResellerPaymentProofs.resellerId] = resellerId
                it[ResellerPaymentProofs.uri] = request.uri.trim()
                it[ResellerPaymentProofs.amount] = request.amount
                it[ResellerPaymentProofs.date] = request.date.trim()
                it[ResellerPaymentProofs.beneficiary] = request.beneficiary.trim()
                it[ResellerPaymentProofs.note] = request.note?.trim()
                it[ResellerPaymentProofs.createdAt] = now
            }
            ResellerProofResponse(
                id = id.toString(),
                uri = request.uri.trim(),
                amount = request.amount,
                date = request.date.trim(),
                beneficiary = request.beneficiary.trim(),
                note = request.note?.trim(),
                createdAt = now
            )
        }
    }

    fun sendCoins(resellerId: UUID, recipientId: UUID, request: ResellerSendCoinsRequest): ResellerSendCoinsResponse {
        require(request.amount > 0) { "Amount must be greater than 0" }
        validateLimits(resellerId, recipientId, request.amount)
        val balance = economyService.debitCoins(resellerId, request.amount)
        economyService.creditCoins(recipientId, request.amount)
        return ResellerSendCoinsResponse(balance = balance.balance)
    }

    fun listLogs(resellerId: UUID, limit: Int): List<ResellerLogResponse> {
        val resolvedLimit = limit.coerceIn(1, 200)
        return transaction {
            ResellerAuditLogs.select { ResellerAuditLogs.resellerId eq resellerId }
                .orderBy(ResellerAuditLogs.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    ResellerLogResponse(
                        id = row[ResellerAuditLogs.id].toString(),
                        resellerId = row[ResellerAuditLogs.resellerId].toString(),
                        actorId = row[ResellerAuditLogs.actorId].toString(),
                        action = row[ResellerAuditLogs.action],
                        message = row[ResellerAuditLogs.message],
                        createdAt = row[ResellerAuditLogs.createdAt]
                    )
                }
        }
    }

    fun logAction(resellerId: UUID, actorId: UUID, action: String, message: String?) {
        transaction {
            ResellerAuditLogs.insert {
                it[ResellerAuditLogs.id] = UUID.randomUUID()
                it[ResellerAuditLogs.resellerId] = resellerId
                it[ResellerAuditLogs.actorId] = actorId
                it[ResellerAuditLogs.action] = action
                it[ResellerAuditLogs.message] = message
                it[ResellerAuditLogs.createdAt] = System.currentTimeMillis()
            }
        }
    }

    fun resolveUserId(rawValue: String): UUID? {
        return transaction {
            val trimmed = rawValue.trim()
            val parsed = runCatching { UUID.fromString(trimmed) }.getOrNull()
            if (parsed != null) {
                return@transaction Users.select { Users.id eq parsed }.singleOrNull()?.get(Users.id)
            }
            Users.select { Users.username eq trimmed }.singleOrNull()?.get(Users.id)
        }
    }

    private fun validateLimits(resellerId: UUID, sellerId: UUID, amount: Long) {
        transaction {
            val limits = ResellerSellerLimits.select {
                (ResellerSellerLimits.resellerId eq resellerId) and (ResellerSellerLimits.sellerId eq sellerId)
            }.singleOrNull() ?: return@transaction

            if (amount > limits[ResellerSellerLimits.totalLimit]) {
                throw IllegalArgumentException("Amount exceeds seller total limit")
            }

            val dailyLimit = limits[ResellerSellerLimits.dailyLimit]
            if (dailyLimit <= 0) return@transaction

            val zone = ZoneId.systemDefault()
            val startOfDay = LocalDate.now(zone).atStartOfDay(zone).toInstant().toEpochMilli()
            val sum = ResellerSales
                .slice(ResellerSales.amount.sum())
                .select {
                    (ResellerSales.resellerId eq resellerId) and
                        (ResellerSales.sellerId eq sellerId) and
                        (ResellerSales.createdAt greaterEq startOfDay)
                }
                .singleOrNull()
                ?.getOrNull(ResellerSales.amount.sum()) ?: 0L

            if (sum + amount > dailyLimit) {
                throw IllegalArgumentException("Amount exceeds seller daily limit")
            }
        }
    }

    fun ensureWallet(userId: UUID) {
        transaction {
            val exists = CoinWallets.select { CoinWallets.userId eq userId }.any()
            if (!exists) {
                CoinWallets.insert {
                    it[CoinWallets.userId] = userId
                    it[CoinWallets.balance] = 0
                    it[CoinWallets.updatedAt] = System.currentTimeMillis()
                }
            }
        }
    }
}
