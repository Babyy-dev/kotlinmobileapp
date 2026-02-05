package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.GiftSendRequest
import com.kappa.backend.models.JoinRoomRequest
import com.kappa.backend.models.MuteParticipantRequest
import com.kappa.backend.models.RoomMessageRequest
import com.kappa.backend.models.RoomCreateRequest
import com.kappa.backend.models.SeatMode
import com.kappa.backend.services.AuthService
import com.kappa.backend.services.RoomInteractionService
import com.kappa.backend.services.RoomService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receiveParameters
import io.ktor.server.request.receiveOrNull
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

fun Route.roomRoutes(
    roomService: RoomService,
    authService: AuthService,
    roomInteractionService: RoomInteractionService
) {
    get("rooms") {
        val rooms = roomService.listRooms()
        call.respond(ApiResponse(success = true, data = rooms))
    }

    get("admin/rooms") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isAdminRole(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val statusParam = call.request.queryParameters["status"]
        val normalized = statusParam?.trim()?.lowercase()
        val statusFilter = if (normalized.isNullOrBlank() || normalized == "all") null else normalized
        val rooms = roomService.listRooms(statusFilter)
        call.respond(ApiResponse(success = true, data = rooms))
    }

    post("rooms") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val jsonRequest = call.receiveOrNull<RoomCreateRequest>()
        val params = if (jsonRequest == null) call.receiveParameters() else null
        val name = jsonRequest?.name ?: params?.get("name") ?: "New Room"
        val seatMode = jsonRequest?.seatMode?.name ?: params?.get("seatMode") ?: SeatMode.FREE.name
        val maxSeats = jsonRequest?.maxSeats ?: params?.get("maxSeats")?.toIntOrNull() ?: 28
        val password = jsonRequest?.password ?: params?.get("password")
        val country = jsonRequest?.country ?: params?.get("country")
        val created = roomService.createRoom(name, seatMode, maxSeats, password, country)
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

        val request = call.receiveOrNull<JoinRoomRequest>()
        val result = roomService.joinRoom(UUID.fromString(roomId), UUID.fromString(userId), user.username, request?.password)
        if (result.response == null) {
            val (status, message) = when (result.failure) {
                RoomService.JoinRoomFailure.ROOM_NOT_FOUND -> HttpStatusCode.NotFound to "Room not found"
                RoomService.JoinRoomFailure.INVALID_PASSWORD -> HttpStatusCode.Forbidden to "Invalid room password"
                RoomService.JoinRoomFailure.BANNED -> HttpStatusCode.Forbidden to "User is banned from this room"
                null -> HttpStatusCode.BadRequest to "Unable to join room"
            }
            call.respond(status, ApiResponse<Unit>(success = false, error = message))
            return@post
        }
        call.respond(ApiResponse(success = true, data = result.response))
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

    post("rooms/{id}/close") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val closed = roomService.closeRoom(UUID.fromString(roomId))
        if (!closed) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Room not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Room closed"))
    }

    post("rooms/{id}/family/{familyId}") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val familyId = call.parameters["familyId"]
        val familyUuid = familyId?.let { value -> runCatching { UUID.fromString(value) }.getOrNull() }
        val response = roomService.assignFamily(UUID.fromString(roomId), familyUuid)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Room not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    get("rooms/{id}/seats") {
        val roomId = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val seats = roomService.listSeats(UUID.fromString(roomId))
        if (seats == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Room not found"))
            return@get
        }
        call.respond(ApiResponse(success = true, data = seats))
    }

    post("rooms/{id}/seats/{seat}/take") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val seatNumber = call.parameters["seat"]?.toIntOrNull() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Invalid seat number")
        )
        val error = roomService.takeSeat(UUID.fromString(roomId), seatNumber, UUID.fromString(userId))
        if (error != null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = error))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Seat taken"))
    }

    post("rooms/{id}/seats/{seat}/leave") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val seatNumber = call.parameters["seat"]?.toIntOrNull() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Invalid seat number")
        )
        val error = roomService.leaveSeat(UUID.fromString(roomId), seatNumber, UUID.fromString(userId))
        if (error != null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = error))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Seat released"))
    }

    post("rooms/{id}/seats/{seat}/lock") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val seatNumber = call.parameters["seat"]?.toIntOrNull() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Invalid seat number")
        )
        val error = roomService.lockSeat(UUID.fromString(roomId), seatNumber)
        if (error != null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = error))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Seat locked"))
    }

    post("rooms/{id}/seats/{seat}/unlock") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val seatNumber = call.parameters["seat"]?.toIntOrNull() ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Invalid seat number")
        )
        val error = roomService.unlockSeat(UUID.fromString(roomId), seatNumber)
        if (error != null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = error))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Seat unlocked"))
    }

    post("rooms/{id}/participants/{userId}/mute") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val targetId = call.parameters["userId"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing user id")
        )
        val request = call.receiveOrNull<MuteParticipantRequest>()
            ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Missing mute payload"))
        val error = roomService.muteParticipant(UUID.fromString(roomId), UUID.fromString(targetId), request.muted)
        if (error != null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = error))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Participant updated"))
    }

    post("rooms/{id}/participants/{userId}/kick") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val targetId = call.parameters["userId"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing user id")
        )
        val error = roomService.kickParticipant(UUID.fromString(roomId), UUID.fromString(targetId))
        if (error != null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = error))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Participant kicked"))
    }

    post("rooms/{id}/participants/{userId}/ban") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (!isRoomModerator(role)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val targetId = call.parameters["userId"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing user id")
        )
        val error = roomService.banParticipant(UUID.fromString(roomId), UUID.fromString(targetId))
        if (error != null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = error))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Participant banned"))
    }

    get("rooms/{id}/messages") {
        val roomId = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
        val messages = roomInteractionService.listMessages(UUID.fromString(roomId), limit)
        if (messages == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Room not found"))
            return@get
        }
        call.respond(ApiResponse(success = true, data = messages))
    }

    post("rooms/{id}/messages") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val request = call.receiveOrNull<RoomMessageRequest>()
            ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Missing message"))
        val result = roomInteractionService.sendMessage(
            roomId = UUID.fromString(roomId),
            userId = UUID.fromString(userId),
            message = request.message
        )
        if (result.response == null) {
            val (status, message) = when (result.failure) {
                RoomInteractionService.MessageFailure.ROOM_NOT_FOUND ->
                    HttpStatusCode.NotFound to "Room not found"
                RoomInteractionService.MessageFailure.USER_NOT_IN_ROOM ->
                    HttpStatusCode.Forbidden to "Join the room first"
                RoomInteractionService.MessageFailure.USER_NOT_FOUND ->
                    HttpStatusCode.NotFound to "User not found"
                RoomInteractionService.MessageFailure.INVALID_MESSAGE ->
                    HttpStatusCode.BadRequest to "Message is invalid"
                null -> HttpStatusCode.BadRequest to "Unable to send message"
            }
            call.respond(status, ApiResponse<Unit>(success = false, error = message))
            return@post
        }
        call.respond(ApiResponse(success = true, data = result.response))
    }

    get("rooms/{id}/gifts") {
        val roomId = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
        val gifts = roomInteractionService.listGifts(UUID.fromString(roomId), limit)
        if (gifts == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Room not found"))
            return@get
        }
        call.respond(ApiResponse(success = true, data = gifts))
    }

    post("rooms/{id}/gifts") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val roomId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing room id")
        )
        val request = call.receiveOrNull<GiftSendRequest>()
            ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Missing gift payload"))
        val recipientId = request.recipientId?.let { value ->
            runCatching { UUID.fromString(value) }.getOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid recipient id"))
        }
        val giftId = request.giftId?.let { value ->
            runCatching { UUID.fromString(value) }.getOrNull()
                ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid gift id"))
        }
        val result = roomInteractionService.sendGift(
            roomId = UUID.fromString(roomId),
            senderId = UUID.fromString(userId),
            recipientId = recipientId,
            amount = request.amount,
            giftId = giftId,
            giftTypeOverride = request.giftType
        )
        if (result.response == null) {
            val (status, message) = when (result.failure) {
                RoomInteractionService.GiftFailure.ROOM_NOT_FOUND ->
                    HttpStatusCode.NotFound to "Room not found"
                RoomInteractionService.GiftFailure.USER_NOT_IN_ROOM ->
                    HttpStatusCode.Forbidden to "Join the room first"
                RoomInteractionService.GiftFailure.RECIPIENT_NOT_FOUND ->
                    HttpStatusCode.NotFound to "Recipient not found"
                RoomInteractionService.GiftFailure.RECIPIENT_NOT_IN_ROOM ->
                    HttpStatusCode.BadRequest to "Recipient not in room"
                RoomInteractionService.GiftFailure.INVALID_AMOUNT ->
                    HttpStatusCode.BadRequest to "Gift amount must be greater than 0"
                RoomInteractionService.GiftFailure.INSUFFICIENT_BALANCE ->
                    HttpStatusCode.BadRequest to "Insufficient balance"
                null -> HttpStatusCode.BadRequest to "Unable to send gift"
            }
            call.respond(status, ApiResponse<Unit>(success = false, error = message))
            return@post
        }
        call.respond(ApiResponse(success = true, data = result.response))
    }
}

private fun isRoomModerator(role: String): Boolean {
    return role in setOf("ADMIN", "RESELLER", "AGENCY", "HOST", "TEAM")
}

private fun isAdminRole(role: String): Boolean {
    return role == "ADMIN"
}
