package com.kappa.backend.services

import com.kappa.backend.models.GameActionRequest
import com.kappa.backend.models.GameActionResponse
import com.kappa.backend.models.GameJoinRequest
import com.kappa.backend.models.GameJoinResponse
import com.kappa.backend.models.GameStatePayload
import com.kappa.backend.models.GameEventEnvelope
import com.kappa.backend.models.GameGiftPlayRequest
import com.kappa.backend.models.GameGiftPayload
import com.kappa.backend.models.GameRewardPayload
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

data class GameState(
    val roomId: String,
    var phase: String = "lobby",
    val players: MutableList<String> = CopyOnWriteArrayList(),
    var updatedAt: Long = Instant.now().toEpochMilli()
)

data class SessionInfo(
    val roomId: String,
    val userId: String,
    val sessionId: String,
    var lastActionAt: Long = 0L
)

class GameRealtimeService(
    private val sessionRegistry: GameSessionRegistry,
    private val economyService: EconomyService,
    private val liveKitRoomService: LiveKitRoomService
) {
    private val logger = LoggerFactory.getLogger(GameRealtimeService::class.java)
    private val sessions = ConcurrentHashMap<String, SessionInfo>()
    private val roomStates = ConcurrentHashMap<String, GameState>()
    private val json = Json { encodeDefaults = false; ignoreUnknownKeys = true }

    fun join(request: GameJoinRequest): GameJoinResponse {
        val roomId = request.roomId.trim()
        val userId = request.userId.trim()
        val sessionId = request.sessionId.trim()
        if (roomId.isBlank() || userId.isBlank() || sessionId.isBlank()) {
            return GameJoinResponse(status = "error", message = "roomId, userId, sessionId required")
        }
        if (!sessionRegistry.validate(sessionId, roomId, userId)) {
            return GameJoinResponse(status = "error", message = "session invalid or expired")
        }
        sessions[sessionId] = SessionInfo(roomId = roomId, userId = userId, sessionId = sessionId)
        val state = roomStates.computeIfAbsent(roomId) { GameState(roomId = roomId) }
        if (!state.players.contains(userId)) {
            state.players.add(userId)
        }
        state.updatedAt = Instant.now().toEpochMilli()
        emitState(state, lastAction = null, payload = null, gift = null)
        return GameJoinResponse(status = "ok", sessionId = sessionId, state = state.toPayload(null, null, null))
    }

    fun action(request: GameActionRequest): GameActionResponse {
        val roomId = request.roomId.trim()
        val userId = request.userId.trim()
        val action = request.action.trim()
        if (roomId.isBlank() || userId.isBlank() || action.isBlank()) {
            return GameActionResponse(status = "error", message = "roomId, userId, action required")
        }
        val session = sessions[request.sessionId] ?: return GameActionResponse(status = "error", message = "session not joined")
        if (session.roomId != roomId || session.userId != userId) {
            return GameActionResponse(status = "error", message = "session mismatch")
        }
        val now = Instant.now().toEpochMilli()
        if (now - session.lastActionAt < 250) {
            return GameActionResponse(status = "error", message = "rate_limited")
        }
        session.lastActionAt = now
        val state = roomStates.computeIfAbsent(roomId) { GameState(roomId = roomId) }
        if (action == "start") {
            state.phase = "started"
        } else if (action == "end") {
            state.phase = "ended"
        } else if (action == "reward") {
            emitReward(roomId, userId, "queued", null, null)
        }
        state.updatedAt = now
        val payloadJson = request.payload?.let { payload ->
            buildJsonObject {
                payload.forEach { (key, value) ->
                    if (value != null) {
                        put(key, value)
                    }
                }
            }
        }
        emitState(state, lastAction = action, payload = payloadJson, gift = null)
        return GameActionResponse(status = "ok")
    }

    fun giftPlay(request: GameGiftPlayRequest): GameActionResponse {
        val roomId = request.roomId.trim()
        val userId = request.userId.trim()
        val giftId = request.giftId.trim()
        val quantity = request.quantity
        if (roomId.isBlank() || userId.isBlank() || giftId.isBlank() || quantity <= 0) {
            return GameActionResponse(status = "error", message = "roomId, userId, giftId, quantity required")
        }
        val session = sessions[request.sessionId] ?: return GameActionResponse(status = "error", message = "session not joined")
        if (session.roomId != roomId || session.userId != userId) {
            return GameActionResponse(status = "error", message = "session mismatch")
        }
        val giftUuid = runCatching { UUID.fromString(giftId) }.getOrNull()
            ?: return GameActionResponse(status = "error", message = "invalid gift id")
        val cost = economyService.getGiftCost(giftUuid) ?: return GameActionResponse(status = "error", message = "gift not found")
        val totalCost = cost * quantity.toLong()
        runCatching { economyService.debitCoins(UUID.fromString(userId), totalCost) }
            .onFailure { return GameActionResponse(status = "error", message = "insufficient_balance") }
        val state = roomStates.computeIfAbsent(roomId) { GameState(roomId = roomId) }
        state.updatedAt = Instant.now().toEpochMilli()
        emitState(
            state,
            lastAction = null,
            payload = null,
            gift = GameGiftPayload(giftId = giftId, quantity = quantity)
        )
        return GameActionResponse(status = "ok")
    }

    private fun emitReward(roomId: String, userId: String, status: String, reward: Long?, balance: Long?) {
        val payload = GameRewardPayload(roomId = roomId, userId = userId, status = status, reward = reward, balance = balance)
        val envelope = GameEventEnvelope(type = "reward", payload = payload)
        val jsonText = json.encodeToString(envelope)
        liveKitRoomService.sendData(roomId, jsonText.toByteArray(Charsets.UTF_8))
    }

    private fun emitState(
        state: GameState,
        lastAction: String?,
        payload: JsonObject?,
        gift: GameGiftPayload?
    ) {
        val payloadModel = state.toPayload(lastAction, payload, gift)
        val envelope = GameEventEnvelope(type = "state_update", payload = payloadModel)
        val jsonText = json.encodeToString(envelope)
        val ok = liveKitRoomService.sendData(state.roomId, jsonText.toByteArray(Charsets.UTF_8))
        if (!ok) {
            logger.warn("LiveKit state_update failed for room {}", state.roomId)
        }
    }

    private fun GameState.toPayload(
        lastAction: String?,
        payload: JsonObject?,
        gift: GameGiftPayload?
    ): GameStatePayload {
        return GameStatePayload(
            roomId = roomId,
            phase = phase,
            players = players.toList(),
            updatedAt = updatedAt,
            lastAction = lastAction,
            payload = payload,
            gift = gift
        )
    }
}
