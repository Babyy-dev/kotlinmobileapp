package com.kappa.backend.data

import com.kappa.backend.models.SeatMode
import com.kappa.backend.models.SeatStatus
import com.kappa.backend.models.UserRole
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.mindrot.jbcrypt.BCrypt
import java.util.UUID

object SeedData {
    fun seedIfEmpty() {
        if (Users.selectAll().any()) {
            return
        }

        val now = System.currentTimeMillis()
        val agencyId = UUID.randomUUID()
        val adminId = UUID.randomUUID()
        val resellerId = UUID.randomUUID()
        val agencyIdUser = UUID.randomUUID()
        val hostId = UUID.randomUUID()
        val teamId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val passwordHash = BCrypt.hashpw("password123", BCrypt.gensalt())

        Agencies.insert {
            it[id] = agencyId
            it[name] = "Kappa Agency"
            it[ownerUserId] = agencyIdUser
            it[commissionValueUsd] = 2.20.toBigDecimal()
            it[commissionBlockDiamonds] = 620000
            it[status] = "active"
            it[createdAt] = now
        }

        insertUser(adminId, "admin", "admin@kappa.app", UserRole.ADMIN, null, passwordHash, now)
        insertUser(resellerId, "reseller", "reseller@kappa.app", UserRole.RESELLER, agencyId, passwordHash, now)
        insertUser(agencyIdUser, "agency", "agency@kappa.app", UserRole.AGENCY, agencyId, passwordHash, now)
        insertUser(hostId, "host", "host@kappa.app", UserRole.HOST, agencyId, passwordHash, now)
        insertUser(teamId, "team", "team@kappa.app", UserRole.TEAM, agencyId, passwordHash, now)
        insertUser(userId, "user", "user@kappa.app", UserRole.USER, agencyId, passwordHash, now)

        insertWallet(adminId, 500000, now)
        insertWallet(resellerId, 250000, now)
        insertWallet(agencyIdUser, 100000, now)
        insertWallet(hostId, 75000, now)
        insertWallet(teamId, 25000, now)
        insertWallet(userId, 1250, now)

        val loungeRoomId = UUID.randomUUID()
        Rooms.insert {
            it[id] = loungeRoomId
            it[name] = "Kappa Lounge"
            it[seatMode] = SeatMode.FREE.name
            it[Rooms.maxSeats] = 28
            it[Rooms.passwordHash] = null
            it[status] = "active"
            it[createdAt] = now
            it[Rooms.agencyId] = agencyId
        }
        seedSeats(loungeRoomId, 28)

        val stageRoomId = UUID.randomUUID()
        Rooms.insert {
            it[id] = stageRoomId
            it[name] = "Agency Stage"
            it[seatMode] = SeatMode.BLOCKED.name
            it[Rooms.maxSeats] = 28
            it[Rooms.passwordHash] = null
            it[status] = "active"
            it[createdAt] = now
            it[Rooms.agencyId] = agencyId
        }
        seedSeats(stageRoomId, 28)
    }

    private fun insertUser(
        userId: UUID,
        username: String,
        email: String,
        role: UserRole,
        agencyId: UUID?,
        passwordHash: String,
        createdAt: Long
    ) {
        Users.insert {
            it[id] = userId
            it[Users.username] = username
            it[Users.email] = email
            it[Users.passwordHash] = passwordHash
            it[Users.role] = role.name
            it[Users.agencyId] = agencyId
            it[Users.status] = "active"
            it[Users.createdAt] = createdAt
        }
    }

    private fun insertWallet(userId: UUID, balance: Long, now: Long) {
        CoinWallets.insert {
            it[CoinWallets.userId] = userId
            it[CoinWallets.balance] = balance
            it[CoinWallets.updatedAt] = now
        }
    }

    private fun seedSeats(roomId: UUID, maxSeats: Int) {
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
