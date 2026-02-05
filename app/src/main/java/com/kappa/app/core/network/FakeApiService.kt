package com.kappa.app.core.network

import com.kappa.app.core.network.model.AgencyApplicationDto
import com.kappa.app.core.network.model.AgencyApplicationRequestDto
import com.kappa.app.core.network.model.AgencyCommissionDto
import com.kappa.app.core.network.model.AgencyRoomDto
import com.kappa.app.core.network.model.AgencyHostDto
import com.kappa.app.core.network.model.CoinBalanceDto
import com.kappa.app.core.network.model.GiftSendDto
import com.kappa.app.core.network.model.GiftSendRequest
import com.kappa.app.core.network.model.GiftCatalogDto
import com.kappa.app.core.network.model.HomeBannerDto
import com.kappa.app.core.network.model.MiniGameDto
import com.kappa.app.core.network.model.SearchResultDto
import com.kappa.app.core.network.model.AgencySummaryDto
import com.kappa.app.core.network.model.GameSessionRequest
import com.kappa.app.core.network.model.GameSessionResponse
import com.kappa.app.core.network.model.InboxThreadDto
import com.kappa.app.core.network.model.InboxMessageRequest
import com.kappa.app.core.network.model.InboxMessageResponse
import com.kappa.app.core.network.model.FriendDto
import com.kappa.app.core.network.model.FamilyCreateRequest
import com.kappa.app.core.network.model.FamilyJoinRequest
import com.kappa.app.core.network.model.FamilyDto
import com.kappa.app.core.network.model.FamilyMemberDto
import com.kappa.app.core.network.model.FamilyRoomDto
import com.kappa.app.core.network.model.AdminGlobalConfigDto
import com.kappa.app.core.network.model.AdminGameConfigDto
import com.kappa.app.core.network.model.AdminUserConfigDto
import com.kappa.app.core.network.model.AdminQualificationConfigDto
import com.kappa.app.core.network.model.AdminLockRuleDto
import com.kappa.app.core.network.model.AdminAuditLogDto
import com.kappa.app.core.network.model.GuestLoginRequest
import com.kappa.app.core.network.model.JoinRoomDto
import com.kappa.app.core.network.model.JoinRoomRequest
import com.kappa.app.core.network.model.LoginRequest
import com.kappa.app.core.network.model.LoginResponse
import com.kappa.app.core.network.model.MuteParticipantRequest
import com.kappa.app.core.network.model.PhoneOtpRequest
import com.kappa.app.core.network.model.PhoneOtpResponse
import com.kappa.app.core.network.model.PhoneOtpVerifyRequest
import com.kappa.app.core.network.model.ProfileUpdateRequest
import com.kappa.app.core.network.model.RefreshRequest
import com.kappa.app.core.network.model.ResellerApplicationDto
import com.kappa.app.core.network.model.ResellerSellerDto
import com.kappa.app.core.network.model.ResellerSellerRequestDto
import com.kappa.app.core.network.model.ResellerSellerLimitDto
import com.kappa.app.core.network.model.ResellerSellerLimitRequestDto
import com.kappa.app.core.network.model.ResellerSaleDto
import com.kappa.app.core.network.model.ResellerSaleRequestDto
import com.kappa.app.core.network.model.ResellerProofDto
import com.kappa.app.core.network.model.ResellerProofRequestDto
import com.kappa.app.core.network.model.ResellerSendCoinsRequestDto
import com.kappa.app.core.network.model.ResellerSendCoinsResponseDto
import com.kappa.app.core.network.model.RoomCreateRequest
import com.kappa.app.core.network.model.RoomDto
import com.kappa.app.core.network.model.RoomMessageDto
import com.kappa.app.core.network.model.RoomMessageRequest
import com.kappa.app.core.network.model.RoomSeatDto
import com.kappa.app.core.network.model.SignupRequest
import com.kappa.app.core.network.model.TeamCreateRequestDto
import com.kappa.app.core.network.model.TeamDto
import com.kappa.app.core.network.model.UserDto
import okhttp3.MultipartBody
import java.util.UUID
import java.util.concurrent.ConcurrentHashMap
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FakeApiService @Inject constructor() : ApiService {

    private val users = ConcurrentHashMap<String, UserDto>()
    private val usernames = ConcurrentHashMap<String, String>()
    private val emails = ConcurrentHashMap<String, String>()
    private val phones = ConcurrentHashMap<String, String>()
    private val coins = ConcurrentHashMap<String, Long>()
    private val rooms = ConcurrentHashMap<String, RoomDto>()
    private val roomSeats = ConcurrentHashMap<String, MutableList<RoomSeatDto>>()
    private val roomMessages = ConcurrentHashMap<String, MutableList<RoomMessageDto>>()
    private val roomGifts = ConcurrentHashMap<String, MutableList<GiftSendDto>>()
    private val giftCatalog = mutableListOf<GiftCatalogDto>()
    private val homeBanners = mutableListOf<HomeBannerDto>()
    private val miniGames = mutableListOf<MiniGameDto>()
    private val agencies = mutableListOf<AgencySummaryDto>()
    private val favoriteRooms = ConcurrentHashMap<String, MutableSet<String>>()
    private val inboxThreads = mutableListOf<InboxThreadDto>()
    private val friends = mutableListOf<FriendDto>()
    private var family: FamilyDto? = null
    private val familyMembers = mutableListOf<FamilyMemberDto>()
    private val familyRooms = mutableListOf<FamilyRoomDto>()
    private val teams = ConcurrentHashMap<String, TeamDto>()
    private val teamMembers = ConcurrentHashMap<String, MutableSet<String>>()
    private val agencyApplications = mutableListOf<AgencyApplicationDto>()
    private val resellerApplications = mutableListOf<ResellerApplicationDto>()
    private val commissions = mutableListOf<AgencyCommissionDto>()
    private val agencyRooms = mutableListOf<AgencyRoomDto>()
    private val agencyHosts = mutableListOf<AgencyHostDto>()
    private val resellerSellers = mutableListOf<ResellerSellerDto>()
    private val resellerLimits = mutableListOf<ResellerSellerLimitDto>()
    private val resellerSales = mutableListOf<ResellerSaleDto>()
    private val resellerProofs = mutableListOf<ResellerProofDto>()
    private var adminGlobalConfig = AdminGlobalConfigDto(70.0, 30.0, 60.0, 90.0)
    private val adminGameConfigs = mutableListOf<AdminGameConfigDto>()
    private val adminUserConfigs = mutableListOf<AdminUserConfigDto>()
    private val adminQualificationConfigs = mutableListOf<AdminQualificationConfigDto>()
    private val adminLockRules = mutableListOf<AdminLockRuleDto>()
    private val adminAuditLogs = mutableListOf<AdminAuditLogDto>()
    private var currentUserId: String? = null

    init {
        seedDefaults()
    }

    override suspend fun login(request: LoginRequest): BaseApiResponse<LoginResponse> {
        val raw = request.username.trim()
        val phoneKey = raw.filter { it.isDigit() || it == '+' }
        val existingId = if (raw.contains("@")) {
            emails[raw]
        } else if (phoneKey.startsWith("+")) {
            phones[phoneKey]
        } else {
            usernames[raw]
        }
        val userId = existingId ?: run {
            val inferredRole = when (raw.lowercase()) {
                "admin", "master" -> "admin"
                "agency", "agency_owner" -> "agency"
                "reseller" -> "reseller"
                "host" -> "host"
                "team" -> "team"
                else -> "user"
            }
            createUser(
                username = if (raw.contains("@")) raw.substringBefore("@") else raw,
                email = if (raw.contains("@")) raw else "$raw@kappa.local",
                role = inferredRole
            ).id
        }
        currentUserId = userId
        val user = users[userId] ?: return error("User not found")
        return success(buildLoginResponse(user))
    }

    override suspend fun signup(request: SignupRequest): BaseApiResponse<LoginResponse> {
        if (usernames.containsKey(request.username) || emails.containsKey(request.email)) {
            return error("User already exists")
        }
        val user = createUser(
            username = request.username,
            email = request.email,
            role = request.role ?: "user",
            phone = request.phone,
            nickname = request.nickname,
            avatarUrl = request.avatarUrl,
            agencyId = request.agencyId
        )
        currentUserId = user.id
        return success(buildLoginResponse(user))
    }

    override suspend fun requestOtp(request: PhoneOtpRequest): BaseApiResponse<PhoneOtpResponse> {
        val response = PhoneOtpResponse(
            phone = request.phone,
            code = "123456",
            expiresAt = System.currentTimeMillis() + 5 * 60_000
        )
        return success(response)
    }

    override suspend fun verifyOtp(request: PhoneOtpVerifyRequest): BaseApiResponse<LoginResponse> {
        val user = createUser(
            username = "user_${request.phone}",
            email = "${request.phone}@kappa.local",
            role = "user",
            phone = request.phone
        )
        currentUserId = user.id
        return success(buildLoginResponse(user))
    }

    override suspend fun guestLogin(request: GuestLoginRequest): BaseApiResponse<LoginResponse> {
        val user = createUser(
            username = "guest_${UUID.randomUUID().toString().take(6)}",
            email = "guest@kappa.local",
            role = "guest",
            nickname = request.nickname ?: "Guest",
            avatarUrl = request.avatarUrl,
            isGuest = true
        )
        currentUserId = user.id
        return success(buildLoginResponse(user))
    }

    override suspend fun logout(request: RefreshRequest): BaseApiResponse<Unit> {
        currentUserId = null
        return success(Unit)
    }

    override suspend fun refresh(request: RefreshRequest): BaseApiResponse<LoginResponse> {
        val user = getCurrentUserInternal() ?: return error("No user")
        return success(buildLoginResponse(user))
    }

    override suspend fun getCurrentUser(): BaseApiResponse<UserDto> {
        return success(getCurrentUserInternal() ?: return error("No active user"))
    }

    override suspend fun updateProfile(request: ProfileUpdateRequest): BaseApiResponse<UserDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val updated = user.copy(
            nickname = request.nickname ?: user.nickname,
            avatarUrl = request.avatarUrl ?: user.avatarUrl,
            country = request.country ?: user.country,
            language = request.language ?: user.language
        )
        users[user.id] = updated
        return success(updated)
    }

    override suspend fun uploadAvatar(avatar: MultipartBody.Part): BaseApiResponse<UserDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val updated = user.copy(avatarUrl = "local://${avatar.body.hashCode()}")
        users[user.id] = updated
        return success(updated)
    }

    override suspend fun getUser(id: String): BaseApiResponse<UserDto> {
        val user = users[id] ?: return error("User not found")
        return success(user)
    }

    override suspend fun getCoinBalance(): BaseApiResponse<CoinBalanceDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val balance = coins[user.id] ?: 0L
        return success(CoinBalanceDto(userId = user.id, balance = balance, currency = "coins"))
    }

    override suspend fun getRooms(): BaseApiResponse<List<RoomDto>> {
        val userId = getCurrentUserInternal()?.id
        val favorites = if (userId == null) emptySet() else favoriteRooms[userId].orEmpty()
        val result = rooms.values
            .map { room ->
                if (favorites.contains(room.id)) room.copy(isFavorite = true) else room
            }
            .sortedBy { it.name }
        return success(result)
    }

    override suspend fun getHomeBanners(): BaseApiResponse<List<HomeBannerDto>> {
        return success(homeBanners.toList())
    }

    override suspend fun getPopularMiniGames(): BaseApiResponse<List<MiniGameDto>> {
        return success(miniGames.toList())
    }

    override suspend fun createRoom(request: RoomCreateRequest): BaseApiResponse<RoomDto> {
        val room = RoomDto(
            id = "room_${UUID.randomUUID().toString().take(6)}",
            name = request.name,
            isActive = true,
            seatMode = request.seatMode,
            participantCount = 1,
            maxSeats = request.maxSeats,
            requiresPassword = !request.password.isNullOrBlank(),
            country = request.country ?: "Global",
            region = resolveRegion(request.country),
            agencyName = "Kappa Agency",
            agencyIconUrl = null,
            roomCode = "ROOM-${UUID.randomUUID().toString().take(5)}",
            isFavorite = false
        )
        rooms[room.id] = room
        roomSeats[room.id] = buildSeats(room.maxSeats)
        roomMessages[room.id] = mutableListOf()
        roomGifts[room.id] = mutableListOf()
        return success(room)
    }

    override suspend fun closeRoom(id: String): BaseApiResponse<Unit> {
        val room = rooms[id] ?: return error("Room not found")
        rooms[id] = room.copy(isActive = false)
        return success(Unit)
    }

    override suspend fun getRoomSeats(id: String): BaseApiResponse<List<RoomSeatDto>> {
        return success(roomSeats[id] ?: buildSeats(28))
    }

    override suspend fun takeSeat(id: String, seat: Int): BaseApiResponse<Unit> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val seats = roomSeats.getOrPut(id) { buildSeats(28) }
        val target = seats.find { it.seatNumber == seat } ?: return error("Seat not found")
        if (target.status == "LOCKED") return error("Seat locked")
        val index = seats.indexOf(target)
        seats[index] = target.copy(status = "OCCUPIED", userId = user.id, username = user.nickname ?: user.username)
        return success(Unit)
    }

    override suspend fun leaveSeat(id: String, seat: Int): BaseApiResponse<Unit> {
        val seats = roomSeats.getOrPut(id) { buildSeats(28) }
        val target = seats.find { it.seatNumber == seat } ?: return error("Seat not found")
        val index = seats.indexOf(target)
        seats[index] = target.copy(status = "FREE", userId = null, username = null)
        return success(Unit)
    }

    override suspend fun lockSeat(id: String, seat: Int): BaseApiResponse<Unit> {
        val seats = roomSeats.getOrPut(id) { buildSeats(28) }
        val target = seats.find { it.seatNumber == seat } ?: return error("Seat not found")
        val index = seats.indexOf(target)
        seats[index] = target.copy(status = "LOCKED", userId = null, username = null)
        return success(Unit)
    }

    override suspend fun unlockSeat(id: String, seat: Int): BaseApiResponse<Unit> {
        val seats = roomSeats.getOrPut(id) { buildSeats(28) }
        val target = seats.find { it.seatNumber == seat } ?: return error("Seat not found")
        val index = seats.indexOf(target)
        seats[index] = target.copy(status = "FREE", userId = null, username = null)
        return success(Unit)
    }

    override suspend fun requestSeat(id: String, seat: Int): BaseApiResponse<Unit> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val seats = roomSeats.getOrPut(id) { buildSeats(28) }
        val target = seats.find { it.seatNumber == seat } ?: return error("Seat not found")
        if (target.status != "LOCKED") {
            return error("Seat is not locked")
        }
        val requestMessage = RoomMessageDto(
            id = "req_${UUID.randomUUID().toString().take(6)}",
            roomId = id,
            userId = user.id,
            username = user.nickname ?: user.username,
            message = "Seat request for #${seat}",
            createdAt = System.currentTimeMillis(),
            type = "REWARD"
        )
        roomMessages.getOrPut(id) { mutableListOf() }.add(requestMessage)
        return success(Unit)
    }

    override suspend fun joinRoom(id: String, request: JoinRoomRequest?): BaseApiResponse<JoinRoomDto> {
        val room = rooms[id] ?: return error("Room not found")
        val updated = room.copy(participantCount = room.participantCount + 1)
        rooms[id] = updated
        return success(
            JoinRoomDto(
                room = updated,
                livekitUrl = "wss://local.livekit",
                token = "local-token-${UUID.randomUUID().toString().take(8)}"
            )
        )
    }

    override suspend fun leaveRoom(id: String): BaseApiResponse<Unit> {
        val room = rooms[id] ?: return error("Room not found")
        rooms[id] = room.copy(participantCount = (room.participantCount - 1).coerceAtLeast(0))
        return success(Unit)
    }

    override suspend fun toggleRoomFavorite(id: String, favorite: Boolean): BaseApiResponse<RoomDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val room = rooms[id] ?: return error("Room not found")
        val favorites = favoriteRooms.getOrPut(user.id) { mutableSetOf() }
        if (favorite) {
            favorites.add(id)
        } else {
            favorites.remove(id)
        }
        return success(room.copy(isFavorite = favorites.contains(id)))
    }

    override suspend fun muteParticipant(id: String, userId: String, request: MuteParticipantRequest): BaseApiResponse<Unit> {
        return success(Unit)
    }

    override suspend fun kickParticipant(id: String, userId: String): BaseApiResponse<Unit> {
        clearUserFromSeats(id, userId)
        return success(Unit)
    }

    override suspend fun banParticipant(id: String, userId: String): BaseApiResponse<Unit> {
        clearUserFromSeats(id, userId)
        return success(Unit)
    }

    override suspend fun getRoomMessages(id: String, limit: Int?): BaseApiResponse<List<RoomMessageDto>> {
        val list = roomMessages[id] ?: mutableListOf()
        val result = if (limit == null) list else list.takeLast(limit)
        return success(result)
    }

    override suspend fun sendRoomMessage(id: String, request: RoomMessageRequest): BaseApiResponse<RoomMessageDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val message = RoomMessageDto(
            id = "msg_${UUID.randomUUID().toString().take(6)}",
            roomId = id,
            userId = user.id,
            username = user.nickname ?: user.username,
            message = request.message,
            createdAt = System.currentTimeMillis(),
            type = request.type ?: "CHAT"
        )
        roomMessages.getOrPut(id) { mutableListOf() }.add(message)
        return success(message)
    }

    override suspend fun getRoomGifts(id: String, limit: Int?): BaseApiResponse<List<GiftSendDto>> {
        val list = roomGifts[id] ?: mutableListOf()
        val result = if (limit == null) list else list.takeLast(limit)
        return success(result)
    }

    override suspend fun sendGift(id: String, request: GiftSendRequest): BaseApiResponse<GiftSendDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val currentBalance = coins[user.id] ?: 0L
        val multiplier = if (request.giftType?.contains("MULTI", ignoreCase = true) == true) {
            listOf(2, 3, 4, 5).random()
        } else {
            1
        }
        val totalAmount = request.amount * multiplier
        if (currentBalance < totalAmount) {
            return error("Insufficient balance")
        }
        val newBalance = currentBalance - totalAmount
        coins[user.id] = newBalance
        val recipients = when (request.target) {
            "ALL" -> roomSeats[id].orEmpty().mapNotNull { it.userId }.distinct()
            "SELECTED" -> request.recipientIds.orEmpty()
            else -> listOfNotNull(request.recipientId)
        }
        val gift = GiftSendDto(
            id = "gift_${UUID.randomUUID().toString().take(6)}",
            roomId = id,
            senderId = user.id,
            recipientId = recipients.firstOrNull(),
            amount = totalAmount,
            senderBalance = newBalance,
            createdAt = System.currentTimeMillis()
        )
        roomGifts.getOrPut(id) { mutableListOf() }.add(gift)
        val rewardMessage = RoomMessageDto(
            id = "reward_${UUID.randomUUID().toString().take(6)}",
            roomId = id,
            userId = user.id,
            username = user.nickname ?: user.username,
            message = "Gift ${request.giftId ?: "gift"} x$multiplier sent (${totalAmount} coins)",
            createdAt = System.currentTimeMillis(),
            type = "REWARD"
        )
        roomMessages.getOrPut(id) { mutableListOf() }.add(rewardMessage)
        maybeAddCommission(user, totalAmount)
        return success(gift)
    }

    override suspend fun getGiftCatalog(): BaseApiResponse<List<GiftCatalogDto>> {
        return success(giftCatalog.toList())
    }

    override suspend fun createGameSession(request: GameSessionRequest): BaseApiResponse<GameSessionResponse> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val response = GameSessionResponse(
            roomId = request.roomId,
            userId = user.id,
            sessionId = UUID.randomUUID().toString(),
            expiresAt = System.currentTimeMillis() + 5 * 60_000
        )
        return success(response)
    }

    override suspend fun getInboxThreads(): BaseApiResponse<List<InboxThreadDto>> {
        return success(inboxThreads)
    }

    override suspend fun sendInboxMessage(request: InboxMessageRequest): BaseApiResponse<InboxMessageResponse> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val response = InboxMessageResponse(
            id = "msg_${UUID.randomUUID().toString().take(6)}",
            threadId = "thread_${request.recipientId.take(6)}",
            senderId = user.id,
            recipientId = request.recipientId,
            message = request.message,
            createdAt = System.currentTimeMillis()
        )
        return success(response)
    }

    override suspend fun markInboxThreadRead(id: String): BaseApiResponse<Unit> {
        return success(Unit)
    }

    override suspend fun getFriends(): BaseApiResponse<List<FriendDto>> {
        return success(friends)
    }

    override suspend fun searchFriends(query: String): BaseApiResponse<List<FriendDto>> {
        val result = friends.filter { friend ->
            friend.username.contains(query, ignoreCase = true) ||
                (friend.nickname?.contains(query, ignoreCase = true) == true)
        }
        return success(result)
    }

    override suspend fun addFriend(id: String): BaseApiResponse<Unit> {
        return success(Unit)
    }

    override suspend fun createFamily(request: FamilyCreateRequest): BaseApiResponse<FamilyDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val created = FamilyDto(
            id = "family_${UUID.randomUUID().toString().take(6)}",
            name = request.name,
            code = "ABC123",
            ownerId = user.id
        )
        family = created
        familyMembers.clear()
        familyMembers.add(FamilyMemberDto(userId = user.id, username = user.username, nickname = user.nickname, role = "OWNER"))
        return success(created)
    }

    override suspend fun joinFamily(request: FamilyJoinRequest): BaseApiResponse<FamilyDto> {
        val existing = family ?: return error("Family not found")
        return success(existing)
    }

    override suspend fun getMyFamily(): BaseApiResponse<FamilyDto?> {
        return success(family)
    }

    override suspend fun getFamilyMembers(id: String): BaseApiResponse<List<FamilyMemberDto>> {
        return success(familyMembers)
    }

    override suspend fun getFamilyRooms(id: String): BaseApiResponse<List<FamilyRoomDto>> {
        return success(familyRooms)
    }

    override suspend fun createFamilyRoom(id: String, request: RoomCreateRequest): BaseApiResponse<RoomDto> {
        val room = RoomDto(
            id = "room_${UUID.randomUUID().toString().take(6)}",
            name = request.name,
            isActive = true,
            seatMode = request.seatMode,
            participantCount = 0,
            maxSeats = request.maxSeats,
            requiresPassword = request.password != null,
            country = request.country ?: "Global"
        )
        rooms[room.id] = room
        roomSeats[room.id] = buildSeats(room.maxSeats)
        roomMessages[room.id] = mutableListOf()
        roomGifts[room.id] = mutableListOf()
        familyRooms.add(FamilyRoomDto(room.id, room.name, if (room.isActive) "active" else "closed"))
        return success(room)
    }

    override suspend fun assignRoomFamily(id: String, familyId: String): BaseApiResponse<RoomDto> {
        val room = rooms[id] ?: return error("Room not found")
        return success(room)
    }

    override suspend fun getAdminGlobalConfig(): BaseApiResponse<AdminGlobalConfigDto?> {
        return success(adminGlobalConfig)
    }

    override suspend fun setAdminGlobalConfig(request: AdminGlobalConfigDto): BaseApiResponse<AdminGlobalConfigDto> {
        adminGlobalConfig = request
        addAdminAuditLog("GLOBAL_CONFIG_UPDATE", "RTP ${request.rtp} | House ${request.houseEdge}")
        return success(request)
    }

    override suspend fun getAdminGameConfigs(): BaseApiResponse<List<AdminGameConfigDto>> {
        return success(adminGameConfigs.toList())
    }

    override suspend fun upsertAdminGameConfig(request: AdminGameConfigDto): BaseApiResponse<AdminGameConfigDto> {
        adminGameConfigs.removeAll { it.id == request.id }
        adminGameConfigs.add(request)
        addAdminAuditLog("GAME_CONFIG_UPSERT", "${request.gameName} RTP ${request.rtp}")
        return success(request)
    }

    override suspend fun deleteAdminGameConfig(id: String): BaseApiResponse<Unit> {
        adminGameConfigs.removeAll { it.id == id }
        addAdminAuditLog("GAME_CONFIG_DELETE", id)
        return success(Unit)
    }

    override suspend fun getAdminUserConfigs(): BaseApiResponse<List<AdminUserConfigDto>> {
        return success(adminUserConfigs.toList())
    }

    override suspend fun upsertAdminUserConfig(request: AdminUserConfigDto): BaseApiResponse<AdminUserConfigDto> {
        adminUserConfigs.removeAll { it.id == request.id }
        adminUserConfigs.add(request)
        addAdminAuditLog("USER_CONFIG_UPSERT", "${request.userId} ${request.qualification}")
        return success(request)
    }

    override suspend fun deleteAdminUserConfig(id: String): BaseApiResponse<Unit> {
        adminUserConfigs.removeAll { it.id == id }
        addAdminAuditLog("USER_CONFIG_DELETE", id)
        return success(Unit)
    }

    override suspend fun getAdminQualificationConfigs(): BaseApiResponse<List<AdminQualificationConfigDto>> {
        return success(adminQualificationConfigs.toList())
    }

    override suspend fun upsertAdminQualificationConfig(request: AdminQualificationConfigDto): BaseApiResponse<AdminQualificationConfigDto> {
        adminQualificationConfigs.removeAll { it.id == request.id }
        adminQualificationConfigs.add(request)
        addAdminAuditLog("QUALIFICATION_UPSERT", request.qualification)
        return success(request)
    }

    override suspend fun getAdminLockRules(): BaseApiResponse<List<AdminLockRuleDto>> {
        return success(adminLockRules.toList())
    }

    override suspend fun upsertAdminLockRule(request: AdminLockRuleDto): BaseApiResponse<AdminLockRuleDto> {
        adminLockRules.removeAll { it.id == request.id }
        adminLockRules.add(request)
        addAdminAuditLog("LOCK_RULE_UPSERT", request.name)
        return success(request)
    }

    override suspend fun deleteAdminLockRule(id: String): BaseApiResponse<Unit> {
        adminLockRules.removeAll { it.id == id }
        addAdminAuditLog("LOCK_RULE_DELETE", id)
        return success(Unit)
    }

    override suspend fun getAdminAuditLogs(): BaseApiResponse<List<AdminAuditLogDto>> {
        return success(adminAuditLogs.toList())
    }

    override suspend fun applyForAgency(request: AgencyApplicationRequestDto): BaseApiResponse<AgencyApplicationDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val application = AgencyApplicationDto(
            id = "agency_${UUID.randomUUID().toString().take(6)}",
            userId = user.id,
            agencyName = request.agencyName,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        agencyApplications.add(application)
        return success(application)
    }

    override suspend fun applyForReseller(): BaseApiResponse<ResellerApplicationDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val application = ResellerApplicationDto(
            id = "reseller_${UUID.randomUUID().toString().take(6)}",
            userId = user.id,
            status = "PENDING",
            createdAt = System.currentTimeMillis()
        )
        resellerApplications.add(application)
        return success(application)
    }

    override suspend fun getAgencyApplications(): BaseApiResponse<List<AgencyApplicationDto>> {
        return success(agencyApplications.toList())
    }

    override suspend fun getMyAgencyApplications(): BaseApiResponse<List<AgencyApplicationDto>> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        return success(agencyApplications.filter { it.userId == user.id })
    }

    override suspend fun approveAgencyApplication(id: String): BaseApiResponse<Unit> {
        val index = agencyApplications.indexOfFirst { it.id == id }
        if (index == -1) return error("Application not found")
        val current = agencyApplications[index]
        agencyApplications[index] = current.copy(status = "APPROVED", reviewedAt = System.currentTimeMillis())
        return success(Unit)
    }

    override suspend fun rejectAgencyApplication(id: String): BaseApiResponse<Unit> {
        val index = agencyApplications.indexOfFirst { it.id == id }
        if (index == -1) return error("Application not found")
        val current = agencyApplications[index]
        agencyApplications[index] = current.copy(status = "REJECTED", reviewedAt = System.currentTimeMillis())
        return success(Unit)
    }

    override suspend fun getMyResellerApplications(): BaseApiResponse<List<ResellerApplicationDto>> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        return success(resellerApplications.filter { it.userId == user.id })
    }

    override suspend fun createTeam(request: TeamCreateRequestDto): BaseApiResponse<TeamDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val team = TeamDto(
            id = "team_${UUID.randomUUID().toString().take(6)}",
            name = request.name,
            ownerUserId = user.id,
            agencyId = user.agencyId
        )
        teams[team.id] = team
        teamMembers.getOrPut(team.id) { mutableSetOf() }.add(user.id)
        return success(team)
    }

    override suspend fun listTeams(): BaseApiResponse<List<TeamDto>> {
        return success(teams.values.toList())
    }

    override suspend fun joinTeam(teamId: String): BaseApiResponse<Unit> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        teamMembers.getOrPut(teamId) { mutableSetOf() }.add(user.id)
        return success(Unit)
    }

    override suspend fun leaveTeam(teamId: String): BaseApiResponse<Unit> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        teamMembers.getOrPut(teamId) { mutableSetOf() }.remove(user.id)
        return success(Unit)
    }

    override suspend fun getMyCommissions(limit: Int?): BaseApiResponse<List<AgencyCommissionDto>> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val list = commissions.filter { it.userId == user.id }
        val result = if (limit == null) list else list.take(limit)
        return success(result)
    }

    override suspend fun getAgencyRooms(): BaseApiResponse<List<AgencyRoomDto>> {
        if (agencyRooms.isEmpty()) {
            agencyRooms.addAll(
                rooms.values.take(5).map { AgencyRoomDto(it.id, it.name, if (it.isActive) "Active" else "Closed") }
            )
        }
        return success(agencyRooms.toList())
    }

    override suspend fun getAgencyHosts(): BaseApiResponse<List<AgencyHostDto>> {
        if (agencyHosts.isEmpty()) {
            agencyHosts.addAll(
                users.values.take(5).map {
                    AgencyHostDto(it.id, it.nickname ?: it.username, (coins[it.id] ?: 0L) / 100)
                }
            )
        }
        return success(agencyHosts.toList())
    }

    override suspend fun getResellerSellers(): BaseApiResponse<List<ResellerSellerDto>> {
        return success(resellerSellers.toList())
    }

    override suspend fun addResellerSeller(request: ResellerSellerRequestDto): BaseApiResponse<ResellerSellerDto> {
        val seller = ResellerSellerDto(
            id = "rs_${UUID.randomUUID().toString().take(6)}",
            sellerId = request.sellerId,
            createdAt = System.currentTimeMillis()
        )
        resellerSellers.add(seller)
        return success(seller)
    }

    override suspend fun getResellerSellerLimits(sellerId: String): BaseApiResponse<ResellerSellerLimitDto?> {
        val limit = resellerLimits.find { it.sellerId == sellerId }
        return success(limit)
    }

    override suspend fun setResellerSellerLimits(
        sellerId: String,
        request: ResellerSellerLimitRequestDto
    ): BaseApiResponse<ResellerSellerLimitDto> {
        val updated = ResellerSellerLimitDto(
            sellerId = sellerId,
            totalLimit = request.totalLimit,
            dailyLimit = request.dailyLimit,
            updatedAt = System.currentTimeMillis()
        )
        resellerLimits.removeAll { it.sellerId == sellerId }
        resellerLimits.add(updated)
        return success(updated)
    }

    override suspend fun getResellerSales(): BaseApiResponse<List<ResellerSaleDto>> {
        return success(resellerSales.toList())
    }

    override suspend fun createResellerSale(request: ResellerSaleRequestDto): BaseApiResponse<ResellerSaleDto> {
        val sale = ResellerSaleDto(
            id = "sale_${UUID.randomUUID().toString().take(6)}",
            saleId = request.saleId,
            sellerId = request.sellerId,
            buyerId = request.buyerId,
            amount = request.amount,
            currency = request.currency,
            destinationAccount = request.destinationAccount,
            createdAt = System.currentTimeMillis()
        )
        resellerSales.add(sale)
        return success(sale)
    }

    override suspend fun getResellerProofs(): BaseApiResponse<List<ResellerProofDto>> {
        return success(resellerProofs.toList())
    }

    override suspend fun createResellerProof(request: ResellerProofRequestDto): BaseApiResponse<ResellerProofDto> {
        val proof = ResellerProofDto(
            id = "proof_${UUID.randomUUID().toString().take(6)}",
            uri = request.uri,
            amount = request.amount,
            date = request.date,
            beneficiary = request.beneficiary,
            note = request.note,
            createdAt = System.currentTimeMillis()
        )
        resellerProofs.add(proof)
        return success(proof)
    }

    override suspend fun sendResellerCoins(request: ResellerSendCoinsRequestDto): BaseApiResponse<ResellerSendCoinsResponseDto> {
        val user = getCurrentUserInternal() ?: return error("No active user")
        val currentBalance = coins[user.id] ?: 0L
        if (currentBalance < request.amount) {
            return error("Insufficient balance")
        }
        val newBalance = currentBalance - request.amount
        coins[user.id] = newBalance
        return success(ResellerSendCoinsResponseDto(balance = newBalance))
    }

    override suspend fun searchAll(query: String): BaseApiResponse<SearchResultDto> {
        val normalized = query.trim()
        if (normalized.isBlank()) {
            return success(SearchResultDto())
        }
        val roomsResult = rooms.values.filter {
            it.id.contains(normalized, ignoreCase = true) ||
                it.name.contains(normalized, ignoreCase = true)
        }
        val usersResult = users.values.filter {
            it.id.contains(normalized, ignoreCase = true) ||
                it.username.contains(normalized, ignoreCase = true) ||
                (it.nickname?.contains(normalized, ignoreCase = true) == true)
        }
        val agenciesResult = agencies.filter {
            it.id.contains(normalized, ignoreCase = true) ||
                it.name.contains(normalized, ignoreCase = true)
        }
        return success(
            SearchResultDto(
                rooms = roomsResult,
                users = usersResult,
                agencies = agenciesResult
            )
        )
    }

    private fun seedDefaults() {
        val admin = createUser("admin", "admin@kappa.local", "admin")
        val agency = createUser("agency", "agency@kappa.local", "agency")
        val reseller = createUser("reseller", "reseller@kappa.local", "reseller")
        val user = createUser("user", "user@kappa.local", "user")
        currentUserId = user.id
        val roomA = RoomDto(
            id = "room_001",
            name = "Sala VIP",
            isActive = true,
            seatMode = "FREE",
            participantCount = 18,
            maxSeats = 28,
            requiresPassword = false,
            country = "UAE",
            region = "Middle East",
            agencyName = "Kappa Agency",
            agencyIconUrl = null,
            roomCode = "VIP-001",
            isFavorite = false
        )
        val roomB = RoomDto(
            id = "room_002",
            name = "Sala Favorito",
            isActive = true,
            seatMode = "FREE",
            participantCount = 12,
            maxSeats = 28,
            requiresPassword = false,
            country = "Bahrain",
            region = "Middle East",
            agencyName = "Kappa Agency",
            agencyIconUrl = null,
            roomCode = "FAV-002",
            isFavorite = false
        )
        rooms[roomA.id] = roomA
        rooms[roomB.id] = roomB
        roomSeats[roomA.id] = buildSeats(roomA.maxSeats)
        roomSeats[roomB.id] = buildSeats(roomB.maxSeats)
        roomMessages[roomA.id] = mutableListOf()
        roomMessages[roomB.id] = mutableListOf()
        roomGifts[roomA.id] = mutableListOf()
        roomGifts[roomB.id] = mutableListOf()
        coins[admin.id] = 500_000
        coins[agency.id] = 250_000
        coins[reseller.id] = 250_000
        coins[user.id] = 120_000
        giftCatalog.addAll(
            listOf(
                GiftCatalogDto("gift_rose", "Rose", "INDIVIDUAL", 100, 100, true, "Unique"),
                GiftCatalogDto("gift_wave", "Wave", "GROUP_FIXED", 20, 100, true, "Unique"),
                GiftCatalogDto("gift_mult", "Multiplier", "GROUP_MULTIPLIER", 20, 10, true, "Multiplier")
            )
        )
        homeBanners.addAll(
            listOf(
                HomeBannerDto("banner_01", "More rooms to choose", "Join the most popular lounges", "local://banner/1", "rooms", "popular"),
                HomeBannerDto("banner_02", "Daily rewards", "Check in to win prizes", "local://banner/2", "rewards", "daily"),
                HomeBannerDto("banner_03", "Mini game rush", "Play and win coins", "local://banner/3", "games", "hub")
            )
        )
        miniGames.addAll(
            listOf(
                MiniGameDto("lucky_draw", "Lucky Draw", "Spin and win rewards", 200),
                MiniGameDto("battle_arena", "Battle Arena", "Score more in 30 seconds", 300),
                MiniGameDto("gift_rush", "Gift Rush", "Send gifts to climb the rank", 500)
            )
        )
        agencies.addAll(
            listOf(
                AgencySummaryDto("agency_01", "Kappa Agency", "UAE"),
                AgencySummaryDto("agency_02", "Desert Stars", "Bahrain"),
                AgencySummaryDto("agency_03", "Nova Agency", "Brazil")
            )
        )
        agencyApplications.add(
            AgencyApplicationDto(
                id = "agency_${UUID.randomUUID().toString().take(6)}",
                userId = agency.id,
                agencyName = "Kappa Agency",
                status = "PENDING",
                createdAt = System.currentTimeMillis()
            )
        )
        agencyRooms.addAll(
            listOf(
                AgencyRoomDto("room_001", "Sala VIP", "Active"),
                AgencyRoomDto("room_002", "Sala Favorito", "Active")
            )
        )
        agencyHosts.addAll(
            listOf(
                AgencyHostDto(agency.id, agency.nickname ?: agency.username, 1200),
                AgencyHostDto(admin.id, admin.nickname ?: admin.username, 800)
            )
        )
        resellerSellers.addAll(
            listOf(
                ResellerSellerDto("rs_001", "seller_alpha", System.currentTimeMillis()),
                ResellerSellerDto("rs_002", "seller_beta", System.currentTimeMillis())
            )
        )
        resellerLimits.add(
            ResellerSellerLimitDto(
                sellerId = "seller_alpha",
                totalLimit = 50000,
                dailyLimit = 5000,
                updatedAt = System.currentTimeMillis()
            )
        )
        resellerSales.add(
            ResellerSaleDto(
                id = "sale_001",
                saleId = "SL-001",
                sellerId = "seller_alpha",
                buyerId = user.id,
                amount = 1200,
                currency = "USD",
                destinationAccount = "ACC-001",
                createdAt = System.currentTimeMillis()
            )
        )
        resellerProofs.add(
            ResellerProofDto(
                id = "proof_001",
                uri = "local://proof_001",
                amount = 1200,
                date = "2026-02-12",
                beneficiary = "Kappa LLC",
                note = "Seed proof",
                createdAt = System.currentTimeMillis()
            )
        )
        seedAdminDefaults()
    }

    private fun seedAdminDefaults() {
        adminGameConfigs.clear()
        adminGameConfigs.addAll(
            listOf(
                AdminGameConfigDto("bean_growth", "Bean Growth", 70.0, 30.0),
                AdminGameConfigDto("egg_smash", "Egg Smash", 70.0, 30.0),
                AdminGameConfigDto("slot_x", "Slot", 70.0, 30.0),
                AdminGameConfigDto("crash_x", "Crash", 70.0, 30.0),
                AdminGameConfigDto("roulette_x", "Roulette", 70.0, 30.0),
                AdminGameConfigDto("lucky77", "Lucky77", 70.0, 30.0)
            )
        )
        adminUserConfigs.clear()
        adminUserConfigs.add(AdminUserConfigDto("u1", "user_001", "VIP", 97.0, 3.0))
        adminQualificationConfigs.clear()
        adminQualificationConfigs.addAll(
            listOf(
                AdminQualificationConfigDto("q_normal", "normal", 70.0, 30.0, 0, 0),
                AdminQualificationConfigDto("q_vip1", "VIP1", 70.0, 30.0, 500, 10)
            )
        )
        adminLockRules.clear()
        adminLockRules.addAll(
            listOf(
                AdminLockRuleDto("r1", "Withdrawal cooldown", 30, 0, 0, 0, 0, "user", listOf("withdrawals")),
                AdminLockRuleDto("r2", "Min turnover", 0, 1000, 0, 0, 0, "user", listOf("withdrawals", "gifts"))
            )
        )
        adminAuditLogs.clear()
        addAdminAuditLog("SEED", "Admin defaults loaded")
    }

    private fun createUser(
        username: String,
        email: String,
        role: String,
        phone: String? = null,
        nickname: String? = null,
        avatarUrl: String? = null,
        agencyId: String? = null,
        isGuest: Boolean = false
    ): UserDto {
        val id = "u_${UUID.randomUUID().toString().take(8)}"
        val user = UserDto(
            id = id,
            username = username,
            email = email,
            role = role,
            phone = phone,
            nickname = nickname ?: username,
            avatarUrl = avatarUrl,
            country = "Brazil",
            language = "pt",
            isGuest = isGuest,
            agencyId = agencyId
        )
        users[id] = user
        usernames[username] = id
        emails[email] = id
        if (!phone.isNullOrBlank()) {
            phones[phone] = id
        }
        coins[id] = coins[id] ?: 100_000
        return user
    }

    private fun buildLoginResponse(user: UserDto): LoginResponse {
        return LoginResponse(
            accessToken = "token_${UUID.randomUUID().toString().take(10)}",
            refreshToken = "refresh_${UUID.randomUUID().toString().take(10)}",
            user = user
        )
    }

    private fun getCurrentUserInternal(): UserDto? {
        return currentUserId?.let { users[it] }
    }

    private fun buildSeats(maxSeats: Int): MutableList<RoomSeatDto> {
        val list = mutableListOf<RoomSeatDto>()
        for (i in 1..maxSeats) {
            list.add(RoomSeatDto(seatNumber = i, status = "FREE"))
        }
        return list
    }

    private fun clearUserFromSeats(roomId: String, userId: String) {
        val seats = roomSeats[roomId] ?: return
        for (i in seats.indices) {
            if (seats[i].userId == userId) {
                seats[i] = seats[i].copy(status = "FREE", userId = null, username = null)
            }
        }
    }

    private fun maybeAddCommission(user: UserDto, amount: Long) {
        val agencyId = user.agencyId ?: return
        val diamonds = (amount * 0.10).toLong()
        val commissionUsd = String.format("%.2f", (amount / 10.0) * 2.2 / 10.0)
        commissions.add(
            AgencyCommissionDto(
                id = "comm_${UUID.randomUUID().toString().take(6)}",
                agencyId = agencyId,
                userId = user.id,
                diamondsAmount = diamonds,
                commissionUsd = commissionUsd,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    private fun addAdminAuditLog(action: String, message: String?) {
        val actorId = currentUserId ?: "system"
        adminAuditLogs.add(
            0,
            AdminAuditLogDto(
                id = "log_${UUID.randomUUID().toString().take(8)}",
                actorId = actorId,
                action = action,
                message = message,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    private fun resolveRegion(country: String?): String? {
        if (country.isNullOrBlank()) return null
        return when (country.lowercase()) {
            "uae", "saudi arabia", "bahrain", "qatar", "kuwait", "oman" -> "Middle East"
            "brazil", "united states", "canada", "mexico" -> "Americas"
            "france", "germany", "spain", "italy", "united kingdom" -> "Europe"
            "india", "china", "japan", "indonesia", "philippines" -> "Asia"
            "nigeria", "south africa", "egypt", "kenya" -> "Africa"
            else -> "Global"
        }
    }

    private fun <T> success(data: T): BaseApiResponse<T> {
        return BaseApiResponse(success = true, data = data)
    }

    private fun <T> error(message: String): BaseApiResponse<T> {
        return BaseApiResponse(success = false, data = null, error = message, message = message)
    }
}
