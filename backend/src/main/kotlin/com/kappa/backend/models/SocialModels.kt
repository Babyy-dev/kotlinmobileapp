package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class InboxThreadResponse(
    val id: String,
    val peerId: String,
    val peerName: String,
    val lastMessage: String?,
    val updatedAt: Long,
    val unreadCount: Int = 0
)

@Serializable
data class InboxMessageRequest(
    val recipientId: String,
    val message: String
)

@Serializable
data class InboxMessageResponse(
    val id: String,
    val threadId: String,
    val senderId: String,
    val recipientId: String,
    val message: String,
    val createdAt: Long
)

@Serializable
data class FriendResponse(
    val userId: String,
    val username: String,
    val nickname: String?
)

@Serializable
data class FamilyCreateRequest(
    val name: String
)

@Serializable
data class FamilyJoinRequest(
    val code: String
)

@Serializable
data class FamilyResponse(
    val id: String,
    val name: String,
    val code: String,
    val ownerId: String
)

@Serializable
data class FamilyMemberResponse(
    val userId: String,
    val username: String,
    val nickname: String?,
    val role: String
)

@Serializable
data class FamilyRoomResponse(
    val id: String,
    val name: String,
    val status: String
)
