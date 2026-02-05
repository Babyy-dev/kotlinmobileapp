package com.kappa.app.game.presentation

data class GameJoinRequest(
    val roomId: String,
    val userId: String,
    val sessionId: String
)

data class GameAction(
    val roomId: String,
    val userId: String,
    val action: String,
    val payload: Map<String, Any?>? = null
)

data class GameWsEnvelope<T>(
    val type: String,
    val payload: T
)

sealed class GameSessionEvent {
    data class Joined(
        val sessionId: String
    ) : GameSessionEvent()

    data class State(
        val roomId: String,
        val phase: String,
        val players: List<GamePlayer>,
        val updatedAt: Long,
        val timeLeft: Int = 0,
        val pot: Long = 0
    ) : GameSessionEvent()

    data class Result(
        val roomId: String,
        val status: String,
        val reward: Long? = null,
        val balance: Long? = null
    ) : GameSessionEvent()

    data class Error(val message: String) : GameSessionEvent()
}
