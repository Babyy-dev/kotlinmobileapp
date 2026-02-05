package com.kappa.app.core.network.model

data class InboxThreadDto(
    val id: String,
    val peerId: String,
    val peerName: String,
    val lastMessage: String?,
    val updatedAt: Long,
    val unreadCount: Int = 0
)

data class InboxMessageRequest(
    val recipientId: String,
    val message: String
)

data class InboxMessageResponse(
    val id: String,
    val threadId: String,
    val senderId: String,
    val recipientId: String,
    val message: String,
    val createdAt: Long
)

data class FriendDto(
    val userId: String,
    val username: String,
    val nickname: String?
)

data class FamilyCreateRequest(
    val name: String
)

data class FamilyJoinRequest(
    val code: String
)

data class FamilyDto(
    val id: String,
    val name: String,
    val code: String,
    val ownerId: String
)

data class FamilyMemberDto(
    val userId: String,
    val username: String,
    val nickname: String?,
    val role: String
)

data class FamilyRoomDto(
    val id: String,
    val name: String,
    val status: String
)
