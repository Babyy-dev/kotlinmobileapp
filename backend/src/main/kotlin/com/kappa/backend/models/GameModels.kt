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
