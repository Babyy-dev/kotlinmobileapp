package com.kappa.backend.routes

import com.kappa.backend.models.AgencyApplicationRequest
import com.kappa.backend.models.AgencyUpdateRequest
import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.TeamCreateRequest
import com.kappa.backend.services.AgencyService
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

fun Route.agencyRoutes(agencyService: AgencyService) {
    post("agency/apply") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<AgencyApplicationRequest>()
        val response = agencyService.applyForAgency(UUID.fromString(userId), request.agencyName)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("reseller/apply") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val response = agencyService.applyForReseller(UUID.fromString(userId))
        call.respond(ApiResponse(success = true, data = response))
    }

    post("teams") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<TeamCreateRequest>()
        val response = agencyService.createTeam(UUID.fromString(userId), request.name)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("teams/{id}/join") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val teamId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing team id")
        )
        val joined = agencyService.joinTeam(UUID.fromString(userId), UUID.fromString(teamId))
        if (!joined) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Team not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Joined team"))
    }

    post("teams/{id}/leave") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val teamId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing team id")
        )
        val left = agencyService.leaveTeam(UUID.fromString(userId), UUID.fromString(teamId))
        if (!left) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Not in team"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = Unit, message = "Left team"))
    }

    get("teams") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val response = agencyService.listTeams(UUID.fromString(userId))
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/agency-applications/{id}/approve") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val appId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing application id")
        )
        val response = agencyService.reviewAgencyApplication(UUID.fromString(appId), true)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Application not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/agency-applications/{id}/reject") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val appId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing application id")
        )
        val response = agencyService.reviewAgencyApplication(UUID.fromString(appId), false)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Application not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/reseller-applications/{id}/approve") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val appId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing application id")
        )
        val response = agencyService.reviewResellerApplication(UUID.fromString(appId), true)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Application not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/reseller-applications/{id}/reject") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val appId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing application id")
        )
        val response = agencyService.reviewResellerApplication(UUID.fromString(appId), false)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Application not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    get("admin/agency-applications") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val response = agencyService.listAgencyApplications()
        call.respond(ApiResponse(success = true, data = response))
    }

    get("admin/reseller-applications") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val response = agencyService.listResellerApplications()
        call.respond(ApiResponse(success = true, data = response))
    }

    get("admin/agencies") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val status = call.request.queryParameters["status"]
        val limit = call.request.queryParameters["limit"]?.toIntOrNull() ?: 100
        val response = agencyService.listAgencies(status, limit)
        call.respond(ApiResponse(success = true, data = response))
    }

    post("admin/agencies/{id}") {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        if (role != "ADMIN") {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@post
        }
        val agencyId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing agency id")
        )
        val request = call.receive<AgencyUpdateRequest>()
        val response = agencyService.updateAgency(UUID.fromString(agencyId), request)
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Agency not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }
}
