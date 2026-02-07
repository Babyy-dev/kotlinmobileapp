package com.kappa.backend.services

import com.kappa.backend.config.AppConfig
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URL
import java.util.Base64

@Serializable
data class LiveKitSendDataRequest(
    val room: String,
    val data: String,
    val kind: String = "RELIABLE",
    val destinationIdentities: List<String>? = null,
    val topic: String? = null
)

class LiveKitRoomService(
    private val config: AppConfig,
    private val tokenService: LiveKitTokenService
) {
    private val logger = LoggerFactory.getLogger(LiveKitRoomService::class.java)
    private val json = Json { encodeDefaults = false; ignoreUnknownKeys = true }

    fun sendData(roomName: String, payload: ByteArray, topic: String? = null, identities: List<String>? = null): Boolean {
        if (roomName.isBlank()) return false
        val token = tokenService.generateServerToken(roomName)
        val request = LiveKitSendDataRequest(
            room = roomName,
            data = Base64.getEncoder().encodeToString(payload),
            kind = "RELIABLE",
            destinationIdentities = identities,
            topic = topic
        )
        val body = json.encodeToString(request)
        val url = URL("${liveKitApiBase()}/twirp/livekit.RoomService/SendData")
        val connection = (url.openConnection() as HttpURLConnection).apply {
            requestMethod = "POST"
            doOutput = true
            setRequestProperty("Content-Type", "application/json")
            setRequestProperty("Authorization", "Bearer $token")
        }

        return try {
            connection.outputStream.use { it.write(body.toByteArray(Charsets.UTF_8)) }
            val responseCode = connection.responseCode
            if (responseCode !in 200..299) {
                logger.warn("LiveKit sendData failed: {} {}", responseCode, connection.responseMessage)
                false
            } else {
                true
            }
        } catch (ex: Exception) {
            logger.error("LiveKit sendData error", ex)
            false
        } finally {
            connection.disconnect()
        }
    }

    private fun liveKitApiBase(): String {
        val raw = config.livekitUrl.trim()
        val http = when {
            raw.startsWith("wss://") -> "https://${raw.removePrefix("wss://")}"
            raw.startsWith("ws://") -> "http://${raw.removePrefix("ws://")}"
            raw.startsWith("https://") || raw.startsWith("http://") -> raw
            else -> "https://$raw"
        }
        return http.trimEnd('/')
    }
}
