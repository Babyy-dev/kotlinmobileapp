package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class GameSessionRequest(
    val roomId: String
)

@Serializable
data class GameSessionResponse(
    val roomId: String,
    val userId: String,
    val sessionId: String,
    val expiresAt: Long
)

@Serializable
data class GameJoinRequest(
    val roomId: String,
    val userId: String,
    val sessionId: String
)

@Serializable
data class GameJoinResponse(
    val status: String,
    val sessionId: String? = null,
    val state: GameStatePayload? = null,
    val message: String? = null
)

@Serializable
data class GameActionRequest(
    val roomId: String,
    val userId: String,
    val sessionId: String,
    val action: String,
    val payload: Map<String, String?>? = null
)

@Serializable
data class GameGiftPlayRequest(
    val roomId: String,
    val userId: String,
    val sessionId: String,
    val giftId: String,
    val quantity: Int
)

@Serializable
data class GameActionResponse(
    val status: String,
    val message: String? = null
)

@Serializable
data class GameGiftPayload(
    val giftId: String,
    val quantity: Int
)

@Serializable
data class GameRewardPayload(
    val roomId: String,
    val userId: String,
    val status: String,
    val reward: Long? = null,
    val balance: Long? = null
)

@Serializable
data class GameStatePayload(
    val roomId: String,
    val phase: String,
    val players: List<String>,
    val updatedAt: Long,
    val timeLeft: Int = 0,
    val pot: Long = 0,
    val lastAction: String? = null,
    val payload: kotlinx.serialization.json.JsonObject? = null,
    val gift: GameGiftPayload? = null
)

@Serializable
data class GameEventEnvelope<T>(
    val type: String,
    val payload: T
)
