package com.kappa.backend.services

import com.kappa.backend.config.AppConfig
import com.kappa.backend.data.RoomParticipants
import com.kappa.backend.data.Rooms
import com.kappa.backend.models.JoinRoomResponse
import com.kappa.backend.models.RoomResponse
import com.kappa.backend.models.SeatMode
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class RoomService(
    private val config: AppConfig,
    private val liveKitTokenService: LiveKitTokenService
) {
    fun listRooms(): List<RoomResponse> {
        return transaction {
            Rooms.select { Rooms.status eq "active" }
                .map { row ->
                    val roomId = row[Rooms.id]
                    RoomResponse(
                        id = roomId.toString(),
                        name = row[Rooms.name],
                        isActive = row[Rooms.status] == "active",
                        seatMode = SeatMode.valueOf(row[Rooms.seatMode]),
                        participantCount = participantCount(roomId)
                    )
                }
        }
    }

    fun joinRoom(roomId: UUID, userId: UUID, username: String): JoinRoomResponse? {
        return transaction {
            val roomRow = Rooms.select { Rooms.id eq roomId }.singleOrNull() ?: return@transaction null
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
                    it[joinedAt] = now
                    it[leftAt] = null
                }
            }

            val token = liveKitTokenService.generateToken(userId.toString(), username, roomRow[Rooms.name])
            val roomResponse = RoomResponse(
                id = roomRow[Rooms.id].toString(),
                name = roomRow[Rooms.name],
                isActive = roomRow[Rooms.status] == "active",
                seatMode = SeatMode.valueOf(roomRow[Rooms.seatMode]),
                participantCount = participantCount(roomId)
            )

            JoinRoomResponse(
                room = roomResponse,
                livekitUrl = config.livekitUrl,
                token = token
            )
        }
    }

    fun createRoom(name: String, seatMode: String): RoomResponse {
        return transaction {
            val now = System.currentTimeMillis()
            val roomId = UUID.randomUUID()
            val resolvedSeatMode = runCatching { SeatMode.valueOf(seatMode) }.getOrDefault(SeatMode.FREE)
            Rooms.insert {
                it[id] = roomId
                it[Rooms.name] = name
                it[Rooms.seatMode] = resolvedSeatMode.name
                it[status] = "active"
                it[createdAt] = now
                it[agencyId] = null
            }
            RoomResponse(
                id = roomId.toString(),
                name = name,
                isActive = true,
                seatMode = resolvedSeatMode,
                participantCount = 0
            )
        }
    }

    fun leaveRoom(roomId: UUID, userId: UUID): Boolean {
        return transaction {
            val now = System.currentTimeMillis()
            RoomParticipants.update({
                (RoomParticipants.roomId eq roomId) and
                    (RoomParticipants.userId eq userId) and
                    (RoomParticipants.leftAt.isNull())
            }) {
                it[leftAt] = now
            } > 0
        }
    }

    private fun participantCount(roomId: UUID): Int {
        return RoomParticipants.select {
            (RoomParticipants.roomId eq roomId) and RoomParticipants.leftAt.isNull()
        }.count().toInt()
    }
}
