package com.kappa.app.domain.audio

data class RoomMessage(
    val id: String,
    val userId: String,
    val username: String,
    val message: String,
    val createdAt: Long,
    val messageType: String = "CHAT"
)

data class GiftLog(
    val id: String,
    val senderId: String,
    val recipientId: String?,
    val amount: Long,
    val senderBalance: Long,
    val createdAt: Long
)
