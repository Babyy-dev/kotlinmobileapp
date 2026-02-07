package com.kappa.app.core.network.model

data class GameSessionRequest(
    val roomId: String
)

data class GameSessionResponse(
    val roomId: String,
    val userId: String,
    val sessionId: String,
    val expiresAt: Long
)

data class GameJoinRequest(
    val roomId: String,
    val userId: String,
    val sessionId: String
)

data class GameActionRequest(
    val roomId: String,
    val userId: String,
    val sessionId: String,
    val action: String,
    val payload: Map<String, String?>? = null
)

data class GameGiftPlayRequest(
    val roomId: String,
    val userId: String,
    val sessionId: String,
    val giftId: String,
    val quantity: Int
)

data class GameActionResponse(
    val status: String,
    val message: String? = null
)

data class GameJoinResponse(
    val status: String,
    val sessionId: String? = null,
    val message: String? = null
)
