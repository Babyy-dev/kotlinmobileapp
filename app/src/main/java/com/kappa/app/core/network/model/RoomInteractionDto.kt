package com.kappa.app.core.network.model

data class RoomMessageDto(
    val id: String,
    val roomId: String,
    val userId: String,
    val username: String,
    val message: String,
    val createdAt: Long,
    val type: String = "CHAT"
)

data class RoomMessageRequest(
    val message: String,
    val type: String? = null
)

data class GiftSendRequest(
    val recipientId: String? = null,
    val amount: Long,
    val giftId: String? = null,
    val giftType: String? = null,
    val target: String? = null,
    val recipientIds: List<String>? = null
)

data class GiftSendDto(
    val id: String,
    val roomId: String,
    val senderId: String,
    val recipientId: String? = null,
    val amount: Long,
    val senderBalance: Long,
    val createdAt: Long
)
