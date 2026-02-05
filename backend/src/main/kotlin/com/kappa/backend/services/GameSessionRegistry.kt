package com.kappa.backend.services

import java.util.UUID
import java.util.concurrent.ConcurrentHashMap

data class GameSession(
    val sessionId: String,
    val roomId: String,
    val userId: String,
    val expiresAt: Long
)

class GameSessionRegistry(
    private val ttlMillis: Long = 5 * 60 * 1000
) {
    private val sessions = ConcurrentHashMap<String, GameSession>()

    fun create(roomId: UUID, userId: UUID): GameSession {
        val sessionId = UUID.randomUUID().toString()
        val expiresAt = System.currentTimeMillis() + ttlMillis
        val session = GameSession(
            sessionId = sessionId,
            roomId = roomId.toString(),
            userId = userId.toString(),
            expiresAt = expiresAt
        )
        sessions[sessionId] = session
        return session
    }

    fun validateAndConsume(sessionId: String, roomId: String, userId: String): Boolean {
        val session = sessions[sessionId] ?: return false
        if (session.roomId != roomId || session.userId != userId) {
            return false
        }
        if (session.expiresAt < System.currentTimeMillis()) {
            sessions.remove(sessionId)
            return false
        }
        sessions.remove(sessionId)
        return true
    }
}
