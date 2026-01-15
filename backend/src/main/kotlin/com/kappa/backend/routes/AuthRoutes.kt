package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.LoginRequest
import com.kappa.backend.models.RefreshRequest
import com.kappa.backend.models.SignupRequest
import com.kappa.backend.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post

fun Route.authRoutes(authService: AuthService) {
    post("auth/signup") {
        val request = call.receive<SignupRequest>()
        val result = authService.signup(request)
        val response = result.response
        if (response == null) {
            val (status, message) = when (result.failure) {
                AuthService.SignupFailureReason.USERNAME_TAKEN -> HttpStatusCode.Conflict to "Username already exists"
                AuthService.SignupFailureReason.INVALID_ROLE -> HttpStatusCode.BadRequest to "Invalid role for signup"
                AuthService.SignupFailureReason.AGENCY_REQUIRED -> HttpStatusCode.BadRequest to "Agency required for user signup"
                AuthService.SignupFailureReason.AGENCY_NOT_FOUND -> HttpStatusCode.BadRequest to "Agency not found"
                AuthService.SignupFailureReason.WEAK_PASSWORD -> HttpStatusCode.BadRequest to "Password must be at least 6 characters"
                AuthService.SignupFailureReason.INVALID_INPUT -> HttpStatusCode.BadRequest to "Username, email, and password are required"
                null -> HttpStatusCode.BadRequest to "Signup failed"
            }
            call.respond(status, ApiResponse<Unit>(success = false, error = message))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("auth/login") {
        val request = call.receive<LoginRequest>()
        val response = authService.login(request.username, request.password)
        if (response == null) {
            call.respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(success = false, error = "Invalid credentials"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("auth/refresh") {
        val request = call.receive<RefreshRequest>()
        val response = authService.refresh(request.refreshToken)
        if (response == null) {
            call.respond(HttpStatusCode.Unauthorized, ApiResponse<Unit>(success = false, error = "Invalid refresh token"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("auth/logout") {
        val request = call.receive<RefreshRequest>()
        val revoked = authService.logout(request.refreshToken)
        if (!revoked) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Token not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Logged out"))
    }
}
