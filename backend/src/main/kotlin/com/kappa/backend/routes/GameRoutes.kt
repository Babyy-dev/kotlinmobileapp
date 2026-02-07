package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.GameSessionRequest
import com.kappa.backend.models.GameSessionResponse
import com.kappa.backend.services.GameSessionRegistry
import com.kappa.backend.services.GameRealtimeService
import com.kappa.backend.models.GameJoinRequest
import com.kappa.backend.models.GameActionRequest
import com.kappa.backend.models.GameGiftPlayRequest
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
    sessionRegistry: GameSessionRegistry,
    gameRealtimeService: GameRealtimeService
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

    post("games/join") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receiveOrNull<GameJoinRequest>()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Missing payload")
            )
        if (request.userId != userId) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "User mismatch"))
            return@post
        }
        val response = gameRealtimeService.join(request)
        call.respond(ApiResponse(success = response.status == "ok", data = response, error = response.message))
    }

    post("games/action") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receiveOrNull<GameActionRequest>()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Missing payload")
            )
        if (request.userId != userId) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "User mismatch"))
            return@post
        }
        val response = gameRealtimeService.action(request)
        call.respond(ApiResponse(success = response.status == "ok", data = response, error = response.message))
    }

    post("games/gift-play") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receiveOrNull<GameGiftPlayRequest>()
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Missing payload")
            )
        if (request.userId != userId) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "User mismatch"))
            return@post
        }
        val response = gameRealtimeService.giftPlay(request)
        call.respond(ApiResponse(success = response.status == "ok", data = response, error = response.message))
    }
}
