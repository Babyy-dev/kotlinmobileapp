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

        insertDiamondWallet(adminId, 0, now)
        insertDiamondWallet(resellerId, 0, now)
        insertDiamondWallet(agencyIdUser, 0, now)
        insertDiamondWallet(hostId, 0, now)
        insertDiamondWallet(teamId, 0, now)
        insertDiamondWallet(userId, 0, now)

        seedGifts(now)
        seedCoinPackages(now)

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

    private fun insertDiamondWallet(userId: UUID, balance: Long, now: Long) {
        DiamondWallets.insert {
            it[DiamondWallets.userId] = userId
            it[DiamondWallets.balance] = balance
            it[DiamondWallets.locked] = 0
            it[DiamondWallets.updatedAt] = now
        }
    }

    private fun seedGifts(now: Long) {
        val gifts = listOf(
            Triple("Rose", "INDIVIDUAL", 100L) to 100,
            Triple("Wave", "GROUP_FIXED", 20L) to 100,
            Triple("Multiplier", "GROUP_MULTIPLIER", 20L) to 10
        )
        gifts.forEach { (gift, percent) ->
            Gifts.insert {
                it[id] = UUID.randomUUID()
                it[name] = gift.first
                it[giftType] = gift.second
                it[costCoins] = gift.third
                it[diamondPercent] = percent
                it[isActive] = true
                it[createdAt] = now
            }
        }
    }

    private fun seedCoinPackages(now: Long) {
        val packages = listOf(
            Triple("Starter Pack", 1000L, 0.99),
            Triple("Value Pack", 5500L, 4.99),
            Triple("Mega Pack", 12000L, 9.99)
        )
        packages.forEach { (name, coins, price) ->
            CoinPackages.insert {
                it[id] = UUID.randomUUID()
                it[CoinPackages.name] = name
                it[coinAmount] = coins
                it[priceUsd] = price.toBigDecimal()
                it[isActive] = true
                it[createdAt] = now
            }
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
