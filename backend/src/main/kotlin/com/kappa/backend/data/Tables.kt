package com.kappa.backend.data

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 120)
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 32)
    val agencyId = uuid("agency_id").nullable()
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Agencies : Table("agencies") {
    val id = uuid("id")
    val name = varchar("name", 120)
    val ownerUserId = uuid("owner_user_id")
    val commissionValueUsd = decimal("commission_value_usd", 6, 2)
    val commissionBlockDiamonds = long("commission_block_diamonds")
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object CoinWallets : Table("wallet_coins") {
    val userId = uuid("user_id").uniqueIndex()
    val balance = long("balance_coins")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(userId)
}

object Rooms : Table("rooms") {
    val id = uuid("id")
    val agencyId = uuid("agency_id").nullable()
    val name = varchar("name", 120)
    val seatMode = varchar("seat_mode", 16)
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RoomParticipants : Table("room_participants") {
    val id = uuid("id")
    val roomId = uuid("room_id")
    val userId = uuid("user_id")
    val joinedAt = long("joined_at")
    val leftAt = long("left_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object RefreshTokens : Table("refresh_tokens") {
    val token = varchar("token", 255)
    val userId = uuid("user_id")
    val expiresAt = long("expires_at")
    override val primaryKey = PrimaryKey(token)
}
