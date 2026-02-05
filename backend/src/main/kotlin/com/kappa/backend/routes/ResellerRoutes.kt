package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.ResellerProofRequest
import com.kappa.backend.models.ResellerSaleRequest
import com.kappa.backend.models.ResellerSellerLimitRequest
import com.kappa.backend.models.ResellerSellerRequest
import com.kappa.backend.models.ResellerSendCoinsRequest
import com.kappa.backend.models.UserRole
import com.kappa.backend.services.ResellerService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import java.util.UUID

fun Route.resellerRoutes(resellerService: ResellerService) {
    fun requireReseller(call: io.ktor.server.application.ApplicationCall): String? {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        return if (role == UserRole.RESELLER.name) principal?.subject else null
    }

    get("reseller/sellers") {
        val resellerId = requireReseller(call) ?: return@get call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val response = resellerService.listSellers(UUID.fromString(resellerId))
        call.respond(ApiResponse(success = true, data = response))
    }

    post("reseller/sellers") {
        val resellerId = requireReseller(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<ResellerSellerRequest>()
        val sellerId = resellerService.resolveUserId(request.sellerId)
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Seller not found")
            )
        val response = resellerService.addSeller(UUID.fromString(resellerId), sellerId)
        resellerService.logAction(UUID.fromString(resellerId), UUID.fromString(resellerId), "SELLER_ADD", "Seller ${sellerId}")
        call.respond(ApiResponse(success = true, data = response))
    }

    get("reseller/sellers/{id}/limits") {
        val resellerId = requireReseller(call) ?: return@get call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val sellerId = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing seller id")
        )
        val resolved = resellerService.resolveUserId(sellerId)
            ?: return@get call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Seller not found")
            )
        val response = resellerService.getLimits(UUID.fromString(resellerId), resolved)
        call.respond(ApiResponse(success = true, data = response))
    }

    put("reseller/sellers/{id}/limits") {
        val resellerId = requireReseller(call) ?: return@put call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val sellerId = call.parameters["id"] ?: return@put call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing seller id")
        )
        val resolved = resellerService.resolveUserId(sellerId)
            ?: return@put call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Seller not found")
            )
        val request = call.receive<ResellerSellerLimitRequest>()
        val response = resellerService.setLimits(UUID.fromString(resellerId), resolved, request)
        resellerService.logAction(UUID.fromString(resellerId), UUID.fromString(resellerId), "SELLER_LIMIT_UPDATE", "Seller ${resolved}")
        call.respond(ApiResponse(success = true, data = response))
    }

    get("reseller/sales") {
        val resellerId = requireReseller(call) ?: return@get call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val response = resellerService.listSales(UUID.fromString(resellerId))
        call.respond(ApiResponse(success = true, data = response))
    }

    post("reseller/sales") {
        val resellerId = requireReseller(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<ResellerSaleRequest>()
        val response = resellerService.createSale(UUID.fromString(resellerId), request)
        resellerService.logAction(UUID.fromString(resellerId), UUID.fromString(resellerId), "SALE_CREATE", "Sale ${response.id}")
        call.respond(ApiResponse(success = true, data = response))
    }

    get("reseller/proofs") {
        val resellerId = requireReseller(call) ?: return@get call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val response = resellerService.listProofs(UUID.fromString(resellerId))
        call.respond(ApiResponse(success = true, data = response))
    }

    post("reseller/proofs") {
        val resellerId = requireReseller(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<ResellerProofRequest>()
        val response = resellerService.createProof(UUID.fromString(resellerId), request)
        resellerService.logAction(UUID.fromString(resellerId), UUID.fromString(resellerId), "PROOF_CREATE", "Proof ${response.id}")
        call.respond(ApiResponse(success = true, data = response))
    }

    post("reseller/send-coins") {
        val resellerId = requireReseller(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<ResellerSendCoinsRequest>()
        val recipientId = resellerService.resolveUserId(request.recipientId)
            ?: return@post call.respond(
                HttpStatusCode.BadRequest,
                ApiResponse<Unit>(success = false, error = "Recipient not found")
            )
        val response = resellerService.sendCoins(UUID.fromString(resellerId), recipientId, request)
        resellerService.logAction(
            UUID.fromString(resellerId),
            UUID.fromString(resellerId),
            "COIN_SEND",
            "Sent ${request.amount} to ${recipientId}"
        )
        call.respond(ApiResponse(success = true, data = response))
    }

    get("reseller/logs") {
        val resellerId = requireReseller(call) ?: return@get call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
        val response = resellerService.listLogs(UUID.fromString(resellerId), limit)
        call.respond(ApiResponse(success = true, data = response))
    }
}
