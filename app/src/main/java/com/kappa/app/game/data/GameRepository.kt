package com.kappa.app.game.data

import com.kappa.app.game.presentation.GameAction
import com.kappa.app.game.presentation.GameSessionEvent
import kotlinx.coroutines.flow.Flow

interface GameRepository {
    fun connect(): Flow<GameSessionEvent>
    fun joinGame(roomId: String, gameId: String, userId: String, entryFee: Long, type: com.kappa.app.game.presentation.GameType, balance: Long)
    fun sendAction(action: GameAction)
    fun sendGiftPlay(roomId: String, userId: String, sessionId: String, giftId: String, quantity: Int)
    fun leaveGame(gameId: String, sessionId: String)
    fun disconnect()
}
