package com.kappa.app.game.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.game.data.GameRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class GameViewState(
    val gameId: String = "",
    val title: String = "",
    val type: GameType = GameType.LUCKY_DRAW,
    val userId: String = "",
    val roomId: String = "",
    val sessionId: String = "",
    val balance: Long = 5000,
    val pot: Long = 0,
    val timeLeft: Int = 0,
    val phase: String = "lobby",
    val gameStatus: String = "",
    val players: List<GamePlayer> = emptyList(),
    val isJoined: Boolean = false,
    val message: String? = null
)

@HiltViewModel
class GameViewModel @Inject constructor(
    private val repository: GameRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(GameViewState())
    val viewState: StateFlow<GameViewState> = _viewState.asStateFlow()

    private var eventJob: Job? = null
    private var lastActionWindowStart: Long = 0
    private var actionCount: Int = 0

    init {
        eventJob = viewModelScope.launch {
            repository.connect().collect { event ->
                when (event) {
                    is GameSessionEvent.Joined -> {
                        _viewState.value = _viewState.value.copy(
                            sessionId = event.sessionId,
                            isJoined = true,
                            message = "Joined game"
                        )
                    }
                    is GameSessionEvent.State -> {
                        _viewState.value = _viewState.value.copy(
                            players = event.players,
                            timeLeft = if (event.timeLeft > 0) event.timeLeft else computeCountdown(event.updatedAt),
                            phase = event.phase,
                            pot = if (event.pot > 0) event.pot else event.players.size.toLong()
                        )
                    }
                    is GameSessionEvent.Result -> {
                        _viewState.value = _viewState.value.copy(
                            gameStatus = event.status,
                            balance = event.balance ?: _viewState.value.balance,
                            message = event.reward?.let { "Reward: $it" } ?: "Game finished"
                        )
                    }
                    is GameSessionEvent.Error -> {
                        _viewState.value = _viewState.value.copy(message = event.message)
                    }
                }
            }
        }
    }

    fun configure(gameId: String, title: String, type: GameType, balance: Long, userId: String, roomId: String) {
        _viewState.value = _viewState.value.copy(
            gameId = gameId,
            title = title,
            type = type,
            balance = balance,
            userId = userId,
            roomId = roomId
        )
    }

    fun join(entryFee: Long) {
        val state = _viewState.value
        if (state.balance < entryFee) {
            _viewState.value = state.copy(message = "Not enough coins")
            return
        }
        repository.joinGame(state.roomId, state.gameId, state.userId, entryFee, state.type, state.balance)
    }

    fun sendAction(actionType: String) {
        val state = _viewState.value
        if (!state.isJoined || state.sessionId.isBlank()) {
            _viewState.value = state.copy(message = "Join game first")
            return
        }
        if (!allowAction()) {
            _viewState.value = state.copy(message = "Too many actions")
            return
        }
        val action = GameAction(
            roomId = state.roomId,
            userId = state.userId,
            action = actionType,
            payload = mapOf(
                "gameId" to state.gameId,
                "timestamp" to System.currentTimeMillis()
            )
        )
        repository.sendAction(action)
    }

    fun sendGiftPlay(giftId: String, quantity: Int) {
        val state = _viewState.value
        if (!state.isJoined || state.sessionId.isBlank()) {
            _viewState.value = state.copy(message = "Join game first")
            return
        }
        repository.sendGiftPlay(
            roomId = state.roomId,
            userId = state.userId,
            sessionId = state.sessionId,
            giftId = giftId,
            quantity = quantity
        )
    }

    fun setMessage(text: String) {
        _viewState.value = _viewState.value.copy(message = text)
    }

    private fun computeCountdown(updatedAt: Long): Int {
        if (updatedAt <= 0L) return 0
        val elapsed = (System.currentTimeMillis() - updatedAt).coerceAtLeast(0L)
        val remaining = 30 - (elapsed / 1000).toInt()
        return remaining.coerceAtLeast(0)
    }

    private fun allowAction(): Boolean {
        val now = System.currentTimeMillis()
        if (now - lastActionWindowStart > 1000) {
            lastActionWindowStart = now
            actionCount = 0
        }
        actionCount += 1
        return actionCount <= 15
    }

    fun clearMessage() {
        _viewState.value = _viewState.value.copy(message = null)
    }

    override fun onCleared() {
        repository.disconnect()
        eventJob?.cancel()
        super.onCleared()
    }
}
