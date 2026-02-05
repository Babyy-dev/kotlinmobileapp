package com.kappa.app.game.data

import com.kappa.app.game.presentation.GameAction
import com.kappa.app.game.presentation.GamePlayer
import com.kappa.app.game.presentation.GameSessionEvent
import com.kappa.app.game.presentation.GameType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import java.util.UUID

class LocalGameEngine(
    private val scope: CoroutineScope
) {
    private val events = MutableSharedFlow<GameSessionEvent>(extraBufferCapacity = 64)
    private val players = mutableMapOf<String, GamePlayer>()
    private var sessionId: String = ""
    private var roomId: String = ""
    private var gameId: String = ""
    private var pot: Long = 0
    private var timeLeft: Int = 30
    private var gameType: GameType = GameType.LUCKY_DRAW
    private var running = false

    fun eventsFlow() = events

    fun join(roomId: String, gameId: String, userId: String, entryFee: Long, type: GameType, balance: Long) {
        this.roomId = roomId
        this.gameId = gameId
        this.gameType = type
        sessionId = UUID.randomUUID().toString()
        pot += entryFee
        players[userId] = GamePlayer(userId, userId.take(8), 0)
        events.tryEmit(GameSessionEvent.Joined(sessionId = sessionId))
        if (!running) {
            startLoop()
        }
    }

    fun action(action: GameAction) {
        if (!running) {
            return
        }
        val player = players.values.firstOrNull() ?: return
        val updated = when (gameType) {
            GameType.LUCKY_DRAW -> player.copy(score = (1..100).random())
            GameType.BATTLE -> player.copy(score = player.score + 5)
            GameType.GIFT_RUSH -> player.copy(score = player.score + 10)
            GameType.TAP_SPEED -> player.copy(score = player.score + 1)
        }
        players[player.id] = updated
        events.tryEmit(
            GameSessionEvent.State(
                roomId = roomId,
                phase = "running",
                players = players.values.toList(),
                updatedAt = System.currentTimeMillis(),
                timeLeft = timeLeft,
                pot = pot
            )
        )
    }

    private fun startLoop() {
        running = true
        scope.launch(Dispatchers.Default) {
            timeLeft = 30
            while (timeLeft >= 0) {
                events.emit(
                    GameSessionEvent.State(
                        roomId = roomId,
                        phase = "running",
                        players = players.values.toList(),
                        updatedAt = System.currentTimeMillis(),
                        timeLeft = timeLeft,
                        pot = pot
                    )
                )
                delay(1000)
                timeLeft -= 1
            }
            running = false
            val sorted = players.values.sortedByDescending { it.score }
            val winners = sorted.take(3)
            val rewards = mutableMapOf<String, Long>()
            if (winners.isNotEmpty()) {
                val rewardPool = (pot * 0.8).toLong()
                val perWinner = rewardPool / winners.size
                winners.forEach { rewards[it.id] = perWinner }
            }
            val rewardTotal = rewards.values.sum()
            events.emit(
                GameSessionEvent.Result(
                    roomId = roomId,
                    status = "ended",
                    reward = rewardTotal,
                    balance = null
                )
            )
            players.clear()
            pot = 0
        }
    }
}
