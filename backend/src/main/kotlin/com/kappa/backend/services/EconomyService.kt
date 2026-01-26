package com.kappa.backend.services

import com.kappa.backend.data.CoinPackages
import com.kappa.backend.data.CoinPurchases
import com.kappa.backend.data.CoinTransactions
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.AgencyCommissions
import com.kappa.backend.data.DiamondConversions
import com.kappa.backend.data.DiamondTransactions
import com.kappa.backend.data.DiamondWallets
import com.kappa.backend.data.Gifts
import com.kappa.backend.data.Announcements
import com.kappa.backend.data.RewardRequests
import com.kappa.backend.models.CoinBalanceResponse
import com.kappa.backend.models.CoinPackageRequest
import com.kappa.backend.models.CoinPackageResponse
import com.kappa.backend.models.CoinPurchaseResponse
import com.kappa.backend.models.CoinTransactionResponse
import com.kappa.backend.models.DiamondBalanceResponse
import com.kappa.backend.models.DiamondConversionResponse
import com.kappa.backend.models.DiamondTransactionResponse
import com.kappa.backend.models.GiftCatalogResponse
import com.kappa.backend.models.GiftCreateRequest
import com.kappa.backend.models.GiftUpdateRequest
import com.kappa.backend.models.AnnouncementResponse
import com.kappa.backend.models.AnnouncementRequest
import com.kappa.backend.models.AgencyCommissionResponse
import com.kappa.backend.models.RewardRequestResponse
import kotlin.math.abs
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
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

    fun getDiamondBalance(userId: UUID): DiamondBalanceResponse {
        return transaction {
            val row = DiamondWallets.select { DiamondWallets.userId eq userId }.singleOrNull()
            val balance = row?.get(DiamondWallets.balance) ?: 0L
            val locked = row?.get(DiamondWallets.locked) ?: 0L
            DiamondBalanceResponse(
                userId = userId.toString(),
                balance = balance,
                locked = locked
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

    fun listDiamondTransactions(userId: UUID, limit: Int = 50): List<DiamondTransactionResponse> {
        val resolvedLimit = limit.coerceIn(1, 200)
        return transaction {
            DiamondTransactions
                .select { DiamondTransactions.userId eq userId }
                .orderBy(DiamondTransactions.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    DiamondTransactionResponse(
                        id = row[DiamondTransactions.id].toString(),
                        userId = row[DiamondTransactions.userId].toString(),
                        type = row[DiamondTransactions.type],
                        amount = row[DiamondTransactions.amount],
                        balanceAfter = row[DiamondTransactions.balanceAfter],
                        createdAt = row[DiamondTransactions.createdAt]
                    )
                }
        }
    }

    fun creditDiamonds(userId: UUID, amount: Long, giftTransactionId: UUID?, type: String = "GIFT"): DiamondBalanceResponse {
        require(amount > 0) { "Diamond amount must be greater than 0" }
        return adjustDiamonds(userId, amount, giftTransactionId, type)
    }

    fun convertDiamondsToCoins(userId: UUID, diamonds: Long): DiamondConversionResponse {
        require(diamonds > 0) { "Diamonds must be greater than 0" }
        require(diamonds % 2L == 0L) { "Diamonds must be in multiples of 2" }
        val coinsGenerated = diamonds / 2L
        return transaction {
            val balance = adjustDiamonds(userId, -diamonds, null, "CONVERSION")
            val coinBalance = adjustBalance(userId, coinsGenerated, "CONVERSION")
            DiamondConversions.insert {
                it[id] = UUID.randomUUID()
                it[DiamondConversions.userId] = userId
                it[diamondsUsed] = diamonds
                it[DiamondConversions.coinsGenerated] = coinsGenerated
                it[createdAt] = System.currentTimeMillis()
            }
            DiamondConversionResponse(
                userId = userId.toString(),
                diamondsUsed = diamonds,
                coinsGenerated = coinsGenerated,
                coinBalance = coinBalance.balance,
                diamondBalance = balance.balance,
                lockedDiamonds = balance.locked
            )
        }
    }

    fun listCoinPackages(): List<CoinPackageResponse> {
        return transaction {
            CoinPackages.select { CoinPackages.isActive eq true }
                .map { row ->
                    CoinPackageResponse(
                        id = row[CoinPackages.id].toString(),
                        name = row[CoinPackages.name],
                        coinAmount = row[CoinPackages.coinAmount],
                        priceUsd = row[CoinPackages.priceUsd].toPlainString(),
                        isActive = row[CoinPackages.isActive]
                    )
                }
        }
    }

    fun createCoinPackage(request: CoinPackageRequest): CoinPackageResponse {
        require(request.name.isNotBlank()) { "Package name is required" }
        require(request.coinAmount > 0) { "Coin amount must be greater than 0" }
        val price = request.priceUsd.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Invalid price")
        return transaction {
            val id = UUID.randomUUID()
            CoinPackages.insert {
                it[CoinPackages.id] = id
                it[CoinPackages.name] = request.name.trim()
                it[coinAmount] = request.coinAmount
                it[priceUsd] = price
                it[isActive] = request.isActive
                it[createdAt] = System.currentTimeMillis()
            }
            CoinPackageResponse(
                id = id.toString(),
                name = request.name.trim(),
                coinAmount = request.coinAmount,
                priceUsd = price.toPlainString(),
                isActive = request.isActive
            )
        }
    }

    fun updateCoinPackage(id: UUID, request: CoinPackageRequest): CoinPackageResponse? {
        val price = request.priceUsd.toBigDecimalOrNull()
            ?: throw IllegalArgumentException("Invalid price")
        return transaction {
            val row = CoinPackages.select { CoinPackages.id eq id }.singleOrNull() ?: return@transaction null
            CoinPackages.update({ CoinPackages.id eq id }) {
                it[name] = request.name.trim()
                it[coinAmount] = request.coinAmount
                it[priceUsd] = price
                it[isActive] = request.isActive
            }
            CoinPackageResponse(
                id = row[CoinPackages.id].toString(),
                name = request.name.trim(),
                coinAmount = request.coinAmount,
                priceUsd = price.toPlainString(),
                isActive = request.isActive
            )
        }
    }

    fun listGiftCatalog(): List<GiftCatalogResponse> {
        return transaction {
            Gifts.select { Gifts.isActive eq true }
                .map { row ->
                    GiftCatalogResponse(
                        id = row[Gifts.id].toString(),
                        name = row[Gifts.name],
                        giftType = row[Gifts.giftType],
                        costCoins = row[Gifts.costCoins],
                        diamondPercent = row[Gifts.diamondPercent],
                        isActive = row[Gifts.isActive]
                    )
                }
        }
    }

    fun createGift(request: GiftCreateRequest): GiftCatalogResponse {
        require(request.name.isNotBlank()) { "Gift name is required" }
        require(request.costCoins > 0) { "Gift cost must be greater than 0" }
        require(request.diamondPercent in 1..100) { "Diamond percent must be 1-100" }
        return transaction {
            val giftId = UUID.randomUUID()
            Gifts.insert {
                it[id] = giftId
                it[name] = request.name.trim()
                it[giftType] = request.giftType
                it[costCoins] = request.costCoins
                it[diamondPercent] = request.diamondPercent
                it[isActive] = true
                it[createdAt] = System.currentTimeMillis()
            }
            GiftCatalogResponse(
                id = giftId.toString(),
                name = request.name.trim(),
                giftType = request.giftType,
                costCoins = request.costCoins,
                diamondPercent = request.diamondPercent,
                isActive = true
            )
        }
    }

    fun updateGift(giftId: UUID, request: GiftUpdateRequest): GiftCatalogResponse? {
        return transaction {
            val row = Gifts.select { Gifts.id eq giftId }.singleOrNull() ?: return@transaction null
            Gifts.update({ Gifts.id eq giftId }) {
                request.name?.let { value -> it[name] = value.trim() }
                request.giftType?.let { value -> it[giftType] = value }
                request.costCoins?.let { value -> it[costCoins] = value }
                request.diamondPercent?.let { value -> it[diamondPercent] = value }
                request.isActive?.let { value -> it[isActive] = value }
            }
            val updated = Gifts.select { Gifts.id eq giftId }.single()
            GiftCatalogResponse(
                id = updated[Gifts.id].toString(),
                name = updated[Gifts.name],
                giftType = updated[Gifts.giftType],
                costCoins = updated[Gifts.costCoins],
                diamondPercent = updated[Gifts.diamondPercent],
                isActive = updated[Gifts.isActive]
            )
        }
    }

    fun listAnnouncements(): List<AnnouncementResponse> {
        return transaction {
            Announcements.select { Announcements.isActive eq true }
                .orderBy(Announcements.createdAt, SortOrder.DESC)
                .map { row ->
                    AnnouncementResponse(
                        id = row[Announcements.id].toString(),
                        title = row[Announcements.title],
                        message = row[Announcements.message],
                        isActive = row[Announcements.isActive]
                    )
                }
        }
    }

    fun createAnnouncement(request: AnnouncementRequest): AnnouncementResponse {
        require(request.title.isNotBlank()) { "Announcement title is required" }
        require(request.message.isNotBlank()) { "Announcement message is required" }
        return transaction {
            val now = System.currentTimeMillis()
            val id = UUID.randomUUID()
            Announcements.insert {
                it[Announcements.id] = id
                it[Announcements.title] = request.title.trim()
                it[Announcements.message] = request.message.trim()
                it[Announcements.isActive] = request.isActive
                it[Announcements.createdAt] = now
            }
            AnnouncementResponse(
                id = id.toString(),
                title = request.title.trim(),
                message = request.message.trim(),
                isActive = request.isActive
            )
        }
    }

    fun updateAnnouncement(id: UUID, request: AnnouncementRequest): AnnouncementResponse? {
        return transaction {
            val row = Announcements.select { Announcements.id eq id }.singleOrNull() ?: return@transaction null
            Announcements.update({ Announcements.id eq id }) {
                it[title] = request.title.trim()
                it[message] = request.message.trim()
                it[isActive] = request.isActive
            }
            AnnouncementResponse(
                id = row[Announcements.id].toString(),
                title = request.title.trim(),
                message = request.message.trim(),
                isActive = request.isActive
            )
        }
    }

    fun listAgencyCommissions(agencyId: UUID?, limit: Int = 100): List<AgencyCommissionResponse> {
        val resolvedLimit = limit.coerceIn(1, 500)
        return transaction {
            val baseQuery = if (agencyId == null) {
                AgencyCommissions.selectAll()
            } else {
                AgencyCommissions.select { AgencyCommissions.agencyId eq agencyId }
            }
            baseQuery
                .orderBy(AgencyCommissions.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    AgencyCommissionResponse(
                        id = row[AgencyCommissions.id].toString(),
                        agencyId = row[AgencyCommissions.agencyId].toString(),
                        userId = row[AgencyCommissions.userId].toString(),
                        diamondsAmount = row[AgencyCommissions.diamondsAmount],
                        commissionUsd = row[AgencyCommissions.commissionUsd].toPlainString(),
                        createdAt = row[AgencyCommissions.createdAt]
                    )
                }
        }
    }

    fun purchaseCoins(userId: UUID, packageId: UUID, provider: String, providerTxId: String): CoinPurchaseResponse {
        return transaction {
            val packageRow = CoinPackages.select { CoinPackages.id eq packageId }.singleOrNull()
                ?: throw IllegalArgumentException("Package not found")
            val now = System.currentTimeMillis()
            val purchaseId = UUID.randomUUID()
            CoinPurchases.insert {
                it[id] = purchaseId
                it[CoinPurchases.userId] = userId
                it[CoinPurchases.packageId] = packageId
                it[CoinPurchases.provider] = provider
                it[CoinPurchases.providerTxId] = providerTxId
                it[CoinPurchases.status] = "COMPLETED"
                it[createdAt] = now
            }
            val balance = adjustBalance(userId, packageRow[CoinPackages.coinAmount], "PURCHASE")
            CoinPurchaseResponse(
                id = purchaseId.toString(),
                status = "COMPLETED",
                coinBalance = balance.balance
            )
        }
    }

    fun refundPurchase(purchaseId: UUID): CoinPurchaseResponse? {
        return transaction {
            val purchaseRow = CoinPurchases.select { CoinPurchases.id eq purchaseId }.singleOrNull()
                ?: return@transaction null
            val userId = purchaseRow[CoinPurchases.userId]
            val status = purchaseRow[CoinPurchases.status]
            if (status == "REFUNDED") {
                val balance = getCoinBalance(userId)
                return@transaction CoinPurchaseResponse(
                    id = purchaseId.toString(),
                    status = "REFUNDED",
                    coinBalance = balance.balance
                )
            }

            val packageRow = CoinPackages.select { CoinPackages.id eq purchaseRow[CoinPurchases.packageId] }
                .singleOrNull() ?: throw IllegalArgumentException("Package not found")
            val balance = adjustBalance(userId, -packageRow[CoinPackages.coinAmount], "REFUND")
            CoinPurchases.update({ CoinPurchases.id eq purchaseId }) {
                it[CoinPurchases.status] = "REFUNDED"
            }
            CoinPurchaseResponse(
                id = purchaseId.toString(),
                status = "REFUNDED",
                coinBalance = balance.balance
            )
        }
    }

    fun requestReward(userId: UUID, diamonds: Long): RewardRequestResponse {
        require(diamonds > 0) { "Diamonds must be greater than 0" }
        require(diamonds % 600000L == 0L) { "Diamonds must be in multiples of 600000" }
        return transaction {
            val wallet = DiamondWallets.select { DiamondWallets.userId eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("Diamond wallet not found")
            val available = wallet[DiamondWallets.balance] - wallet[DiamondWallets.locked]
            require(available >= diamonds) { "Insufficient diamonds" }
            DiamondWallets.update({ DiamondWallets.userId eq userId }) {
                it[locked] = wallet[DiamondWallets.locked] + diamonds
                it[updatedAt] = System.currentTimeMillis()
            }
            val requestId = UUID.randomUUID()
            RewardRequests.insert {
                it[id] = requestId
                it[RewardRequests.userId] = userId
                it[diamondsRequested] = diamonds
                it[status] = "PENDING"
                it[createdAt] = System.currentTimeMillis()
                it[processedAt] = null
                it[note] = null
            }
            RewardRequestResponse(
                id = requestId.toString(),
                userId = userId.toString(),
                diamonds = diamonds,
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
        }
    }

    fun listRewardRequests(userId: UUID): List<RewardRequestResponse> {
        return transaction {
            RewardRequests.select { RewardRequests.userId eq userId }
                .orderBy(RewardRequests.createdAt, SortOrder.DESC)
                .map { row ->
                    RewardRequestResponse(
                        id = row[RewardRequests.id].toString(),
                        userId = row[RewardRequests.userId].toString(),
                        diamonds = row[RewardRequests.diamondsRequested],
                        status = row[RewardRequests.status],
                        createdAt = row[RewardRequests.createdAt],
                        processedAt = row[RewardRequests.processedAt],
                        note = row[RewardRequests.note]
                    )
                }
        }
    }

    fun listAllRewardRequests(status: String?, limit: Int): List<RewardRequestResponse> {
        val resolvedLimit = limit.coerceIn(1, 200)
        val normalizedStatus = status?.trim()?.uppercase()
        return transaction {
            val query = if (normalizedStatus.isNullOrBlank()) {
                RewardRequests.selectAll()
            } else {
                RewardRequests.select { RewardRequests.status eq normalizedStatus }
            }
            query.orderBy(RewardRequests.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    RewardRequestResponse(
                        id = row[RewardRequests.id].toString(),
                        userId = row[RewardRequests.userId].toString(),
                        diamonds = row[RewardRequests.diamondsRequested],
                        status = row[RewardRequests.status],
                        createdAt = row[RewardRequests.createdAt],
                        processedAt = row[RewardRequests.processedAt],
                        note = row[RewardRequests.note]
                    )
                }
        }
    }

    fun reviewRewardRequest(requestId: UUID, approved: Boolean, note: String?): RewardRequestResponse? {
        return transaction {
            val row = RewardRequests.select { RewardRequests.id eq requestId }.singleOrNull()
                ?: return@transaction null
            if (row[RewardRequests.status] != "PENDING") {
                return@transaction null
            }
            val userId = row[RewardRequests.userId]
            val diamonds = row[RewardRequests.diamondsRequested]
            val now = System.currentTimeMillis()
            val newStatus = if (approved) "APPROVED" else "REJECTED"
            RewardRequests.update({ RewardRequests.id eq requestId }) {
                it[status] = newStatus
                it[processedAt] = now
                it[RewardRequests.note] = note
            }
            if (approved) {
                val wallet = DiamondWallets.select { DiamondWallets.userId eq userId }.singleOrNull()
                    ?: return@transaction null
                DiamondWallets.update({ DiamondWallets.userId eq userId }) {
                    it[balance] = wallet[DiamondWallets.balance] - diamonds
                    it[locked] = wallet[DiamondWallets.locked] - diamonds
                    it[updatedAt] = now
                }
                DiamondTransactions.insert {
                    it[id] = UUID.randomUUID()
                    it[DiamondTransactions.userId] = userId
                    it[giftTransactionId] = null
                    it[type] = "REWARD"
                    it[amount] = diamonds
                    it[balanceAfter] = wallet[DiamondWallets.balance] - diamonds
                    it[createdAt] = now
                }
            } else {
                val wallet = DiamondWallets.select { DiamondWallets.userId eq userId }.singleOrNull()
                    ?: return@transaction null
                DiamondWallets.update({ DiamondWallets.userId eq userId }) {
                    it[locked] = wallet[DiamondWallets.locked] - diamonds
                    it[updatedAt] = now
                }
            }
            RewardRequestResponse(
                id = row[RewardRequests.id].toString(),
                userId = userId.toString(),
                diamonds = diamonds,
                status = newStatus,
                createdAt = row[RewardRequests.createdAt],
                processedAt = now,
                note = note
            )
        }
    }

    private fun adjustBalance(userId: UUID, delta: Long, typeOverride: String? = null): CoinBalanceResponse {
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

            val type = typeOverride ?: if (delta >= 0) "CREDIT" else "DEBIT"
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

    private fun adjustDiamonds(
        userId: UUID,
        delta: Long,
        giftTransactionId: UUID?,
        typeOverride: String
    ): DiamondBalanceResponse {
        return transaction {
            val now = System.currentTimeMillis()
            val row = DiamondWallets.select { DiamondWallets.userId eq userId }.singleOrNull()
            val currentBalance = row?.get(DiamondWallets.balance) ?: 0L
            val currentLocked = row?.get(DiamondWallets.locked) ?: 0L
            val newBalance = currentBalance + delta
            require(newBalance >= 0) { "Insufficient diamonds" }
            if (row == null) {
                DiamondWallets.insert {
                    it[DiamondWallets.userId] = userId
                    it[DiamondWallets.balance] = newBalance
                    it[DiamondWallets.locked] = currentLocked
                    it[DiamondWallets.updatedAt] = now
                }
            } else {
                DiamondWallets.update({ DiamondWallets.userId eq userId }) {
                    it[balance] = newBalance
                    it[locked] = currentLocked
                    it[updatedAt] = now
                }
            }

            DiamondTransactions.insert {
                it[id] = UUID.randomUUID()
                it[DiamondTransactions.userId] = userId
                it[DiamondTransactions.giftTransactionId] = giftTransactionId
                it[type] = typeOverride
                it[amount] = abs(delta)
                it[balanceAfter] = newBalance
                it[createdAt] = now
            }

            DiamondBalanceResponse(
                userId = userId.toString(),
                balance = newBalance,
                locked = currentLocked
            )
        }
    }
}
