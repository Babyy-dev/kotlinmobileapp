package com.kappa.backend.services

import com.kappa.backend.data.Agencies
import com.kappa.backend.data.AgencyCommissions
import com.kappa.backend.data.CoinTransactions
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.DiamondTransactions
import com.kappa.backend.data.DiamondWallets
import com.kappa.backend.data.GiftTransactions
import com.kappa.backend.data.Gifts
import com.kappa.backend.data.RoomMessages
import com.kappa.backend.data.RoomParticipants
import com.kappa.backend.data.Rooms
import com.kappa.backend.data.Users
import com.kappa.backend.models.GiftSendResponse
import com.kappa.backend.models.RoomMessageResponse
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID
import kotlin.math.abs
import java.math.BigDecimal

class RoomInteractionService {
    enum class MessageFailure {
        ROOM_NOT_FOUND,
        USER_NOT_IN_ROOM,
        USER_NOT_FOUND,
        INVALID_MESSAGE
    }

    data class MessageResult(
        val response: RoomMessageResponse? = null,
        val failure: MessageFailure? = null
    )

    enum class GiftFailure {
        ROOM_NOT_FOUND,
        USER_NOT_IN_ROOM,
        RECIPIENT_NOT_FOUND,
        RECIPIENT_NOT_IN_ROOM,
        INVALID_AMOUNT,
        INSUFFICIENT_BALANCE
    }

    data class GiftResult(
        val response: GiftSendResponse? = null,
        val failure: GiftFailure? = null
    )

    fun listMessages(roomId: UUID, limit: Int = 50): List<RoomMessageResponse>? {
        val resolvedLimit = limit.coerceIn(1, 200)
        return transaction {
            val exists = Rooms.select { Rooms.id eq roomId }.any()
            if (!exists) {
                return@transaction null
            }
            val rows = RoomMessages.select { RoomMessages.roomId eq roomId }
                .orderBy(RoomMessages.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .toList()
            if (rows.isEmpty()) {
                return@transaction emptyList()
            }
            val userIds = rows.map { it[RoomMessages.userId] }.distinct()
            val usernames = Users.select { Users.id inList userIds }
                .associate { it[Users.id] to it[Users.username] }
            rows.asReversed().map { row ->
                val userId = row[RoomMessages.userId]
                RoomMessageResponse(
                    id = row[RoomMessages.id].toString(),
                    roomId = row[RoomMessages.roomId].toString(),
                    userId = userId.toString(),
                    username = usernames[userId] ?: "Unknown",
                    message = row[RoomMessages.message],
                    createdAt = row[RoomMessages.createdAt]
                )
            }
        }
    }

    fun sendMessage(roomId: UUID, userId: UUID, message: String): MessageResult {
        val trimmed = message.trim()
        if (trimmed.isBlank() || trimmed.length > 500) {
            return MessageResult(failure = MessageFailure.INVALID_MESSAGE)
        }
        return transaction {
            val roomExists = Rooms.select { Rooms.id eq roomId }.any()
            if (!roomExists) {
                return@transaction MessageResult(failure = MessageFailure.ROOM_NOT_FOUND)
            }
            val inRoom = RoomParticipants.select {
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq userId) and
                    RoomParticipants.leftAt.isNull()
            }.any()
            if (!inRoom) {
                return@transaction MessageResult(failure = MessageFailure.USER_NOT_IN_ROOM)
            }
            val userRow = Users.select { Users.id eq userId }.singleOrNull()
                ?: return@transaction MessageResult(failure = MessageFailure.USER_NOT_FOUND)

            val now = System.currentTimeMillis()
            val messageId = UUID.randomUUID()
            RoomMessages.insert {
                it[id] = messageId
                it[RoomMessages.roomId] = roomId
                it[RoomMessages.userId] = userId
                it[RoomMessages.message] = trimmed
                it[createdAt] = now
            }

            MessageResult(
                response = RoomMessageResponse(
                    id = messageId.toString(),
                    roomId = roomId.toString(),
                    userId = userId.toString(),
                    username = userRow[Users.username],
                    message = trimmed,
                    createdAt = now
                )
            )
        }
    }

