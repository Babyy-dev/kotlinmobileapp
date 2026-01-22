package com.kappa.backend.services

import com.kappa.backend.config.AppConfig
import com.kappa.backend.data.RoomBans
import com.kappa.backend.data.RoomParticipants
import com.kappa.backend.data.RoomSeats
import com.kappa.backend.data.Rooms
import com.kappa.backend.data.Users
import com.kappa.backend.models.JoinRoomResponse
import com.kappa.backend.models.ParticipantRole
import com.kappa.backend.models.RoomResponse
import com.kappa.backend.models.RoomSeatResponse
import com.kappa.backend.models.SeatMode
import com.kappa.backend.models.SeatStatus
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

class RoomService(
    private val config: AppConfig,
    private val liveKitTokenService: LiveKitTokenService
) {
    enum class JoinRoomFailure {
        ROOM_NOT_FOUND,
        INVALID_PASSWORD,
        BANNED
    }

    data class JoinRoomResult(
        val response: JoinRoomResponse? = null,
        val failure: JoinRoomFailure? = null
    )

    fun listRooms(): List<RoomResponse> {
        return transaction {
            Rooms.select { Rooms.status eq "active" }
                .map { row ->
                    val roomId = row[Rooms.id]
                    val maxSeats = row[Rooms.maxSeats] ?: 28
                    RoomResponse(
                        id = roomId.toString(),
                        name = row[Rooms.name],
                        isActive = row[Rooms.status] == "active",
                        seatMode = SeatMode.valueOf(row[Rooms.seatMode]),
                        participantCount = participantCount(roomId),
                        maxSeats = maxSeats,
                        requiresPassword = row[Rooms.passwordHash] != null
                    )
                }
        }
    }

    fun joinRoom(roomId: UUID, userId: UUID, username: String, password: String?): JoinRoomResult {
        return transaction {
            val roomRow = Rooms.select { Rooms.id eq roomId }.singleOrNull()
                ?: return@transaction JoinRoomResult(failure = JoinRoomFailure.ROOM_NOT_FOUND)

            val banned = RoomBans.select { (RoomBans.roomId eq roomId) and (RoomBans.userId eq userId) }.any()
            if (banned) {
                return@transaction JoinRoomResult(failure = JoinRoomFailure.BANNED)
            }

            val storedPassword = roomRow[Rooms.passwordHash]
            if (storedPassword != null) {
                val provided = password?.trim().orEmpty()
                if (provided.isBlank() || !BCrypt.checkpw(provided, storedPassword)) {
                    return@transaction JoinRoomResult(failure = JoinRoomFailure.INVALID_PASSWORD)
                }
            }

            val now = System.currentTimeMillis()
            val alreadyInRoom = RoomParticipants.select {
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq userId) and
                    RoomParticipants.leftAt.isNull()
            }.any()
            if (!alreadyInRoom) {
                RoomParticipants.insert {
                    it[id] = UUID.randomUUID()
                    it[RoomParticipants.roomId] = roomId
                    it[RoomParticipants.userId] = userId
                    it[RoomParticipants.role] = ParticipantRole.LISTENER.name
                    it[RoomParticipants.seatNumber] = null
                    it[RoomParticipants.isMuted] = false
                    it[joinedAt] = now
                    it[RoomParticipants.leftAt] = null
                }
            }

            val token = liveKitTokenService.generateToken(userId.toString(), username, roomRow[Rooms.name])
            val roomResponse = RoomResponse(
                id = roomRow[Rooms.id].toString(),
                name = roomRow[Rooms.name],
                isActive = roomRow[Rooms.status] == "active",
                seatMode = SeatMode.valueOf(roomRow[Rooms.seatMode]),
                participantCount = participantCount(roomId),
                maxSeats = roomRow[Rooms.maxSeats] ?: 28,
                requiresPassword = roomRow[Rooms.passwordHash] != null
            )

            JoinRoomResult(
                response = JoinRoomResponse(
                    room = roomResponse,
                    livekitUrl = config.livekitUrl,
                    token = token
                )
            )
        }
    }

    fun createRoom(name: String, seatMode: String, maxSeats: Int, password: String?): RoomResponse {
        return transaction {
            val now = System.currentTimeMillis()
            val roomId = UUID.randomUUID()
            val resolvedSeatMode = runCatching { SeatMode.valueOf(seatMode) }.getOrDefault(SeatMode.FREE)
            val resolvedMaxSeats = maxSeats.coerceIn(1, 28)
            val passwordHash = password?.trim()?.takeIf { it.isNotBlank() }?.let { BCrypt.hashpw(it, BCrypt.gensalt()) }
            Rooms.insert {
                it[id] = roomId
                it[Rooms.name] = name
                it[Rooms.seatMode] = resolvedSeatMode.name
                it[Rooms.maxSeats] = resolvedMaxSeats
                it[Rooms.passwordHash] = passwordHash
                it[status] = "active"
                it[createdAt] = now
                it[agencyId] = null
            }
            (1..resolvedMaxSeats).forEach { seatNumber ->
                RoomSeats.insert {
                    it[RoomSeats.roomId] = roomId
                    it[RoomSeats.seatNumber] = seatNumber
                    it[RoomSeats.userId] = null
                    it[RoomSeats.status] = SeatStatus.FREE.name
                }
            }
            RoomResponse(
                id = roomId.toString(),
                name = name,
                isActive = true,
                seatMode = resolvedSeatMode,
                participantCount = 0,
                maxSeats = resolvedMaxSeats,
                requiresPassword = passwordHash != null
            )
        }
    }

    fun leaveRoom(roomId: UUID, userId: UUID): Boolean {
        return transaction {
            val now = System.currentTimeMillis()
            clearSeatForUser(roomId, userId)
            RoomParticipants.update({
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq userId) and
                    (RoomParticipants.leftAt.isNull())
            }) {
                it[RoomParticipants.leftAt] = now
            } > 0
        }
    }

    fun closeRoom(roomId: UUID): Boolean {
        return transaction {
            val now = System.currentTimeMillis()
            val updated = Rooms.update({ Rooms.id eq roomId }) {
                it[status] = "closed"
            } > 0
            RoomParticipants.update({ (RoomParticipants.roomId eq roomId) and RoomParticipants.leftAt.isNull() }) {
                it[RoomParticipants.leftAt] = now
            }
            RoomSeats.update({ RoomSeats.roomId eq roomId }) {
                it[RoomSeats.userId] = null
                it[RoomSeats.status] = SeatStatus.FREE.name
            }
            updated
        }
    }

    fun listSeats(roomId: UUID): List<RoomSeatResponse>? {
        return transaction {
            val exists = Rooms.select { Rooms.id eq roomId }.any()
            if (!exists) {
                return@transaction null
            }
            val maxSeats = Rooms.select { Rooms.id eq roomId }
                .singleOrNull()
                ?.get(Rooms.maxSeats)
                ?: 28
            ensureSeats(roomId, maxSeats)
            val seatRows = RoomSeats.select { RoomSeats.roomId eq roomId }
                .orderBy(RoomSeats.seatNumber)
                .toList()
            val userIds = seatRows.mapNotNull { it[RoomSeats.userId] }.distinct()
            val usernames = if (userIds.isNotEmpty()) {
                Users.select { Users.id inList userIds }.associate { it[Users.id] to it[Users.username] }
            } else {
                emptyMap()
            }
            seatRows.map { row ->
                val occupant = row[RoomSeats.userId]
                RoomSeatResponse(
                    seatNumber = row[RoomSeats.seatNumber],
                    status = SeatStatus.valueOf(row[RoomSeats.status]),
                    userId = occupant?.toString(),
                    username = occupant?.let { usernames[it] }
                )
            }
        }
    }

    fun takeSeat(roomId: UUID, seatNumber: Int, userId: UUID): String? {
        return transaction {
            val roomRow = Rooms.select { Rooms.id eq roomId }.singleOrNull()
                ?: return@transaction "Room not found"
            val maxSeats = roomRow[Rooms.maxSeats] ?: 28
            val seatMode = SeatMode.valueOf(roomRow[Rooms.seatMode])
            if (seatMode == SeatMode.BLOCKED) {
                return@transaction "Seat requires approval"
            }

            val participant = RoomParticipants.select {
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq userId) and
                    RoomParticipants.leftAt.isNull()
            }.singleOrNull() ?: return@transaction "Join the room first"

            ensureSeats(roomId, maxSeats)
            val seatRow = RoomSeats.select {
                (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber)
            }.singleOrNull() ?: return@transaction "Seat not found"

            val seatStatus = SeatStatus.valueOf(seatRow[RoomSeats.status])
            if (seatStatus == SeatStatus.BLOCKED) {
                return@transaction "Seat is locked"
            }
            val occupiedBy = seatRow[RoomSeats.userId]
            if (occupiedBy != null && occupiedBy != userId) {
                return@transaction "Seat already occupied"
            }

            clearSeatForUser(roomId, userId)

            RoomSeats.update({ (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber) }) {
                it[RoomSeats.userId] = userId
                it[RoomSeats.status] = SeatStatus.OCCUPIED.name
            }
            RoomParticipants.update({ RoomParticipants.id eq participant[RoomParticipants.id] }) {
                it[RoomParticipants.role] = ParticipantRole.SPEAKER.name
                it[RoomParticipants.seatNumber] = seatNumber
            }
            null
        }
    }

    fun leaveSeat(roomId: UUID, seatNumber: Int, userId: UUID): String? {
        return transaction {
            ensureSeats(roomId, 28)
            val seatRow = RoomSeats.select {
                (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber)
            }.singleOrNull() ?: return@transaction "Seat not found"
            if (seatRow[RoomSeats.userId] != userId) {
                return@transaction "Seat not owned by user"
            }
            RoomSeats.update({ (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber) }) {
                it[RoomSeats.userId] = null
                it[RoomSeats.status] = SeatStatus.FREE.name
            }
            RoomParticipants.update({
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq userId) and
                    RoomParticipants.leftAt.isNull()
            }) {
                it[RoomParticipants.role] = ParticipantRole.LISTENER.name
                it[RoomParticipants.seatNumber] = null
            }
            null
        }
    }

    fun lockSeat(roomId: UUID, seatNumber: Int): String? {
        return transaction {
            ensureSeats(roomId, 28)
            val seatRow = RoomSeats.select {
                (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber)
            }.singleOrNull() ?: return@transaction "Seat not found"
            val occupant = seatRow[RoomSeats.userId]
            if (occupant != null) {
                clearSeatForUser(roomId, occupant)
            }
            RoomSeats.update({ (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber) }) {
                it[RoomSeats.userId] = null
                it[RoomSeats.status] = SeatStatus.BLOCKED.name
            }
            null
        }
    }

    fun unlockSeat(roomId: UUID, seatNumber: Int): String? {
        return transaction {
            ensureSeats(roomId, 28)
            val seatRow = RoomSeats.select {
                (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber)
            }.singleOrNull() ?: return@transaction "Seat not found"
            if (SeatStatus.valueOf(seatRow[RoomSeats.status]) != SeatStatus.BLOCKED) {
                return@transaction "Seat is not locked"
            }
            RoomSeats.update({ (RoomSeats.roomId eq roomId) and (RoomSeats.seatNumber eq seatNumber) }) {
                it[RoomSeats.status] = SeatStatus.FREE.name
            }
            null
        }
    }

    fun muteParticipant(roomId: UUID, targetUserId: UUID, muted: Boolean): String? {
        return transaction {
            val updated = RoomParticipants.update({
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq targetUserId) and
                    RoomParticipants.leftAt.isNull()
            }) {
                it[RoomParticipants.isMuted] = muted
            }
            if (updated == 0) "Participant not found" else null
        }
    }

    fun kickParticipant(roomId: UUID, targetUserId: UUID): String? {
        return transaction {
            val now = System.currentTimeMillis()
            clearSeatForUser(roomId, targetUserId)
            val updated = RoomParticipants.update({
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq targetUserId) and
                    RoomParticipants.leftAt.isNull()
            }) {
                it[RoomParticipants.leftAt] = now
            }
            if (updated == 0) "Participant not found" else null
        }
    }

    fun banParticipant(roomId: UUID, targetUserId: UUID): String? {
        return transaction {
            val now = System.currentTimeMillis()
            val alreadyBanned = RoomBans.select {
                (RoomBans.roomId eq roomId) and (RoomBans.userId eq targetUserId)
            }.any()
            if (!alreadyBanned) {
                RoomBans.insert {
                    it[RoomBans.roomId] = roomId
                    it[RoomBans.userId] = targetUserId
                    it[createdAt] = now
                }
            }
            kickParticipant(roomId, targetUserId)
        }
    }

    private fun ensureSeats(roomId: UUID, maxSeats: Int) {
        val hasSeats = RoomSeats.select { RoomSeats.roomId eq roomId }.any()
        if (!hasSeats) {
            (1..maxSeats).forEach { seatNumber ->
                RoomSeats.insert {
                    it[RoomSeats.roomId] = roomId
                    it[RoomSeats.seatNumber] = seatNumber
                    it[RoomSeats.userId] = null
                    it[RoomSeats.status] = SeatStatus.FREE.name
                }
            }
        }
    }

    private fun participantCount(roomId: UUID): Int {
        return RoomParticipants.select {
            (RoomParticipants.roomId eq roomId) and RoomParticipants.leftAt.isNull()
        }.count().toInt()
    }

    private fun clearSeatForUser(roomId: UUID, userId: UUID) {
        val seatRow = RoomSeats.select {
            (RoomSeats.roomId eq roomId) and (RoomSeats.userId eq userId)
        }.singleOrNull()
        if (seatRow != null) {
            RoomSeats.update({ (RoomSeats.roomId eq roomId) and (RoomSeats.userId eq userId) }) {
                it[RoomSeats.userId] = null
                it[RoomSeats.status] = SeatStatus.FREE.name
            }
        }
        RoomParticipants.update({
            (RoomParticipants.roomId eq roomId) and
                (RoomParticipants.userId eq userId) and
                RoomParticipants.leftAt.isNull()
        }) {
            it[RoomParticipants.role] = ParticipantRole.LISTENER.name
            it[RoomParticipants.seatNumber] = null
        }
    }
}
