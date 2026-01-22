package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.ProfileUpdateRequest
import com.kappa.backend.models.RoleUpdateRequest
import com.kappa.backend.models.UserRole
import com.kappa.backend.services.AuthService
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.utils.io.core.readBytes
import java.io.File
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

    post("users/profile") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<ProfileUpdateRequest>()
        val user = authService.updateProfile(UUID.fromString(userId), request)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "User not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = user))
    }

    post("users/avatar") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )

        val uploadDir = File("uploads/avatars")
        if (!uploadDir.exists()) {
            uploadDir.mkdirs()
        }

        var avatarUrl: String? = null
        val multipart = call.receiveMultipart()
        multipart.forEachPart { part ->
            when (part) {
                is PartData.FileItem -> {
                    if (avatarUrl == null) {
                        val extension = resolveAvatarExtension(part.originalFileName)
                        val fileName = "avatar_${userId}_${System.currentTimeMillis()}$extension"
                        val target = File(uploadDir, fileName)
                        val bytes = part.provider().readBytes()
                        target.writeBytes(bytes)
                        avatarUrl = "/uploads/avatars/$fileName"
                    }
                }
                else -> Unit
            }
            part.dispose()
        }

        if (avatarUrl == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Missing avatar file"))
            return@post
        }

        val user = authService.updateProfile(UUID.fromString(userId), ProfileUpdateRequest(avatarUrl = avatarUrl))
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "User not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = user))
    }

    post("users/{id}/role") {
        val principal = call.principal<JWTPrincipal>()
        val roleClaim = principal?.getClaim("role", String::class) ?: ""
        if (!isAdminRole(roleClaim)) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val id = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing user id")
        )
        val request = call.receive<RoleUpdateRequest>()
        val newRole = UserRole.fromApi(request.role)
        if (newRole == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid role"))
            return@post
        }
        val user = authService.updateUserRole(UUID.fromString(id), newRole)
        if (user == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "User not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = user))
    }
}

private fun resolveAvatarExtension(originalFileName: String?): String {
    val safeName = originalFileName?.trim().orEmpty()
    val dotIndex = safeName.lastIndexOf('.')
    if (dotIndex == -1 || dotIndex == safeName.length - 1) {
        return ".png"
    }
    val extension = safeName.substring(dotIndex + 1)
        .lowercase()
        .filter { it.isLetterOrDigit() }
        .take(6)
    return if (extension.isBlank()) ".png" else ".${extension}"
}

private fun isAdminRole(role: String): Boolean {
    return role == "ADMIN"
}
