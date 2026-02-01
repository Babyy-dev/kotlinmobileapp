package com.kappa.app.game.presentation

data class GameJoinRequest(
    val roomId: String,
    val gameId: String,
    val userId: String,
    val entryFee: Long
)

data class GameAction(
    val roomId: String,
    val gameId: String,
    val sessionId: String,
    val actionType: String,
    val timestamp: Long
)

data class GameWsEnvelope<T>(
    val type: String,
    val payload: T
)

sealed class GameSessionEvent {
    data class Joined(
        val sessionId: String,
        val balance: Long
    ) : GameSessionEvent()

    data class State(
        val players: List<GamePlayer>,
        val timeLeft: Int,
        val pot: Long
    ) : GameSessionEvent()

    data class Result(
        val winners: List<GamePlayer>,
        val rewards: Map<String, Long>
    ) : GameSessionEvent()

    data class Error(val message: String) : GameSessionEvent()
}
