package com.kappa.backend.services

import com.kappa.backend.data.CoinTransactions
import com.kappa.backend.data.CoinWallets
import com.kappa.backend.data.RoomGifts
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
            RoomGifts.select { RoomGifts.roomId eq roomId }
                .orderBy(RoomGifts.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    GiftSendResponse(
                        id = row[RoomGifts.id].toString(),
                        roomId = row[RoomGifts.roomId].toString(),
                        senderId = row[RoomGifts.senderId].toString(),
                        recipientId = row[RoomGifts.recipientId]?.toString(),
                        amount = row[RoomGifts.amountCoins],
                        senderBalance = currentBalance(row[RoomGifts.senderId]),
                        createdAt = row[RoomGifts.createdAt]
                    )
                }
                .reversed()
        }
    }

    fun sendGift(roomId: UUID, senderId: UUID, recipientId: UUID?, amount: Long): GiftResult {
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
            if (recipientId != null) {
                val userExists = Users.select { Users.id eq recipientId }.any()
                if (!userExists) {
                    return@transaction GiftResult(failure = GiftFailure.RECIPIENT_NOT_FOUND)
                }
                val recipientInRoom = RoomParticipants.select {
                    (RoomParticipants.roomId eq roomId) and
                        (RoomParticipants.userId eq recipientId) and
                        RoomParticipants.leftAt.isNull()
                }.any()
                if (!recipientInRoom) {
                    return@transaction GiftResult(failure = GiftFailure.RECIPIENT_NOT_IN_ROOM)
                }
            }

            val now = System.currentTimeMillis()
            val senderBalance = try {
                debitCoins(senderId, amount)
            } catch (exception: IllegalArgumentException) {
                return@transaction GiftResult(failure = GiftFailure.INSUFFICIENT_BALANCE)
            }

            val giftId = UUID.randomUUID()
            RoomGifts.insert {
                it[id] = giftId
                it[RoomGifts.roomId] = roomId
                it[RoomGifts.senderId] = senderId
                it[RoomGifts.recipientId] = recipientId
                it[RoomGifts.amountCoins] = amount
                it[createdAt] = now
            }

            GiftResult(
                response = GiftSendResponse(
                    id = giftId.toString(),
                    roomId = roomId.toString(),
                    senderId = senderId.toString(),
                    recipientId = recipientId?.toString(),
                    amount = amount,
                    senderBalance = senderBalance,
                    createdAt = now
                )
            )
        }
    }

    private fun debitCoins(userId: UUID, amount: Long): Long {
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
            it[CoinTransactions.type] = "DEBIT"
            it[CoinTransactions.amount] = abs(amount)
            it[balanceAfter] = newBalance
            it[createdAt] = now
        }

        return newBalance
    }

    private fun currentBalance(userId: UUID): Long {
        return CoinWallets.select { CoinWallets.userId eq userId }
            .singleOrNull()
            ?.get(CoinWallets.balance)
            ?: 0L
    }
}
