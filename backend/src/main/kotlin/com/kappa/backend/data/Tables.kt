package com.kappa.backend.data

import org.jetbrains.exposed.sql.Table

object Users : Table("users") {
    val id = uuid("id")
    val username = varchar("username", 50).uniqueIndex()
    val email = varchar("email", 120)
    val passwordHash = varchar("password_hash", 255)
    val role = varchar("role", 32)
    val phone = varchar("phone", 32).nullable()
    val nickname = varchar("nickname", 80).nullable()
    val avatarUrl = varchar("avatar_url", 512).nullable()
    val country = varchar("country", 64).nullable()
    val language = varchar("language", 64).nullable()
    val isGuest = bool("is_guest").nullable()
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

object DiamondWallets : Table("wallet_diamonds") {
    val userId = uuid("user_id").uniqueIndex()
    val balance = long("balance_diamonds")
    val locked = long("locked_diamonds")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(userId)
}

object PhoneOtps : Table("phone_otps") {
    val id = uuid("id")
    val phone = varchar("phone", 32).index()
    val code = varchar("code", 8)
    val expiresAt = long("expires_at")
    val consumedAt = long("consumed_at").nullable()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object CoinTransactions : Table("coin_transactions") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val type = varchar("type", 16)
    val amount = long("amount")
    val balanceAfter = long("balance_after")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object DiamondTransactions : Table("diamond_transactions") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val giftTransactionId = uuid("gift_transaction_id").nullable().index()
    val type = varchar("type", 24)
    val amount = long("amount")
    val balanceAfter = long("balance_after")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Gifts : Table("gifts") {
    val id = uuid("id")
    val name = varchar("name", 120)
    val giftType = varchar("gift_type", 32)
    val costCoins = long("cost_coins")
    val diamondPercent = integer("diamond_conversion_percent")
    val isActive = bool("is_active")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object GiftTransactions : Table("gift_transactions") {
    val id = uuid("id")
    val giftId = uuid("gift_id").nullable().index()
    val roomId = uuid("room_id").index()
    val senderId = uuid("sender_id").index()
    val recipientId = uuid("recipient_id").nullable().index()
    val giftType = varchar("gift_type", 32)
    val totalCostCoins = long("total_cost_coins")
    val diamondsTotal = long("diamonds_total")
    val recipientCount = integer("recipient_count")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object AgencyCommissions : Table("agency_commissions") {
    val id = uuid("id")
    val agencyId = uuid("agency_id").index()
    val userId = uuid("user_id").index()
    val giftTransactionId = uuid("gift_transaction_id").nullable().index()
    val diamondsAmount = long("diamonds_amount")
    val commissionUsd = decimal("commission_usd", 10, 2)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object DiamondConversions : Table("diamond_to_coin_conversions") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val diamondsUsed = long("diamonds_used")
    val coinsGenerated = long("coins_generated")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RewardRequests : Table("reward_requests") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val diamondsRequested = long("diamonds_requested")
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    val processedAt = long("processed_at").nullable()
    val note = varchar("note", 255).nullable()
    override val primaryKey = PrimaryKey(id)
}

object CoinPackages : Table("coin_packages") {
    val id = uuid("id")
    val name = varchar("name", 120)
    val storeProductId = varchar("store_product_id", 120).nullable()
    val coinAmount = long("coin_amount")
    val priceUsd = decimal("price_usd", 10, 2)
    val isActive = bool("is_active")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object CoinPurchases : Table("coin_purchases") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val packageId = uuid("package_id").index()
    val provider = varchar("provider", 32)
    val providerTxId = varchar("provider_tx_id", 128)
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object SlotPlays : Table("slot_plays") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val betCoins = long("bet_coins")
    val winCoins = long("win_coins")
    val balanceAfter = long("balance_after")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object TeamGroups : Table("teams") {
    val id = uuid("id")
    val agencyId = uuid("agency_id").nullable().index()
    val name = varchar("name", 120)
    val ownerUserId = uuid("owner_user_id").index()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object TeamMembers : Table("team_members") {
    val teamId = uuid("team_id").index()
    val userId = uuid("user_id").index()
    val role = varchar("role", 16)
    val joinedAt = long("joined_at")
    override val primaryKey = PrimaryKey(teamId, userId)
}

object AgencyApplications : Table("agency_applications") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val agencyName = varchar("agency_name", 120)
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    val reviewedAt = long("reviewed_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object ResellerApplications : Table("reseller_applications") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    val reviewedAt = long("reviewed_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object Announcements : Table("announcements") {
    val id = uuid("id")
    val title = varchar("title", 120)
    val message = varchar("message", 500)
    val isActive = bool("is_active")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Rooms : Table("rooms") {
    val id = uuid("id")
    val agencyId = uuid("agency_id").nullable()
    val name = varchar("name", 120)
    val maxSeats = integer("max_seats").nullable()
    val seatMode = varchar("seat_mode", 16)
    val passwordHash = varchar("password_hash", 255).nullable()
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RoomSeats : Table("room_seats") {
    val roomId = uuid("room_id")
    val seatNumber = integer("seat_number")
    val userId = uuid("user_id").nullable()
    val status = varchar("status", 16)
    override val primaryKey = PrimaryKey(roomId, seatNumber)
}

object RoomParticipants : Table("room_participants") {
    val id = uuid("id")
    val roomId = uuid("room_id")
    val userId = uuid("user_id")
    val role = varchar("role", 16)
    val seatNumber = integer("seat_number").nullable()
    val isMuted = bool("is_muted")
    val joinedAt = long("joined_at")
    val leftAt = long("left_at").nullable()
    override val primaryKey = PrimaryKey(id)
}

object RoomBans : Table("room_bans") {
    val roomId = uuid("room_id")
    val userId = uuid("user_id")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(roomId, userId)
}

object RoomMessages : Table("room_messages") {
    val id = uuid("id")
    val roomId = uuid("room_id").index()
    val userId = uuid("user_id").index()
    val message = varchar("message", 500)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RoomGifts : Table("room_gifts") {
    val id = uuid("id")
    val roomId = uuid("room_id").index()
    val senderId = uuid("sender_id").index()
    val recipientId = uuid("recipient_id").nullable().index()
    val amountCoins = long("amount_coins")
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object RefreshTokens : Table("refresh_tokens") {
    val token = varchar("token", 255)
    val userId = uuid("user_id")
    val expiresAt = long("expires_at")
    override val primaryKey = PrimaryKey(token)
}
