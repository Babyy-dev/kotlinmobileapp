package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.SeatMode
import com.kappa.backend.services.AuthService
import com.kappa.backend.services.RoomService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveParameters
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

fun Route.roomRoutes(roomService: RoomService, authService: AuthService) {
    get("rooms") {
        val rooms = roomService.listRooms()
        call.respond(ApiResponse(success = true, data = rooms))
    }

    post("rooms") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role !in setOf("MASTER", "RESELLER", "AGENCY_OWNER")) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val params = call.receiveParameters()
        val name = params["name"] ?: "New Room"
        val seatMode = params["seatMode"] ?: SeatMode.FREE.name
        val created = roomService.createRoom(name, seatMode)
        call.respond(ApiResponse(success = true, data = created))
    }

    post("rooms/{id}/join") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val user = authService.getUserById(UUID.fromString(userId))
            ?: return@post call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "User not found"))

        val response = roomService.joinRoom(UUID.fromString(roomId), UUID.fromString(userId), user.username)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Room not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("rooms/{id}/leave") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val left = roomService.leaveRoom(UUID.fromString(roomId), UUID.fromString(userId))
        if (!left) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Not in room"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Left room"))
    }
}
