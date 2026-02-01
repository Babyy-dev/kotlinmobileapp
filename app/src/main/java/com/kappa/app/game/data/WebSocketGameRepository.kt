package com.kappa.app.game.data

import com.google.gson.Gson
import com.kappa.app.BuildConfig
import com.kappa.app.game.presentation.GameAction
import com.kappa.app.game.presentation.GameJoinRequest
import com.kappa.app.game.presentation.GameWsEnvelope
import com.kappa.app.game.presentation.GameSessionEvent
import com.kappa.app.game.presentation.GameType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import timber.log.Timber

class WebSocketGameRepository(
    private val okHttpClient: OkHttpClient,
    private val gson: Gson,
    private val scope: CoroutineScope
) : GameRepository {

    private var webSocket: WebSocket? = null
    private val localEngine = LocalGameEngine(scope)
    private val useLocal = BuildConfig.GAME_WS_URL.isBlank()

    override fun connect(): Flow<GameSessionEvent> {
        return if (useLocal) {
            localEngine.eventsFlow()
        } else {
            callbackFlow {
                val request = Request.Builder()
                    .url(BuildConfig.GAME_WS_URL)
                    .build()
                webSocket = okHttpClient.newWebSocket(request, object : WebSocketListener() {
                    override fun onOpen(webSocket: WebSocket, response: Response) {
                        Timber.d("Game WS connected")
                    }

                    override fun onMessage(webSocket: WebSocket, text: String) {
                        parseEnvelope(text)?.let { event -> trySend(event) }
                    }

                    override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                        trySend(GameSessionEvent.Error("Connection error: ${t.message}"))
                    }
                })
                awaitClose { webSocket?.close(1000, "closed") }
            }
        }
    }

    override fun joinGame(roomId: String, gameId: String, userId: String, entryFee: Long, type: GameType, balance: Long) {
        if (useLocal) {
            localEngine.join(roomId, gameId, userId, entryFee, type, balance)
            return
        }
        val payload = GameJoinRequest(roomId, gameId, userId, entryFee)
        val envelope = GameWsEnvelope(type = "join", payload = payload)
        webSocket?.send(gson.toJson(envelope))
    }

    override fun sendAction(action: GameAction) {
        if (useLocal) {
            localEngine.action(action)
            return
        }
        val envelope = GameWsEnvelope(type = "action", payload = action)
        webSocket?.send(gson.toJson(envelope))
    }

    override fun leaveGame(gameId: String, sessionId: String) {
        if (!useLocal) {
            webSocket?.close(1000, "leave")
        }
    }

    override fun disconnect() {
        webSocket?.close(1000, "disconnect")
        webSocket = null
    }

    private fun parseEnvelope(text: String): GameSessionEvent? {
        return runCatching {
            val base = gson.fromJson(text, Map::class.java)
            val type = base["type"] as? String ?: return null
            val payload = gson.toJson(base["payload"])
            when (type) {
                "join" -> gson.fromJson(payload, GameSessionEvent.Joined::class.java)
                "state_update" -> gson.fromJson(payload, GameSessionEvent.State::class.java)
                "end" -> gson.fromJson(payload, GameSessionEvent.Result::class.java)
                "reward" -> gson.fromJson(payload, GameSessionEvent.Result::class.java)
                "error" -> gson.fromJson(payload, GameSessionEvent.Error::class.java)
                else -> null
            }
        }.getOrNull()
    }
}
