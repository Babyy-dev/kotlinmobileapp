package com.kappa.app.game.data

import com.google.gson.Gson
import com.kappa.app.BuildConfig
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.model.GameSessionRequest
import com.kappa.app.game.presentation.GameAction
import com.kappa.app.game.presentation.GameJoinRequest
import com.kappa.app.game.presentation.GamePlayer
import com.kappa.app.game.presentation.GameSessionEvent
import com.kappa.app.game.presentation.GameType
import io.socket.client.Ack
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber
import java.net.URI

class WebSocketGameRepository(
    private val okHttpClient: okhttp3.OkHttpClient,
    private val gson: Gson,
    private val apiService: ApiService,
    private val scope: CoroutineScope
) : GameRepository {

    private var socket: Socket? = null
    private var currentSessionId: String = ""
    private var currentRoomId: String = ""
    private var currentUserId: String = ""
    private var shouldReconnect: Boolean = false
    private var reconnectAttempts: Int = 0
    private val localEngine = LocalGameEngine(scope)
    private val useLocal = BuildConfig.GAME_WS_URL.isBlank()
    private var eventSink: ((GameSessionEvent) -> Unit)? = null

    override fun connect(): Flow<GameSessionEvent> {
        return if (useLocal) {
            localEngine.eventsFlow()
        } else {
            callbackFlow {
                eventSink = { event -> trySend(event) }
                val uri = URI(BuildConfig.GAME_WS_URL)
                val options = IO.Options.builder()
                    .setReconnection(true)
                    .setReconnectionAttempts(5)
                    .setReconnectionDelay(800)
                    .build()
                socket = IO.socket(uri, options)

                val onConnect = Emitter.Listener {
                    Timber.d("Socket.IO connected")
                    if (shouldReconnect && currentRoomId.isNotBlank() && currentUserId.isNotBlank() && currentSessionId.isNotBlank()) {
                        emitJoin(currentRoomId, currentUserId, currentSessionId)
                    }
                }
                val onError = Emitter.Listener { args ->
                    val message = args.firstOrNull()?.toString() ?: "Unknown error"
                    trySend(GameSessionEvent.Error(message))
                }
                val onDisconnect = Emitter.Listener {
                    if (shouldReconnect) {
                        reconnectAttempts += 1
                        trySend(GameSessionEvent.Error("Disconnected. Reconnecting ($reconnectAttempts)..."))
                    }
                }
                val onState = Emitter.Listener { args ->
                    val payload = args.firstOrNull()
                    parseState(payload)?.let { trySend(it) }
                }
                val onReward = Emitter.Listener { args ->
                    val payload = args.firstOrNull() as? JSONObject
                    val roomId = payload?.optString("roomId").orEmpty()
                    val status = payload?.optString("status").orEmpty().ifBlank { "reward" }
                    val reward = payload?.optLong("reward")
                    val balance = payload?.optLong("balance")
                    trySend(GameSessionEvent.Result(roomId = roomId, status = status, reward = reward, balance = balance))
                }

                socket?.on(Socket.EVENT_CONNECT, onConnect)
                socket?.on(Socket.EVENT_DISCONNECT, onDisconnect)
                socket?.on("state_update", onState)
                socket?.on("reward", onReward)
                socket?.on("error", onError)
                socket?.connect()

                awaitClose {
                    eventSink = null
                    socket?.off(Socket.EVENT_CONNECT, onConnect)
                    socket?.off(Socket.EVENT_DISCONNECT, onDisconnect)
                    socket?.off("state_update", onState)
                    socket?.off("reward", onReward)
                    socket?.off("error", onError)
                    socket?.disconnect()
                    socket = null
                }
            }
        }
    }

    override fun joinGame(roomId: String, gameId: String, userId: String, entryFee: Long, type: GameType, balance: Long) {
        if (useLocal) {
            localEngine.join(roomId, gameId, userId, entryFee, type, balance)
            return
        }
        currentRoomId = roomId
        currentUserId = userId
        shouldReconnect = true
        scope.launch(Dispatchers.IO) {
            val response = runCatching { apiService.createGameSession(GameSessionRequest(roomId)) }.getOrNull()
            val sessionId = response?.data?.sessionId
            if (response == null || !response.success || sessionId.isNullOrBlank()) {
                Timber.e("Game session create failed: ${response?.error}")
                shouldReconnect = false
                return@launch
            }
            currentSessionId = sessionId
            emitJoin(roomId, userId, sessionId)
        }
    }

    override fun sendAction(action: GameAction) {
        if (useLocal) {
            localEngine.action(action)
            return
        }
        val payload = JSONObject(
            mapOf(
                "roomId" to action.roomId,
                "userId" to action.userId,
                "action" to action.action,
                "payload" to JSONObject(action.payload ?: emptyMap<String, Any?>())
            )
        )
        socket?.emit("action", payload)
    }

    override fun sendGiftPlay(roomId: String, userId: String, sessionId: String, giftId: String, quantity: Int) {
        if (useLocal) {
            return
        }
        if (roomId.isBlank() || userId.isBlank() || sessionId.isBlank()) {
            return
        }
        val payload = JSONObject(
            mapOf(
                "roomId" to roomId,
                "userId" to userId,
                "sessionId" to sessionId,
                "giftId" to giftId,
                "quantity" to quantity
            )
        )
        socket?.emit("gift_play", payload)
    }

    override fun leaveGame(gameId: String, sessionId: String) {
        if (!useLocal) {
            shouldReconnect = false
            socket?.disconnect()
        }
    }

    override fun disconnect() {
        shouldReconnect = false
        socket?.disconnect()
        socket = null
    }

    private fun emitJoin(roomId: String, userId: String, sessionId: String) {
        val joinPayload = GameJoinRequest(roomId = roomId, userId = userId, sessionId = sessionId)
        val json = JSONObject(gson.toJson(joinPayload))
        val ack = Ack { args ->
            val ackPayload = args.firstOrNull() as? JSONObject
            val status = ackPayload?.optString("status").orEmpty()
            if (status == "ok") {
                eventSink?.invoke(GameSessionEvent.Joined(sessionId))
            } else {
                Timber.e("Join failed: ${ackPayload?.optString("message")}")
            }
        }
        socket?.emit("join", json, ack)
    }

    private fun parseState(payload: Any?): GameSessionEvent.State? {
        val json = payload as? JSONObject ?: return null
        val roomId = json.optString("roomId")
        val phase = json.optString("phase")
        val updatedAt = json.optLong("updatedAt")
        val timeLeft = json.optInt("timeLeft", 0)
        val pot = json.optLong("pot", 0L)
        val playersArray = json.optJSONArray("players") ?: JSONArray()
        val players = mutableListOf<GamePlayer>()
        for (i in 0 until playersArray.length()) {
            val item = playersArray.opt(i)
            when (item) {
                is JSONObject -> {
                    val id = item.optString("id")
                    val name = item.optString("name").ifBlank { id.take(6) }
                    val score = item.optInt("score", 0)
                    if (id.isNotBlank()) {
                        players.add(GamePlayer(id = id, name = name, score = score))
                    }
                }
                else -> {
                    val id = item?.toString().orEmpty()
                    if (id.isNotBlank()) {
                        players.add(GamePlayer(id = id, name = id.take(6), score = 0))
                    }
                }
            }
        }
        return GameSessionEvent.State(
            roomId = roomId,
            phase = phase,
            players = players,
            updatedAt = updatedAt,
            timeLeft = timeLeft,
            pot = pot
        )
    }
}
