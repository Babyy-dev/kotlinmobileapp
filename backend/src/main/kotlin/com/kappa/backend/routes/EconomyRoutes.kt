package com.kappa.backend.routes

import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.CoinMutationRequest
import com.kappa.backend.models.CoinPackageRequest
import com.kappa.backend.models.CoinPurchaseRequest
import com.kappa.backend.models.DiamondConversionRequest
import com.kappa.backend.models.AnnouncementRequest
import com.kappa.backend.models.GiftCreateRequest
import com.kappa.backend.models.GiftUpdateRequest
import com.kappa.backend.models.RewardRequestCreate
import com.kappa.backend.services.EconomyService
import com.kappa.backend.services.SlotService
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

fun Route.economyRoutes(economyService: EconomyService, slotService: SlotService) {
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

    get("diamonds/balance") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val balance = economyService.getDiamondBalance(UUID.fromString(userId))
        call.respond(ApiResponse(success = true, data = balance))
    }

    get("diamonds/transactions") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
        val transactions = economyService.listDiamondTransactions(UUID.fromString(userId), limit)
        call.respond(ApiResponse(success = true, data = transactions))
    }

    post("diamonds/convert") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<DiamondConversionRequest>()
        val response = economyService.convertDiamondsToCoins(UUID.fromString(userId), request.diamonds)
        call.respond(ApiResponse(success = true, data = response))
    }

    get("coin-packages") {
        val packages = economyService.listCoinPackages()
        call.respond(ApiResponse(success = true, data = packages))
    }

    post("coin-packages/purchase") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<CoinPurchaseRequest>()
        val packageId = runCatching { UUID.fromString(request.packageId) }.getOrNull()
        if (packageId == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid package id"))
            return@post
        }
        val response = economyService.purchaseCoins(UUID.fromString(userId), packageId, request.provider, request.providerTxId)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/coin-packages") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val request = call.receive<CoinPackageRequest>()
        val response = economyService.createCoinPackage(request)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/coin-packages/{id}") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val packageId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing package id")
        )
        val request = call.receive<CoinPackageRequest>()
        val response = economyService.updateCoinPackage(UUID.fromString(packageId), request)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Package not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/coin-purchases/{id}/refund") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val purchaseId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing purchase id")
        )
        val response = economyService.refundPurchase(UUID.fromString(purchaseId))
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Purchase not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    get("gifts/catalog") {
        val catalog = economyService.listGiftCatalog()
        call.respond(ApiResponse(success = true, data = catalog))
    }

    post("gifts/catalog") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val request = call.receive<GiftCreateRequest>()
        val response = economyService.createGift(request)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("gifts/catalog/{id}") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val giftId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing gift id")
        )
        val request = call.receive<GiftUpdateRequest>()
        val response = economyService.updateGift(UUID.fromString(giftId), request)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Gift not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("rewards/request") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<RewardRequestCreate>()
        val response = economyService.requestReward(UUID.fromString(userId), request.diamonds)
        call.respond(ApiResponse(success = true, data = response))
    }

    get("rewards/requests") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val response = economyService.listRewardRequests(UUID.fromString(userId))
        call.respond(ApiResponse(success = true, data = response))
    }

    get("admin/rewards") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val status = call.request.queryParameters["status"]
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
        val response = economyService.listAllRewardRequests(status, limit)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/rewards/{id}/approve") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val requestId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing request id")
        )
        val response = economyService.reviewRewardRequest(UUID.fromString(requestId), true, null)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Request not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/rewards/{id}/reject") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val requestId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing request id")
        )
        val response = economyService.reviewRewardRequest(UUID.fromString(requestId), false, null)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Request not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    get("announcements") {
        val response = economyService.listAnnouncements()
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/announcements") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val request = call.receive<AnnouncementRequest>()
        val response = economyService.createAnnouncement(request)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/announcements/{id}") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val announcementId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing announcement id")
        )
        val request = call.receive<AnnouncementRequest>()
        val response = economyService.updateAnnouncement(UUID.fromString(announcementId), request)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Announcement not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    get("admin/commissions") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val agencyId = call.request.queryParameters["agencyId"]?.let { value ->
            runCatching { UUID.fromString(value) }.getOrNull()
        }
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
        val response = economyService.listAgencyCommissions(agencyId, limit)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("slots/play") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<com.kappa.backend.models.SlotPlayRequest>()
        val response = slotService.play(UUID.fromString(userId), request.betCoins)
        call.respond(ApiResponse(success = true, data = response))
    }

    get("slots/history") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 50
        val history = slotService.history(UUID.fromString(userId), limit)
        call.respond(ApiResponse(success = true, data = history))
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
