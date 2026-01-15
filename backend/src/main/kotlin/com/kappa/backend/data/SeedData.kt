package com.kappa.backend.data

import com.kappa.backend.models.SeatMode
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
        val masterId = UUID.randomUUID()
        val resellerId = UUID.randomUUID()
        val agencyOwnerId = UUID.randomUUID()
        val userId = UUID.randomUUID()
        val passwordHash = BCrypt.hashpw("password123", BCrypt.gensalt())

        Agencies.insert {
            it[id] = agencyId
            it[name] = "Kappa Agency"
            it[ownerUserId] = agencyOwnerId
            it[commissionValueUsd] = 2.20.toBigDecimal()
            it[commissionBlockDiamonds] = 620000
            it[status] = "active"
            it[createdAt] = now
        }

        insertUser(masterId, "master", "master@kappa.app", UserRole.MASTER, null, passwordHash, now)
        insertUser(resellerId, "reseller", "reseller@kappa.app", UserRole.RESELLER, agencyId, passwordHash, now)
        insertUser(agencyOwnerId, "agency", "agency@kappa.app", UserRole.AGENCY_OWNER, agencyId, passwordHash, now)
        insertUser(userId, "user", "user@kappa.app", UserRole.USER, agencyId, passwordHash, now)

        insertWallet(masterId, 500000, now)
        insertWallet(resellerId, 250000, now)
        insertWallet(agencyOwnerId, 100000, now)
        insertWallet(userId, 1250, now)

        Rooms.insert {
            it[id] = UUID.randomUUID()
            it[name] = "Kappa Lounge"
            it[seatMode] = SeatMode.FREE.name
            it[status] = "active"
            it[createdAt] = now
            it[Rooms.agencyId] = agencyId
        }
        Rooms.insert {
            it[id] = UUID.randomUUID()
            it[name] = "Agency Stage"
            it[seatMode] = SeatMode.BLOCKED.name
            it[status] = "active"
            it[createdAt] = now
            it[Rooms.agencyId] = agencyId
        }
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
}