    fun listGifts(roomId: UUID, limit: Int = 50): List<GiftSendResponse>? {
        val resolvedLimit = limit.coerceIn(1, 200)
        return transaction {
            val exists = Rooms.select { Rooms.id eq roomId }.any()
            if (!exists) {
                return@transaction null
            }
            GiftTransactions.select { GiftTransactions.roomId eq roomId }
                .orderBy(GiftTransactions.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    GiftSendResponse(
                        id = row[GiftTransactions.id].toString(),
                        roomId = row[GiftTransactions.roomId].toString(),
                        senderId = row[GiftTransactions.senderId].toString(),
                        recipientId = row[GiftTransactions.recipientId]?.toString(),
                        amount = row[GiftTransactions.totalCostCoins],
                        senderBalance = currentBalance(row[GiftTransactions.senderId]),
                        createdAt = row[GiftTransactions.createdAt],
                        giftType = row[GiftTransactions.giftType],
                        diamondsTotal = row[GiftTransactions.diamondsTotal],
                        recipientCount = row[GiftTransactions.recipientCount]
                    )
                }
                .reversed()
        }
    }

    fun sendGift(
        roomId: UUID,
        senderId: UUID,
        recipientId: UUID?,
        amount: Long,
        giftId: UUID?,
        giftTypeOverride: String?
    ): GiftResult {
        if (amount <= 0) {
            return GiftResult(failure = GiftFailure.INVALID_AMOUNT)
        }
        return transaction {
            val roomExists = Rooms.select { Rooms.id eq roomId }.any()
            if (!roomExists) {
                return@transaction GiftResult(failure = GiftFailure.ROOM_NOT_FOUND)
            }
            val senderInRoom = RoomParticipants.select {
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq senderId) and
                    RoomParticipants.leftAt.isNull()
            }.any()
            if (!senderInRoom) {
                return@transaction GiftResult(failure = GiftFailure.USER_NOT_IN_ROOM)
            }
            val giftRow = if (giftId != null) {
                Gifts.select { Gifts.id eq giftId }.singleOrNull()
            } else {
                Gifts.select { Gifts.costCoins eq amount }
                    .orderBy(Gifts.createdAt, SortOrder.DESC)
                    .limit(1)
                    .singleOrNull()
            }
            val giftType = giftTypeOverride
                ?: giftRow?.get(Gifts.giftType)
                ?: if (recipientId != null) "INDIVIDUAL" else "GROUP_FIXED"
            val unitCost = giftRow?.get(Gifts.costCoins) ?: amount
            val diamondPercent = giftRow?.get(Gifts.diamondPercent)
                ?: if (giftType == "GROUP_MULTIPLIER") 10 else 100

            val recipients = if (giftType == "INDIVIDUAL") {
                if (recipientId == null) {
                    return@transaction GiftResult(failure = GiftFailure.RECIPIENT_NOT_FOUND)
                }
                val userExists = Users.select { Users.id eq recipientId }.any()
                if (!userExists) {
                    return@transaction GiftResult(failure = GiftFailure.RECIPIENT_NOT_FOUND)
                }
                val recipientRow = RoomParticipants.select {
                    (RoomParticipants.roomId eq roomId) and
                        (RoomParticipants.userId eq recipientId) and
                        (RoomParticipants.leftAt.isNull()) and
                        RoomParticipants.seatNumber.isNotNull()
                }.singleOrNull() ?: return@transaction GiftResult(failure = GiftFailure.RECIPIENT_NOT_IN_ROOM)
                listOf(recipientRow[RoomParticipants.userId])
            } else {
                RoomParticipants.select {
                    (RoomParticipants.roomId eq roomId) and
                        (RoomParticipants.leftAt.isNull()) and
                        RoomParticipants.seatNumber.isNotNull()
                }
                    .map { it[RoomParticipants.userId] }
                    .filter { it != senderId }
            }

            if (recipients.isEmpty()) {
                return@transaction GiftResult(failure = GiftFailure.RECIPIENT_NOT_IN_ROOM)
            }

            val now = System.currentTimeMillis()
            val totalCost = unitCost * recipients.size
            val senderBalance = try {
                debitCoins(senderId, totalCost, "GIFT")
            } catch (exception: IllegalArgumentException) {
                return@transaction GiftResult(failure = GiftFailure.INSUFFICIENT_BALANCE)
            }

            val diamondsPerRecipient = (unitCost * diamondPercent) / 100L
            val diamondsTotal = diamondsPerRecipient * recipients.size

            val giftTransactionId = UUID.randomUUID()
            GiftTransactions.insert {
                it[id] = giftTransactionId
                it[GiftTransactions.giftId] = giftRow?.get(Gifts.id)
                it[GiftTransactions.roomId] = roomId
                it[GiftTransactions.senderId] = senderId
                it[GiftTransactions.recipientId] = if (giftType == "INDIVIDUAL") recipientId else null
                it[GiftTransactions.giftType] = giftType
                it[GiftTransactions.totalCostCoins] = totalCost
                it[GiftTransactions.diamondsTotal] = diamondsTotal
                it[GiftTransactions.recipientCount] = recipients.size
                it[GiftTransactions.createdAt] = now
            }

            recipients.forEach { recipient ->
                creditDiamonds(recipient, diamondsPerRecipient, giftTransactionId)
                val agencyId = Users.select { Users.id eq recipient }
                    .singleOrNull()
                    ?.get(Users.agencyId)
                    ?: return@forEach
                val agencyRow = Agencies.select { Agencies.id eq agencyId }.singleOrNull()
                    ?: return@forEach
                val commissionUsd = BigDecimal(diamondsPerRecipient).multiply(agencyRow[Agencies.commissionValueUsd])
                    .divide(BigDecimal(agencyRow[Agencies.commissionBlockDiamonds]), 4, java.math.RoundingMode.HALF_UP)
                AgencyCommissions.insert {
                    it[id] = UUID.randomUUID()
                    it[AgencyCommissions.agencyId] = agencyId
                    it[AgencyCommissions.userId] = recipient
                    it[AgencyCommissions.giftTransactionId] = giftTransactionId
                    it[AgencyCommissions.diamondsAmount] = diamondsPerRecipient
                    it[AgencyCommissions.commissionUsd] = commissionUsd
                    it[createdAt] = now
                }
            }

            GiftResult(
                response = GiftSendResponse(
                    id = giftTransactionId.toString(),
                    roomId = roomId.toString(),
                    senderId = senderId.toString(),
                    recipientId = recipientId?.toString(),
                    amount = totalCost,
                    senderBalance = senderBalance,
                    createdAt = now,
                    giftType = giftType,
                    diamondsTotal = diamondsTotal,
                    recipientCount = recipients.size
                )
            )
        }
    }

    private fun debitCoins(userId: UUID, amount: Long, typeOverride: String): Long {
        val now = System.currentTimeMillis()
        val row = CoinWallets.select { CoinWallets.userId eq userId }.singleOrNull()
        val currentBalance = row?.get(CoinWallets.balance) ?: 0L
        val newBalance = currentBalance - amount
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

        CoinTransactions.insert {
            it[id] = UUID.randomUUID()
            it[CoinTransactions.userId] = userId
            it[CoinTransactions.type] = typeOverride
            it[CoinTransactions.amount] = abs(amount)
            it[balanceAfter] = newBalance
            it[createdAt] = now
        }

        return newBalance
    }

    private fun creditDiamonds(userId: UUID, amount: Long, giftTransactionId: UUID) {
        val now = System.currentTimeMillis()
        val wallet = DiamondWallets.select { DiamondWallets.userId eq userId }.singleOrNull()
        val currentBalance = wallet?.get(DiamondWallets.balance) ?: 0L
        val currentLocked = wallet?.get(DiamondWallets.locked) ?: 0L
        val newBalance = currentBalance + amount
        if (wallet == null) {
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
            it[type] = "GIFT"
            it[DiamondTransactions.amount] = amount
            it[balanceAfter] = newBalance
            it[createdAt] = now
        }
    }

    private fun currentBalance(userId: UUID): Long {
        return CoinWallets.select { CoinWallets.userId eq userId }
            .singleOrNull()
            ?.get(CoinWallets.balance)
            ?: 0L
    }
}
