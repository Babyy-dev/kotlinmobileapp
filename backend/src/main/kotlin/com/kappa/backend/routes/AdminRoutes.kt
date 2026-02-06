package com.kappa.backend.routes

import com.kappa.backend.data.AdminAuditLogs
import com.kappa.backend.data.AdminGameConfigs
import com.kappa.backend.data.AdminGlobalConfigs
import com.kappa.backend.data.AdminLockRules
import com.kappa.backend.data.AdminQualificationConfigs
import com.kappa.backend.data.AdminUserConfigs
import com.kappa.backend.data.HomeBanners
import com.kappa.backend.models.AdminAuditLog
import com.kappa.backend.models.AdminGameConfig
import com.kappa.backend.models.AdminGlobalConfig
import com.kappa.backend.models.AdminLockRule
import com.kappa.backend.models.AdminQualificationConfig
import com.kappa.backend.models.AdminUserConfig
import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.BannerUploadResponse
import com.kappa.backend.models.HomeBanner
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.streamProvider
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.request.forEachPart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.transactions.transaction
import java.io.File
import java.util.Locale
import java.util.UUID

fun Route.adminRoutes() {
    fun requireAdmin(call: io.ktor.server.application.ApplicationCall): String? {
        val principal = call.principal<JWTPrincipal>()
        val role = principal?.getClaim("role", String::class) ?: ""
        return if (role == "ADMIN") principal?.subject else null
    }

    get("admin/config/global") {
        if (requireAdmin(call) == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val config = transaction {
            AdminGlobalConfigs.selectAll().singleOrNull()?.let { row ->
                AdminGlobalConfig(
                    rtp = row[AdminGlobalConfigs.rtp],
                    houseEdge = row[AdminGlobalConfigs.houseEdge],
                    minRtp = row[AdminGlobalConfigs.minRtp],
                    maxRtp = row[AdminGlobalConfigs.maxRtp]
                )
            }
        }
        call.respond(ApiResponse(success = true, data = config))
    }

    post("admin/config/global") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<AdminGlobalConfig>()
        transaction {
            val now = System.currentTimeMillis()
            val existing = AdminGlobalConfigs.selectAll().singleOrNull()
            if (existing == null) {
                AdminGlobalConfigs.insert {
                    it[id] = UUID.randomUUID()
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[minRtp] = request.minRtp
                    it[maxRtp] = request.maxRtp
                    it[updatedAt] = now
                }
            } else {
                AdminGlobalConfigs.update({ AdminGlobalConfigs.id eq existing[AdminGlobalConfigs.id] }) {
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[minRtp] = request.minRtp
                    it[maxRtp] = request.maxRtp
                    it[updatedAt] = now
                }
            }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "GLOBAL_CONFIG_UPDATE"
                it[AdminAuditLogs.message] = "Global RTP/HouseEdge updated"
                it[AdminAuditLogs.createdAt] = now
            }
        }
        call.respond(ApiResponse(success = true, data = request))
    }

    get("admin/config/games") {
        if (requireAdmin(call) == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val games = transaction {
            AdminGameConfigs.selectAll().map { row ->
                AdminGameConfig(
                    id = row[AdminGameConfigs.id].toString(),
                    gameName = row[AdminGameConfigs.gameName],
                    rtp = row[AdminGameConfigs.rtp],
                    houseEdge = row[AdminGameConfigs.houseEdge]
                )
            }
        }
        call.respond(ApiResponse(success = true, data = games))
    }

    post("admin/config/games") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<AdminGameConfig>()
        val id = UUID.fromString(request.id)
        transaction {
            val now = System.currentTimeMillis()
            val updated = AdminGameConfigs.selectAll().any { it[AdminGameConfigs.id] == id }
            if (updated) {
                AdminGameConfigs.update({ AdminGameConfigs.id eq id }) {
                    it[gameName] = request.gameName
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[updatedAt] = now
                }
            } else {
                AdminGameConfigs.insert {
                    it[AdminGameConfigs.id] = id
                    it[gameName] = request.gameName
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[updatedAt] = now
                }
            }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "GAME_CONFIG_UPDATE"
                it[AdminAuditLogs.message] = "Game config ${request.gameName}"
                it[AdminAuditLogs.createdAt] = now
            }
        }
        call.respond(ApiResponse(success = true, data = request))
    }

    post("admin/config/games/{id}/delete") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val id = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing id")
        )
        transaction {
            AdminGameConfigs.deleteWhere { AdminGameConfigs.id eq UUID.fromString(id) }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "GAME_CONFIG_DELETE"
                it[AdminAuditLogs.message] = "Game config deleted"
                it[AdminAuditLogs.createdAt] = System.currentTimeMillis()
            }
        }
        call.respond(ApiResponse(success = true, data = Unit))
    }

    get("admin/config/users") {
        if (requireAdmin(call) == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val users = transaction {
            AdminUserConfigs.selectAll().map { row ->
                AdminUserConfig(
                    id = row[AdminUserConfigs.id].toString(),
                    userId = row[AdminUserConfigs.userId].toString(),
                    qualification = row[AdminUserConfigs.qualification],
                    rtp = row[AdminUserConfigs.rtp],
                    houseEdge = row[AdminUserConfigs.houseEdge]
                )
            }
        }
        call.respond(ApiResponse(success = true, data = users))
    }

    post("admin/config/users") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<AdminUserConfig>()
        val id = UUID.fromString(request.id)
        transaction {
            val now = System.currentTimeMillis()
            val exists = AdminUserConfigs.selectAll().any { it[AdminUserConfigs.id] == id }
            if (exists) {
                AdminUserConfigs.update({ AdminUserConfigs.id eq id }) {
                    it[userId] = UUID.fromString(request.userId)
                    it[qualification] = request.qualification
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[updatedAt] = now
                }
            } else {
                AdminUserConfigs.insert {
                    it[AdminUserConfigs.id] = id
                    it[userId] = UUID.fromString(request.userId)
                    it[qualification] = request.qualification
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[updatedAt] = now
                }
            }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "USER_CONFIG_UPDATE"
                it[AdminAuditLogs.message] = "User config ${request.userId}"
                it[AdminAuditLogs.createdAt] = now
            }
        }
        call.respond(ApiResponse(success = true, data = request))
    }

    post("admin/config/users/{id}/delete") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val id = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing id")
        )
        transaction {
            AdminUserConfigs.deleteWhere { AdminUserConfigs.id eq UUID.fromString(id) }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "USER_CONFIG_DELETE"
                it[AdminAuditLogs.message] = "User config deleted"
                it[AdminAuditLogs.createdAt] = System.currentTimeMillis()
            }
        }
        call.respond(ApiResponse(success = true, data = Unit))
    }

    get("admin/config/qualifications") {
        if (requireAdmin(call) == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val list = transaction {
            AdminQualificationConfigs.selectAll().map { row ->
                AdminQualificationConfig(
                    id = row[AdminQualificationConfigs.id].toString(),
                    qualification = row[AdminQualificationConfigs.qualification],
                    rtp = row[AdminQualificationConfigs.rtp],
                    houseEdge = row[AdminQualificationConfigs.houseEdge],
                    minPlayedUsd = row[AdminQualificationConfigs.minPlayedUsd],
                    durationDays = row[AdminQualificationConfigs.durationDays]
                )
            }
        }
        call.respond(ApiResponse(success = true, data = list))
    }

    post("admin/config/qualifications") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<AdminQualificationConfig>()
        val id = UUID.fromString(request.id)
        transaction {
            val now = System.currentTimeMillis()
            val exists = AdminQualificationConfigs.selectAll().any { it[AdminQualificationConfigs.id] == id }
            if (exists) {
                AdminQualificationConfigs.update({ AdminQualificationConfigs.id eq id }) {
                    it[qualification] = request.qualification
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[minPlayedUsd] = request.minPlayedUsd
                    it[durationDays] = request.durationDays
                    it[updatedAt] = now
                }
            } else {
                AdminQualificationConfigs.insert {
                    it[AdminQualificationConfigs.id] = id
                    it[qualification] = request.qualification
                    it[rtp] = request.rtp
                    it[houseEdge] = request.houseEdge
                    it[minPlayedUsd] = request.minPlayedUsd
                    it[durationDays] = request.durationDays
                    it[updatedAt] = now
                }
            }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "QUAL_CONFIG_UPDATE"
                it[AdminAuditLogs.message] = "Qualification ${request.qualification}"
                it[AdminAuditLogs.createdAt] = now
            }
        }
        call.respond(ApiResponse(success = true, data = request))
    }

    get("admin/lock-rules") {
        if (requireAdmin(call) == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val list = transaction {
            AdminLockRules.selectAll().map { row ->
                AdminLockRule(
                    id = row[AdminLockRules.id].toString(),
                    name = row[AdminLockRules.name],
                    cooldownMinutes = row[AdminLockRules.cooldownMinutes],
                    minTurnover = row[AdminLockRules.minTurnover],
                    maxLoss = row[AdminLockRules.maxLoss],
                    periodMinutes = row[AdminLockRules.periodMinutes],
                    maxActionsPerPeriod = row[AdminLockRules.maxActionsPerPeriod],
                    scope = row[AdminLockRules.scope],
                    actions = row[AdminLockRules.actions].split(",").filter { it.isNotBlank() }
                )
            }
        }
        call.respond(ApiResponse(success = true, data = list))
    }

    post("admin/lock-rules") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<AdminLockRule>()
        val id = UUID.fromString(request.id)
        transaction {
            val now = System.currentTimeMillis()
            val exists = AdminLockRules.selectAll().any { it[AdminLockRules.id] == id }
            if (exists) {
                AdminLockRules.update({ AdminLockRules.id eq id }) {
                    it[name] = request.name
                    it[cooldownMinutes] = request.cooldownMinutes
                    it[minTurnover] = request.minTurnover
                    it[maxLoss] = request.maxLoss
                    it[periodMinutes] = request.periodMinutes
                    it[maxActionsPerPeriod] = request.maxActionsPerPeriod
                    it[scope] = request.scope
                    it[actions] = request.actions.joinToString(",")
                    it[updatedAt] = now
                }
            } else {
                AdminLockRules.insert {
                    it[AdminLockRules.id] = id
                    it[name] = request.name
                    it[cooldownMinutes] = request.cooldownMinutes
                    it[minTurnover] = request.minTurnover
                    it[maxLoss] = request.maxLoss
                    it[periodMinutes] = request.periodMinutes
                    it[maxActionsPerPeriod] = request.maxActionsPerPeriod
                    it[scope] = request.scope
                    it[actions] = request.actions.joinToString(",")
                    it[updatedAt] = now
                }
            }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "LOCK_RULE_UPDATE"
                it[AdminAuditLogs.message] = "Lock rule ${request.name}"
                it[AdminAuditLogs.createdAt] = now
            }
        }
        call.respond(ApiResponse(success = true, data = request))
    }

    post("admin/lock-rules/{id}/delete") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val id = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing id")
        )
        transaction {
            AdminLockRules.deleteWhere { AdminLockRules.id eq UUID.fromString(id) }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "LOCK_RULE_DELETE"
                it[AdminAuditLogs.message] = "Lock rule deleted"
                it[AdminAuditLogs.createdAt] = System.currentTimeMillis()
            }
        }
        call.respond(ApiResponse(success = true, data = Unit))
    }

    get("admin/audit-logs") {
        if (requireAdmin(call) == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val logs = transaction {
            AdminAuditLogs.selectAll().orderBy(AdminAuditLogs.createdAt).map { row ->
                AdminAuditLog(
                    id = row[AdminAuditLogs.id].toString(),
                    actorId = row[AdminAuditLogs.actorId].toString(),
                    action = row[AdminAuditLogs.action],
                    message = row[AdminAuditLogs.message],
                    createdAt = row[AdminAuditLogs.createdAt]
                )
            }
        }
        call.respond(ApiResponse(success = true, data = logs))
    }

    get("admin/banners") {
        if (requireAdmin(call) == null) {
            call.respond(HttpStatusCode.Forbidden, ApiResponse<Unit>(success = false, error = "Insufficient permissions"))
            return@get
        }
        val banners = transaction {
            HomeBanners
                .selectAll()
                .orderBy(HomeBanners.sortOrder to SortOrder.ASC, HomeBanners.updatedAt to SortOrder.DESC)
                .map { row ->
                    HomeBanner(
                        id = row[HomeBanners.id].toString(),
                        title = row[HomeBanners.title],
                        subtitle = row[HomeBanners.subtitle],
                        imageUrl = row[HomeBanners.imageUrl],
                        actionType = row[HomeBanners.actionType],
                        actionTarget = row[HomeBanners.actionTarget],
                        sortOrder = row[HomeBanners.sortOrder],
                        isActive = row[HomeBanners.isActive]
                    )
                }
        }
        call.respond(ApiResponse(success = true, data = banners))
    }

    post("admin/banners") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val request = call.receive<HomeBanner>()
        val id = UUID.fromString(request.id)
        transaction {
            val now = System.currentTimeMillis()
            val exists = HomeBanners.selectAll().any { it[HomeBanners.id] == id }
            if (exists) {
                HomeBanners.update({ HomeBanners.id eq id }) {
                    it[title] = request.title
                    it[subtitle] = request.subtitle
                    it[imageUrl] = request.imageUrl
                    it[actionType] = request.actionType
                    it[actionTarget] = request.actionTarget
                    it[sortOrder] = request.sortOrder
                    it[isActive] = request.isActive
                    it[updatedAt] = now
                }
            } else {
                HomeBanners.insert {
                    it[HomeBanners.id] = id
                    it[title] = request.title
                    it[subtitle] = request.subtitle
                    it[imageUrl] = request.imageUrl
                    it[actionType] = request.actionType
                    it[actionTarget] = request.actionTarget
                    it[sortOrder] = request.sortOrder
                    it[isActive] = request.isActive
                    it[createdAt] = now
                    it[updatedAt] = now
                }
            }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "BANNER_UPSERT"
                it[AdminAuditLogs.message] = "Banner ${request.title}"
                it[AdminAuditLogs.createdAt] = now
            }
        }
        call.respond(ApiResponse(success = true, data = request))
    }

    post("admin/banners/{id}/delete") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val id = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing id")
        )
        transaction {
            HomeBanners.deleteWhere { HomeBanners.id eq UUID.fromString(id) }
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "BANNER_DELETE"
                it[AdminAuditLogs.message] = "Banner deleted"
                it[AdminAuditLogs.createdAt] = System.currentTimeMillis()
            }
        }
        call.respond(ApiResponse(success = true, data = Unit))
    }

    post("admin/banners/upload") {
        val actorId = requireAdmin(call) ?: return@post call.respond(
            HttpStatusCode.Forbidden,
            ApiResponse<Unit>(success = false, error = "Insufficient permissions")
        )
        val multipart = call.receiveMultipart()
        var uploadedUrl: String? = null
        multipart.forEachPart { part ->
            if (part is PartData.FileItem) {
                val extension = part.originalFileName
                    ?.substringAfterLast('.', "")
                    ?.lowercase(Locale.ENGLISH)
                    ?.takeIf { it in setOf("png", "jpg", "jpeg", "webp") }
                    ?: "png"
                val fileName = "banner_${UUID.randomUUID()}.$extension"
                val directory = File("uploads/banners").apply { mkdirs() }
                val file = File(directory, fileName)
                part.streamProvider().use { input ->
                    file.outputStream().use { output -> input.copyTo(output) }
                }
                uploadedUrl = "/uploads/banners/$fileName"
            }
            part.dispose()
        }
        if (uploadedUrl == null) {
            call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "No file uploaded"))
            return@post
        }
        transaction {
            AdminAuditLogs.insert {
                it[AdminAuditLogs.id] = UUID.randomUUID()
                it[AdminAuditLogs.actorId] = UUID.fromString(actorId)
                it[AdminAuditLogs.action] = "BANNER_UPLOAD"
                it[AdminAuditLogs.message] = uploadedUrl
                it[AdminAuditLogs.createdAt] = System.currentTimeMillis()
            }
        }
        call.respond(ApiResponse(success = true, data = BannerUploadResponse(uploadedUrl!!)))
    }
}
