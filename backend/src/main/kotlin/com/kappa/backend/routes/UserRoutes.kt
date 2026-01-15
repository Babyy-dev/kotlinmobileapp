package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import java.util.UUID

fun Route.userRoutes(authService: AuthService) {
    get("users/me") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val user = authService.getUserById(UUID.fromString(userId))
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "User not found"))
            return@get
        }
        call.respond(ApiResponse(success = true, data = user))
    }

    get("users/{id}") {
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing user id")
        )
        val user = authService.getUserById(UUID.fromString(id))
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "User not found"))
            return@get
        }
        call.respond(ApiResponse(success = true, data = user))
    }
}
