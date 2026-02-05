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

object ResellerSellers : Table("reseller_sellers") {
    val id = uuid("id")
    val resellerId = uuid("reseller_id").index()
    val sellerId = uuid("seller_id").index()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ResellerSellerLimits : Table("reseller_seller_limits") {
    val resellerId = uuid("reseller_id").index()
    val sellerId = uuid("seller_id").index()
    val totalLimit = long("total_limit")
    val dailyLimit = long("daily_limit")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(resellerId, sellerId)
}

object ResellerSales : Table("reseller_sales") {
    val id = uuid("id")
    val resellerId = uuid("reseller_id").index()
    val sellerId = uuid("seller_id").index()
    val buyerId = uuid("buyer_id").index()
    val externalSaleId = varchar("external_sale_id", 64).nullable()
    val amount = long("amount")
    val currency = varchar("currency", 12)
    val destinationAccount = varchar("destination_account", 120)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ResellerPaymentProofs : Table("reseller_payment_proofs") {
    val id = uuid("id")
    val resellerId = uuid("reseller_id").index()
    val uri = varchar("uri", 512)
    val amount = long("amount")
    val date = varchar("date", 32)
    val beneficiary = varchar("beneficiary", 120)
    val note = varchar("note", 255).nullable()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object ResellerAuditLogs : Table("reseller_audit_logs") {
    val id = uuid("id")
    val resellerId = uuid("reseller_id").index()
    val actorId = uuid("actor_id").index()
    val action = varchar("action", 120)
    val message = varchar("message", 500).nullable()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object AgencyAuditLogs : Table("agency_audit_logs") {
    val id = uuid("id")
    val agencyId = uuid("agency_id").index()
    val actorId = uuid("actor_id").index()
    val action = varchar("action", 120)
    val message = varchar("message", 500).nullable()
    val createdAt = long("created_at")
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
    val familyId = uuid("family_id").nullable().index()
    val country = varchar("country", 64).nullable()
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

object AdminGlobalConfigs : Table("admin_global_configs") {
    val id = uuid("id")
    val rtp = double("rtp")
    val houseEdge = double("house_edge")
    val minRtp = double("min_rtp")
    val maxRtp = double("max_rtp")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AdminGameConfigs : Table("admin_game_configs") {
    val id = uuid("id")
    val gameName = varchar("game_name", 120)
    val rtp = double("rtp")
    val houseEdge = double("house_edge")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AdminUserConfigs : Table("admin_user_configs") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val qualification = varchar("qualification", 32)
    val rtp = double("rtp")
    val houseEdge = double("house_edge")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AdminQualificationConfigs : Table("admin_qualification_configs") {
    val id = uuid("id")
    val qualification = varchar("qualification", 32)
    val rtp = double("rtp")
    val houseEdge = double("house_edge")
    val minPlayedUsd = long("min_played_usd")
    val durationDays = integer("duration_days")
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AdminLockRules : Table("admin_lock_rules") {
    val id = uuid("id")
    val name = varchar("name", 120)
    val cooldownMinutes = integer("cooldown_minutes")
    val minTurnover = long("min_turnover")
    val maxLoss = long("max_loss")
    val periodMinutes = integer("period_minutes")
    val maxActionsPerPeriod = integer("max_actions_per_period")
    val scope = varchar("scope", 24)
    val actions = varchar("actions", 255)
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object AdminAuditLogs : Table("admin_audit_logs") {
    val id = uuid("id")
    val actorId = uuid("actor_id").index()
    val action = varchar("action", 120)
    val message = varchar("message", 500).nullable()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object Families : Table("families") {
    val id = uuid("id")
    val name = varchar("name", 120)
    val code = varchar("code", 16).uniqueIndex()
    val ownerId = uuid("owner_id").index()
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object FamilyMembers : Table("family_members") {
    val id = uuid("id")
    val familyId = uuid("family_id").index()
    val userId = uuid("user_id").index()
    val role = varchar("role", 24)
    val joinedAt = long("joined_at")
    override val primaryKey = PrimaryKey(id)
}

object Friends : Table("friends") {
    val id = uuid("id")
    val userId = uuid("user_id").index()
    val friendId = uuid("friend_id").index()
    val status = varchar("status", 16)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object InboxThreads : Table("inbox_threads") {
    val id = uuid("id")
    val userA = uuid("user_a").index()
    val userB = uuid("user_b").index()
    val lastMessage = varchar("last_message", 500).nullable()
    val updatedAt = long("updated_at")
    override val primaryKey = PrimaryKey(id)
}

object InboxMessages : Table("inbox_messages") {
    val id = uuid("id")
    val threadId = uuid("thread_id").index()
    val senderId = uuid("sender_id").index()
    val message = varchar("message", 500)
    val createdAt = long("created_at")
    override val primaryKey = PrimaryKey(id)
}

object InboxThreadReads : Table("inbox_thread_reads") {
    val threadId = uuid("thread_id").index()
    val userId = uuid("user_id").index()
    val lastReadAt = long("last_read_at")
    override val primaryKey = PrimaryKey(threadId, userId)
}
