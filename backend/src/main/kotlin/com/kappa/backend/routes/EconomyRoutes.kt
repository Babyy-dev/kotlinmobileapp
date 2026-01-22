package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.CoinMutationRequest
import com.kappa.backend.services.EconomyService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import java.util.UUID

fun Route.economyRoutes(economyService: EconomyService) {
    get("coins/balance") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val balance = economyService.getCoinBalance(UUID.fromString(userId))
        call.respond(ApiResponse(success = true, data = balance))
    }

    get("coins/transactions") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
        val transactions = economyService.listTransactions(UUID.fromString(userId), limit)
        call.respond(ApiResponse(success = true, data = transactions))
    }

    post("coins/credit") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role !in setOf("ADMIN", "RESELLER")) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val request = call.receive<CoinMutationRequest>()
        val targetId = runCatching { UUID.fromString(request.userId) }.getOrNull()
        if (targetId == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid user id"))
            return@post
        }
        val balance = economyService.creditCoins(targetId, request.amount)
        call.respond(ApiResponse(success = true, data = balance))
    }

    post("coins/debit") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val request = call.receive<CoinMutationRequest>()
        val targetId = runCatching { UUID.fromString(request.userId) }.getOrNull()
        if (targetId == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid user id"))
            return@post
        }
        val balance = economyService.debitCoins(targetId, request.amount)
        call.respond(ApiResponse(success = true, data = balance))
    }
}
