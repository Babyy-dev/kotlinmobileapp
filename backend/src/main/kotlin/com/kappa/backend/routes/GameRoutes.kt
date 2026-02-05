package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.GameSessionRequest
import com.kappa.backend.models.GameSessionResponse
import com.kappa.backend.services.GameSessionRegistry
import com.kappa.backend.services.RoomService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveOrNull
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import java.util.UUID

fun Route.gameRoutes(
    roomService: RoomService,
    sessionRegistry: GameSessionRegistry
) {
    post("games/session") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receiveOrNull<GameSessionRequest>()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Missing payload")
            )
        val roomId = request.roomId.trim()
        if (roomId.isBlank()) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid room id"))
            return@post
        }
        val roomExists = roomService.listRooms(status = null).any { it.id == roomId }
        if (!roomExists) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Room not found"))
            return@post
        }
        val session = sessionRegistry.create(UUID.fromString(roomId), UUID.fromString(userId))
        call.respond(
            ApiResponse(
                success = true,
                data = GameSessionResponse(
                    roomId = session.roomId,
                    userId = session.userId,
                    sessionId = session.sessionId,
                    expiresAt = session.expiresAt
                )
            )
        )
    }
}
