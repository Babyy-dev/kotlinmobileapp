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
