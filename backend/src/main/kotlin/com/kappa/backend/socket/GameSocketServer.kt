package com.kappa.backend.socket

import com.corundumstudio.socketio.AckRequest
import com.corundumstudio.socketio.Configuration
import com.corundumstudio.socketio.SocketIOClient
import com.corundumstudio.socketio.SocketIOServer
import java.time.Instant
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList
import com.kappa.backend.services.EconomyService
import com.kappa.backend.services.GameSessionRegistry

data class GameJoinPayload(
    val roomId: String? = null,
    val userId: String? = null,
    val sessionId: String? = null
)

data class GameActionPayload(
    val roomId: String? = null,
    val userId: String? = null,
    val action: String? = null,
    val payload: Map<String, Any?>? = null,
    val clientTs: Long? = null
)

data class GiftPlayPayload(
    val roomId: String? = null,
    val userId: String? = null,
    val sessionId: String? = null,
    val giftId: String? = null,
    val quantity: Int? = null
)

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

data class ValidAction(
    val roomId: String,
    val userId: String,
    val action: String,
    val payload: Map<String, Any?>?
)

class GameSocketServer(
    host: String,
    port: Int,
    private val sessionRegistry: GameSessionRegistry,
    private val economyService: EconomyService
) {
    private val server: SocketIOServer
    private val sessions = ConcurrentHashMap<UUID, SessionInfo>()
    private val roomStates = ConcurrentHashMap<String, GameState>()

    init {
        val config = Configuration().apply {
            hostname = host
            this.port = port
            origin = "*"
        }
        server = SocketIOServer(config)
        registerHandlers()
    }

    fun start() {
        server.start()
    }

    fun stop() {
        server.stop()
    }

    private fun registerHandlers() {
        server.addConnectListener { client ->
            client.sendEvent("connected", mapOf("sessionId" to client.sessionId.toString()))
        }

        server.addDisconnectListener { client ->
            val info = sessions.remove(client.sessionId)
            info?.let { session ->
                val state = roomStates[session.roomId]
                state?.players?.remove(session.userId)
                emitState(session.roomId)
            }
        }

        server.addEventListener("join", GameJoinPayload::class.java) { client, data, ack ->
            val roomId = data.roomId?.trim().orEmpty()
            val userId = data.userId?.trim().orEmpty()
            val sessionId = data.sessionId?.trim().orEmpty()
            if (roomId.isBlank() || userId.isBlank()) {
                emitError(client, ack, "join_invalid", "roomId and userId are required")
                return@addEventListener
            }
            if (sessionId.isBlank() || !sessionRegistry.validateAndConsume(sessionId, roomId, userId)) {
                emitError(client, ack, "join_invalid", "sessionId is invalid or expired")
                return@addEventListener
            }
            sessions[client.sessionId] = SessionInfo(roomId, userId, sessionId)
            client.joinRoom(roomId)

            val state = roomStates.computeIfAbsent(roomId) { GameState(roomId = roomId) }
            if (!state.players.contains(userId)) {
                state.players.add(userId)
            }
            state.updatedAt = Instant.now().toEpochMilli()

            ack?.sendAckData(
                mapOf(
                    "status" to "ok",
                    "roomId" to roomId,
                    "userId" to userId,
                    "sessionId" to sessionId,
                    "state" to state
                )
            )
            emitState(roomId)
        }

        server.addEventListener("start", GameActionPayload::class.java) { client, data, ack ->
            handleAction(client, data, ack, allowedActions = setOf("start")) { action ->
                val roomId = action.roomId
                val state = roomStates.computeIfAbsent(roomId) { GameState(roomId = roomId) }
                state.phase = "started"
                state.updatedAt = Instant.now().toEpochMilli()
                emitState(roomId)
            }
        }

        server.addEventListener("action", GameActionPayload::class.java) { client, data, ack ->
            handleAction(client, data, ack, allowedActions = null) { action ->
                emitState(action.roomId, mapOf("lastAction" to action.action, "payload" to action.payload))
            }
        }

        server.addEventListener("end", GameActionPayload::class.java) { client, data, ack ->
            handleAction(client, data, ack, allowedActions = setOf("end")) { action ->
                val roomId = action.roomId
                val state = roomStates.computeIfAbsent(roomId) { GameState(roomId = roomId) }
                state.phase = "ended"
                state.updatedAt = Instant.now().toEpochMilli()
                emitState(roomId)
            }
        }

        server.addEventListener("reward", GameActionPayload::class.java) { client, data, ack ->
            handleAction(client, data, ack, allowedActions = setOf("reward")) { action ->
                client.sendEvent(
                    "reward",
                    mapOf("roomId" to action.roomId, "userId" to action.userId, "status" to "queued")
                )
            }
        }

        server.addEventListener("gift_play", GiftPlayPayload::class.java) { client, data, ack ->
            val roomId = data.roomId?.trim().orEmpty()
            val userId = data.userId?.trim().orEmpty()
            val giftId = data.giftId?.trim().orEmpty()
            val quantity = data.quantity ?: 0
            if (roomId.isBlank() || userId.isBlank() || giftId.isBlank() || quantity <= 0) {
                emitError(client, ack, "gift_invalid", "roomId, userId, giftId and quantity are required")
                return@addEventListener
            }
            val session = sessions[client.sessionId]
            if (session == null || session.roomId != roomId || session.userId != userId) {
                emitError(client, ack, "session_invalid", "session not joined or mismatched")
                return@addEventListener
            }
            val giftUuid = runCatching { UUID.fromString(giftId) }.getOrNull()
            if (giftUuid == null) {
                emitError(client, ack, "gift_invalid", "giftId is invalid")
                return@addEventListener
            }
            val cost = economyService.getGiftCost(giftUuid)
            if (cost == null) {
                emitError(client, ack, "gift_invalid", "gift not found")
                return@addEventListener
            }
            val totalCost = cost * quantity.toLong()
            runCatching { economyService.debitCoins(UUID.fromString(userId), totalCost) }
                .onFailure {
                    emitError(client, ack, "insufficient_balance", "Insufficient balance")
                    return@addEventListener
                }
            emitState(roomId, mapOf("gift" to mapOf("giftId" to giftId, "quantity" to quantity)))
            ack?.sendAckData(mapOf("status" to "ok"))
        }
    }

    private fun handleAction(
        client: SocketIOClient,
        data: GameActionPayload,
        ack: AckRequest?,
        allowedActions: Set<String>?,
        onOk: (ValidAction) -> Unit
    ) {
        val roomId = data.roomId?.trim().orEmpty()
        val userId = data.userId?.trim().orEmpty()
        val action = data.action?.trim().orEmpty()
        if (roomId.isBlank() || userId.isBlank() || action.isBlank()) {
            emitError(client, ack, "action_invalid", "roomId, userId and action are required")
            return
        }
        if (allowedActions != null && action !in allowedActions) {
            emitError(client, ack, "action_rejected", "action not allowed")
            return
        }
        val session = sessions[client.sessionId]
        if (session == null || session.roomId != roomId || session.userId != userId) {
            emitError(client, ack, "session_invalid", "session not joined or mismatched")
            return
        }
        val now = Instant.now().toEpochMilli()
        if (now - session.lastActionAt < 250) {
            emitError(client, ack, "rate_limited", "actions too fast")
            return
        }
        session.lastActionAt = now
        ack?.sendAckData(mapOf("status" to "ok"))
        onOk(ValidAction(roomId = roomId, userId = userId, action = action, payload = data.payload))
    }

    private fun emitState(roomId: String, extra: Map<String, Any?> = emptyMap()) {
        val state = roomStates.computeIfAbsent(roomId) { GameState(roomId = roomId) }
        val payload = mutableMapOf<String, Any?>(
            "roomId" to roomId,
            "phase" to state.phase,
            "players" to state.players,
            "updatedAt" to state.updatedAt
        )
        payload.putAll(extra)
        server.getRoomOperations(roomId).sendEvent("state_update", payload)
    }

    private fun emitError(client: SocketIOClient, ack: AckRequest?, code: String, message: String) {
        val payload = mapOf("status" to "error", "code" to code, "message" to message)
        ack?.sendAckData(payload)
        client.sendEvent("error", payload)
    }
}
