package com.kappa.backend.routes

import com.kappa.backend.data.Families
import com.kappa.backend.data.FamilyMembers
import com.kappa.backend.data.Friends
import com.kappa.backend.data.InboxMessages
import com.kappa.backend.data.InboxThreadReads
import com.kappa.backend.data.InboxThreads
import com.kappa.backend.data.Rooms
import com.kappa.backend.data.Users
import com.kappa.backend.models.ApiResponse
import com.kappa.backend.models.FamilyCreateRequest
import com.kappa.backend.models.FamilyJoinRequest
import com.kappa.backend.models.FamilyMemberResponse
import com.kappa.backend.models.FamilyResponse
import com.kappa.backend.models.FamilyRoomResponse
import com.kappa.backend.models.FriendResponse
import com.kappa.backend.models.InboxMessageRequest
import com.kappa.backend.models.InboxMessageResponse
import com.kappa.backend.models.InboxThreadResponse
import com.kappa.backend.models.RoomResponse
import com.kappa.backend.services.SystemMessageService
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.call
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.or
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greater
import org.jetbrains.exposed.sql.SqlExpressionBuilder.neq
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

fun Route.socialRoutes(systemMessageService: SystemMessageService) {
    get("inbox/threads") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val userUuid = UUID.fromString(userId)
        val threads = transaction {
            InboxThreads.select {
                (InboxThreads.userA eq userUuid) or (InboxThreads.userB eq userUuid)
            }.map { row ->
                val peerId = if (row[InboxThreads.userA] == userUuid) row[InboxThreads.userB] else row[InboxThreads.userA]
                val peer = Users.select { Users.id eq peerId }.singleOrNull()
                val readRow = InboxThreadReads.select {
                    (InboxThreadReads.threadId eq row[InboxThreads.id]) and (InboxThreadReads.userId eq userUuid)
                }.singleOrNull()
                val lastReadAt = readRow?.get(InboxThreadReads.lastReadAt) ?: 0L
                val unreadCount = InboxMessages.select {
                    (InboxMessages.threadId eq row[InboxThreads.id]) and
                        (InboxMessages.senderId neq userUuid) and
                        (InboxMessages.createdAt greater lastReadAt)
                }.count().toInt()
                InboxThreadResponse(
                    id = row[InboxThreads.id].toString(),
                    peerId = peerId.toString(),
                    peerName = peer?.get(Users.nickname) ?: peer?.get(Users.username) ?: "User",
                    lastMessage = row[InboxThreads.lastMessage],
                    updatedAt = row[InboxThreads.updatedAt],
                    unreadCount = unreadCount
                )
            }
        }
        call.respond(ApiResponse(success = true, data = threads))
    }

    post("inbox/message") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<InboxMessageRequest>()
        val recipientId = runCatching { UUID.fromString(request.recipientId) }.getOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid recipient id"))
        val senderId = UUID.fromString(userId)
        val messageText = request.message.trim()
        if (messageText.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Message required"))
        }

        val response = transaction {
            val existing = InboxThreads.select {
                ((InboxThreads.userA eq senderId) and (InboxThreads.userB eq recipientId)) or
                    ((InboxThreads.userA eq recipientId) and (InboxThreads.userB eq senderId))
            }.singleOrNull()

            val threadId = existing?.get(InboxThreads.id) ?: UUID.randomUUID().also { id ->
                InboxThreads.insert {
                    it[InboxThreads.id] = id
                    it[userA] = senderId
                    it[userB] = recipientId
                    it[lastMessage] = messageText
                    it[updatedAt] = System.currentTimeMillis()
                }
            }

            InboxThreads.update({ InboxThreads.id eq threadId }) {
                it[lastMessage] = messageText
                it[updatedAt] = System.currentTimeMillis()
            }

            val messageId = UUID.randomUUID()
            InboxMessages.insert {
                it[id] = messageId
                it[InboxMessages.threadId] = threadId
                it[InboxMessages.senderId] = senderId
                it[message] = messageText
                it[createdAt] = System.currentTimeMillis()
            }

            val now = System.currentTimeMillis()
            val existingRead = InboxThreadReads.select {
                (InboxThreadReads.threadId eq threadId) and (InboxThreadReads.userId eq senderId)
            }.singleOrNull()
            if (existingRead == null) {
                InboxThreadReads.insert {
                    it[InboxThreadReads.threadId] = threadId
                    it[InboxThreadReads.userId] = senderId
                    it[InboxThreadReads.lastReadAt] = now
                }
            } else {
                InboxThreadReads.update(
                    { (InboxThreadReads.threadId eq threadId) and (InboxThreadReads.userId eq senderId) }
                ) {
                    it[lastReadAt] = now
                }
            }

            InboxMessageResponse(
                id = messageId.toString(),
                threadId = threadId.toString(),
                senderId = senderId.toString(),
                recipientId = recipientId.toString(),
                message = messageText,
                createdAt = now
            )
        }

        call.respond(ApiResponse(success = true, data = response))
    }

    post("inbox/threads/{id}/read") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val threadId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing thread id")
        )
        val threadUuid = UUID.fromString(threadId)
        val userUuid = UUID.fromString(userId)
        transaction {
            val existing = InboxThreadReads.select {
                (InboxThreadReads.threadId eq threadUuid) and (InboxThreadReads.userId eq userUuid)
            }.singleOrNull()
            val now = System.currentTimeMillis()
            if (existing == null) {
                InboxThreadReads.insert {
                    it[InboxThreadReads.threadId] = threadUuid
                    it[InboxThreadReads.userId] = userUuid
                    it[lastReadAt] = now
                }
            } else {
                InboxThreadReads.update(
                    { (InboxThreadReads.threadId eq threadUuid) and (InboxThreadReads.userId eq userUuid) }
                ) {
                    it[lastReadAt] = now
                }
            }
        }
        call.respond(ApiResponse(success = true, data = Unit))
    }

    get("friends") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val userUuid = UUID.fromString(userId)
        val friends = transaction {
            Friends.select { (Friends.userId eq userUuid) and (Friends.status eq "ACCEPTED") }
                .map { row ->
                    val friendId = row[Friends.friendId]
                    val friend = Users.select { Users.id eq friendId }.singleOrNull()
                    FriendResponse(
                        userId = friendId.toString(),
                        username = friend?.get(Users.username) ?: "user",
                        nickname = friend?.get(Users.nickname)
                    )
                }
        }
        call.respond(ApiResponse(success = true, data = friends))
    }

    get("friends/search") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val query = call.request.queryParameters["query"]?.trim().orEmpty()
        if (query.isBlank()) {
            call.respond(ApiResponse(success = true, data = emptyList<FriendResponse>()))
            return@get
        }
        val results = transaction {
            Users.select {
                (Users.username like "%$query%") or (Users.nickname like "%$query%")
            }.limit(20).map { row ->
                FriendResponse(
                    userId = row[Users.id].toString(),
                    username = row[Users.username],
                    nickname = row[Users.nickname]
                )
            }
        }
        call.respond(ApiResponse(success = true, data = results))
    }

    post("friends/{id}") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val friendId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing friend id")
        )
        val userUuid = UUID.fromString(userId)
        val friendUuid = runCatching { UUID.fromString(friendId) }.getOrNull()
            ?: return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Invalid friend id"))

        transaction {
            Friends.insert {
                it[id] = UUID.randomUUID()
                it[Friends.userId] = userUuid
                it[Friends.friendId] = friendUuid
                it[status] = "ACCEPTED"
                it[createdAt] = System.currentTimeMillis()
            }
            Friends.insert {
                it[id] = UUID.randomUUID()
                it[Friends.userId] = friendUuid
                it[Friends.friendId] = userUuid
                it[status] = "ACCEPTED"
                it[createdAt] = System.currentTimeMillis()
            }
        }
        val friendName = transaction {
            Users.select { Users.id eq userUuid }.singleOrNull()?.get(Users.username) ?: "User"
        }
        systemMessageService.sendSystemMessage(friendUuid, "You are now connected with $friendName.")
        call.respond(ApiResponse(success = true, data = Unit, message = "Friend added"))
    }

    post("family") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<FamilyCreateRequest>()
        val name = request.name.trim()
        if (name.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Family name required"))
        }
        val userUuid = UUID.fromString(userId)
        val response = transaction {
            val familyId = UUID.randomUUID()
            val code = generateFamilyCode()
            Families.insert {
                it[id] = familyId
                it[Families.name] = name
                it[Families.code] = code
                it[ownerId] = userUuid
                it[createdAt] = System.currentTimeMillis()
            }
            FamilyMembers.insert {
                it[id] = UUID.randomUUID()
                it[FamilyMembers.familyId] = familyId
                it[FamilyMembers.userId] = userUuid
                it[role] = "OWNER"
                it[joinedAt] = System.currentTimeMillis()
            }
            FamilyResponse(
                id = familyId.toString(),
                name = name,
                code = code,
                ownerId = userUuid.toString()
            )
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    post("family/join") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val request = call.receive<FamilyJoinRequest>()
        val code = request.code.trim().uppercase()
        if (code.isBlank()) {
            return@post call.respond(HttpStatusCode.BadRequest, ApiResponse<Unit>(success = false, error = "Family code required"))
        }
        val userUuid = UUID.fromString(userId)
        val response = transaction {
            val family = Families.select { Families.code eq code }.singleOrNull()
                ?: return@transaction null
            val familyId = family[Families.id]
            val existing = FamilyMembers.select {
                (FamilyMembers.familyId eq familyId) and (FamilyMembers.userId eq userUuid)
            }.any()
            if (!existing) {
                FamilyMembers.insert {
                    it[id] = UUID.randomUUID()
                    it[FamilyMembers.familyId] = familyId
                    it[FamilyMembers.userId] = userUuid
                    it[role] = "MEMBER"
                    it[joinedAt] = System.currentTimeMillis()
                }
            }
            FamilyResponse(
                id = familyId.toString(),
                name = family[Families.name],
                code = family[Families.code],
                ownerId = family[Families.ownerId].toString()
            )
        }
        if (response == null) {
            call.respond(HttpStatusCode.NotFound, ApiResponse<Unit>(success = false, error = "Family not found"))
            return@post
        }
        call.respond(ApiResponse(success = true, data = response))
    }

    get("family/me") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@get call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val userUuid = UUID.fromString(userId)
        val family = transaction {
            val member = FamilyMembers.select { FamilyMembers.userId eq userUuid }.singleOrNull() ?: return@transaction null
            val familyRow = Families.select { Families.id eq member[FamilyMembers.familyId] }.singleOrNull()
                ?: return@transaction null
            FamilyResponse(
                id = familyRow[Families.id].toString(),
                name = familyRow[Families.name],
                code = familyRow[Families.code],
                ownerId = familyRow[Families.ownerId].toString()
            )
        }
        if (family == null) {
            call.respond(ApiResponse(success = true, data = null))
            return@get
        }
        call.respond(ApiResponse(success = true, data = family))
    }

    get("family/{id}/members") {
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing family id")
        )
        val familyId = UUID.fromString(id)
        val members = transaction {
            FamilyMembers.select { FamilyMembers.familyId eq familyId }.map { row ->
                val userRow = Users.select { Users.id eq row[FamilyMembers.userId] }.singleOrNull()
                FamilyMemberResponse(
                    userId = row[FamilyMembers.userId].toString(),
                    username = userRow?.get(Users.username) ?: "user",
                    nickname = userRow?.get(Users.nickname),
                    role = row[FamilyMembers.role]
                )
            }
        }
        call.respond(ApiResponse(success = true, data = members))
    }

    get("family/{id}/rooms") {
        val id = call.parameters["id"] ?: return@get call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing family id")
        )
        val familyId = UUID.fromString(id)
        val rooms = transaction {
            Rooms.select { Rooms.familyId eq familyId }
                .map { row ->
                    FamilyRoomResponse(
                        id = row[Rooms.id].toString(),
                        name = row[Rooms.name],
                        status = row[Rooms.status]
                    )
                }
        }
        call.respond(ApiResponse(success = true, data = rooms))
    }

    post("family/{id}/rooms") {
        val principal = call.principal<JWTPrincipal>()
        val userId = principal?.subject ?: return@post call.respond(
            HttpStatusCode.Unauthorized,
            ApiResponse<Unit>(success = false, error = "Unauthorized")
        )
        val familyId = call.parameters["id"] ?: return@post call.respond(
            HttpStatusCode.BadRequest,
            ApiResponse<Unit>(success = false, error = "Missing family id")
        )
        val request = call.receive<com.kappa.backend.models.RoomCreateRequest>()
        val created = transaction {
            val roomId = UUID.randomUUID()
            val resolvedCountry = request.country?.trim()?.ifBlank { null } ?: "Global"
            Rooms.insert {
                it[id] = roomId
                it[Rooms.agencyId] = null
                it[Rooms.familyId] = UUID.fromString(familyId)
                it[name] = request.name
                it[maxSeats] = request.maxSeats
                it[seatMode] = request.seatMode.name
                it[passwordHash] = null
                it[status] = "active"
                it[createdAt] = System.currentTimeMillis()
                it[country] = resolvedCountry
            }
            RoomResponse(
                id = roomId.toString(),
                name = request.name,
                isActive = true,
                seatMode = request.seatMode,
                participantCount = 0,
                maxSeats = request.maxSeats,
                requiresPassword = false,
                country = resolvedCountry
            )
        }
        call.respond(ApiResponse(success = true, data = created))
    }
}

private fun generateFamilyCode(): String {
    val chars = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"
    return (1..6).map { chars.random() }.joinToString("")
}
