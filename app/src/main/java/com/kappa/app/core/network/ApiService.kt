package com.kappa.app.core.network

import com.kappa.app.core.network.model.CoinBalanceDto
import com.kappa.app.core.network.model.CoinTransactionDto
import com.kappa.app.core.network.model.GuestLoginRequest
import com.kappa.app.core.network.model.GiftSendDto
import com.kappa.app.core.network.model.GiftSendRequest
import com.kappa.app.core.network.model.JoinRoomDto
import com.kappa.app.core.network.model.JoinRoomRequest
import com.kappa.app.core.network.model.LoginRequest
import com.kappa.app.core.network.model.LoginResponse
import com.kappa.app.core.network.model.PhoneOtpRequest
import com.kappa.app.core.network.model.PhoneOtpResponse
import com.kappa.app.core.network.model.PhoneOtpVerifyRequest
import com.kappa.app.core.network.model.ProfileUpdateRequest
import com.kappa.app.core.network.model.RefreshRequest
import com.kappa.app.core.network.model.RoomCreateRequest
import com.kappa.app.core.network.model.RoomDto
import com.kappa.app.core.network.model.RoomMessageDto
import com.kappa.app.core.network.model.RoomMessageRequest
import com.kappa.app.core.network.model.RoomSeatDto
import com.kappa.app.core.network.model.SignupRequest
import com.kappa.app.core.network.model.UserDto
import com.kappa.app.core.network.model.MuteParticipantRequest
import com.kappa.app.core.network.model.AgencyApplicationDto
import com.kappa.app.core.network.model.AgencyApplicationRequestDto
import com.kappa.app.core.network.model.AgencyCommissionDto
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
import com.kappa.app.core.network.model.ResellerApplicationDto
import com.kappa.app.core.network.model.TeamCreateRequestDto
import com.kappa.app.core.network.model.TeamDto
import com.kappa.app.core.network.model.ResellerSellerRequestDto
import com.kappa.app.core.network.model.ResellerSellerDto
import com.kappa.app.core.network.model.ResellerSellerLimitRequestDto
import com.kappa.app.core.network.model.ResellerSellerLimitDto
import com.kappa.app.core.network.model.ResellerSaleRequestDto
import com.kappa.app.core.network.model.ResellerSaleDto
import com.kappa.app.core.network.model.ResellerProofRequestDto
import com.kappa.app.core.network.model.ResellerProofDto
import com.kappa.app.core.network.model.ResellerSendCoinsRequestDto
import com.kappa.app.core.network.model.ResellerSendCoinsResponseDto
import com.kappa.app.core.network.model.GiftCatalogDto
import com.kappa.app.core.network.model.HomeBannerDto
import com.kappa.app.core.network.model.MiniGameDto
import com.kappa.app.core.network.model.BannerUploadResponseDto
import com.kappa.app.core.network.model.SearchResultDto
import com.kappa.app.core.network.model.AgencyRoomDto
import com.kappa.app.core.network.model.AgencyHostDto
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Base API service interface.
 */
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): BaseApiResponse<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): BaseApiResponse<LoginResponse>

    @POST("auth/otp/request")
    suspend fun requestOtp(@Body request: PhoneOtpRequest): BaseApiResponse<PhoneOtpResponse>

    @POST("auth/otp/verify")
    suspend fun verifyOtp(@Body request: PhoneOtpVerifyRequest): BaseApiResponse<LoginResponse>

    @POST("auth/guest")
    suspend fun guestLogin(@Body request: GuestLoginRequest): BaseApiResponse<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest): BaseApiResponse<Unit>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): BaseApiResponse<LoginResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): BaseApiResponse<UserDto>

    @POST("users/profile")
    suspend fun updateProfile(@Body request: ProfileUpdateRequest): BaseApiResponse<UserDto>

    @Multipart
    @POST("users/avatar")
    suspend fun uploadAvatar(@Part avatar: MultipartBody.Part): BaseApiResponse<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): BaseApiResponse<UserDto>

    @GET("coins/balance")
    suspend fun getCoinBalance(): BaseApiResponse<CoinBalanceDto>

    @GET("coins/transactions")
    suspend fun getCoinTransactions(@Query("limit") limit: Int? = null): BaseApiResponse<List<CoinTransactionDto>>

    @GET("rooms")
    suspend fun getRooms(): BaseApiResponse<List<RoomDto>>

    @GET("home/banners")
    suspend fun getHomeBanners(): BaseApiResponse<List<HomeBannerDto>>

    @GET("home/mini-games")
    suspend fun getPopularMiniGames(): BaseApiResponse<List<MiniGameDto>>

    @POST("rooms")
    suspend fun createRoom(@Body request: RoomCreateRequest): BaseApiResponse<RoomDto>

    @POST("rooms/{id}/close")
    suspend fun closeRoom(@Path("id") id: String): BaseApiResponse<Unit>

    @GET("rooms/{id}/seats")
    suspend fun getRoomSeats(@Path("id") id: String): BaseApiResponse<List<RoomSeatDto>>

    @POST("rooms/{id}/seats/{seat}/take")
    suspend fun takeSeat(@Path("id") id: String, @Path("seat") seat: Int): BaseApiResponse<Unit>

    @POST("rooms/{id}/seats/{seat}/leave")
    suspend fun leaveSeat(@Path("id") id: String, @Path("seat") seat: Int): BaseApiResponse<Unit>

    @POST("rooms/{id}/seats/{seat}/lock")
    suspend fun lockSeat(@Path("id") id: String, @Path("seat") seat: Int): BaseApiResponse<Unit>

    @POST("rooms/{id}/seats/{seat}/unlock")
    suspend fun unlockSeat(@Path("id") id: String, @Path("seat") seat: Int): BaseApiResponse<Unit>

    @POST("rooms/{id}/seats/{seat}/request")
    suspend fun requestSeat(@Path("id") id: String, @Path("seat") seat: Int): BaseApiResponse<Unit>

    @POST("rooms/{id}/join")
    suspend fun joinRoom(@Path("id") id: String, @Body request: JoinRoomRequest? = null): BaseApiResponse<JoinRoomDto>

    @POST("rooms/{id}/leave")
    suspend fun leaveRoom(@Path("id") id: String): BaseApiResponse<Unit>

    @POST("rooms/{id}/favorite")
    suspend fun toggleRoomFavorite(
        @Path("id") id: String,
        @Query("favorite") favorite: Boolean
    ): BaseApiResponse<RoomDto>

    @POST("rooms/{id}/participants/{userId}/mute")
    suspend fun muteParticipant(
        @Path("id") id: String,
        @Path("userId") userId: String,
        @Body request: MuteParticipantRequest
    ): BaseApiResponse<Unit>

    @POST("rooms/{id}/participants/{userId}/kick")
    suspend fun kickParticipant(@Path("id") id: String, @Path("userId") userId: String): BaseApiResponse<Unit>

    @POST("rooms/{id}/participants/{userId}/ban")
    suspend fun banParticipant(@Path("id") id: String, @Path("userId") userId: String): BaseApiResponse<Unit>

    @GET("rooms/{id}/messages")
    suspend fun getRoomMessages(
        @Path("id") id: String,
        @Query("limit") limit: Int? = null
    ): BaseApiResponse<List<RoomMessageDto>>

    @POST("rooms/{id}/messages")
    suspend fun sendRoomMessage(
        @Path("id") id: String,
        @Body request: RoomMessageRequest
    ): BaseApiResponse<RoomMessageDto>

    @GET("rooms/{id}/gifts")
    suspend fun getRoomGifts(
        @Path("id") id: String,
        @Query("limit") limit: Int? = null
    ): BaseApiResponse<List<GiftSendDto>>

    @POST("rooms/{id}/gifts")
    suspend fun sendGift(
        @Path("id") id: String,
        @Body request: GiftSendRequest
    ): BaseApiResponse<GiftSendDto>

    @GET("search")
    suspend fun searchAll(@Query("query") query: String): BaseApiResponse<SearchResultDto>

    @GET("gifts/catalog")
    suspend fun getGiftCatalog(): BaseApiResponse<List<GiftCatalogDto>>

    @POST("games/session")
    suspend fun createGameSession(@Body request: GameSessionRequest): BaseApiResponse<GameSessionResponse>

    @GET("inbox/threads")
    suspend fun getInboxThreads(): BaseApiResponse<List<InboxThreadDto>>

    @POST("inbox/message")
    suspend fun sendInboxMessage(@Body request: InboxMessageRequest): BaseApiResponse<InboxMessageResponse>

    @POST("inbox/threads/{id}/read")
    suspend fun markInboxThreadRead(@Path("id") id: String): BaseApiResponse<Unit>

    @GET("friends")
    suspend fun getFriends(): BaseApiResponse<List<FriendDto>>

    @GET("friends/search")
    suspend fun searchFriends(@Query("query") query: String): BaseApiResponse<List<FriendDto>>

    @POST("friends/{id}")
    suspend fun addFriend(@Path("id") id: String): BaseApiResponse<Unit>

    @POST("family")
    suspend fun createFamily(@Body request: FamilyCreateRequest): BaseApiResponse<FamilyDto>

    @POST("family/join")
    suspend fun joinFamily(@Body request: FamilyJoinRequest): BaseApiResponse<FamilyDto>

    @GET("family/me")
    suspend fun getMyFamily(): BaseApiResponse<FamilyDto?>

    @GET("family/{id}/members")
    suspend fun getFamilyMembers(@Path("id") id: String): BaseApiResponse<List<FamilyMemberDto>>

    @GET("family/{id}/rooms")
    suspend fun getFamilyRooms(@Path("id") id: String): BaseApiResponse<List<FamilyRoomDto>>

    @POST("family/{id}/rooms")
    suspend fun createFamilyRoom(
        @Path("id") id: String,
        @Body request: RoomCreateRequest
    ): BaseApiResponse<RoomDto>

    @POST("rooms/{id}/family/{familyId}")
    suspend fun assignRoomFamily(
        @Path("id") id: String,
        @Path("familyId") familyId: String
    ): BaseApiResponse<RoomDto>

    @GET("admin/config/global")
    suspend fun getAdminGlobalConfig(): BaseApiResponse<AdminGlobalConfigDto?>

    @POST("admin/config/global")
    suspend fun setAdminGlobalConfig(@Body request: AdminGlobalConfigDto): BaseApiResponse<AdminGlobalConfigDto>

    @GET("admin/config/games")
    suspend fun getAdminGameConfigs(): BaseApiResponse<List<AdminGameConfigDto>>

    @POST("admin/config/games")
    suspend fun upsertAdminGameConfig(@Body request: AdminGameConfigDto): BaseApiResponse<AdminGameConfigDto>

    @POST("admin/config/games/{id}/delete")
    suspend fun deleteAdminGameConfig(@Path("id") id: String): BaseApiResponse<Unit>

    @GET("admin/config/users")
    suspend fun getAdminUserConfigs(): BaseApiResponse<List<AdminUserConfigDto>>

    @POST("admin/config/users")
    suspend fun upsertAdminUserConfig(@Body request: AdminUserConfigDto): BaseApiResponse<AdminUserConfigDto>

    @POST("admin/config/users/{id}/delete")
    suspend fun deleteAdminUserConfig(@Path("id") id: String): BaseApiResponse<Unit>

    @GET("admin/config/qualifications")
    suspend fun getAdminQualificationConfigs(): BaseApiResponse<List<AdminQualificationConfigDto>>

    @POST("admin/config/qualifications")
    suspend fun upsertAdminQualificationConfig(@Body request: AdminQualificationConfigDto): BaseApiResponse<AdminQualificationConfigDto>

    @GET("admin/lock-rules")
    suspend fun getAdminLockRules(): BaseApiResponse<List<AdminLockRuleDto>>

    @POST("admin/lock-rules")
    suspend fun upsertAdminLockRule(@Body request: AdminLockRuleDto): BaseApiResponse<AdminLockRuleDto>

    @POST("admin/lock-rules/{id}/delete")
    suspend fun deleteAdminLockRule(@Path("id") id: String): BaseApiResponse<Unit>

    @GET("admin/audit-logs")
    suspend fun getAdminAuditLogs(): BaseApiResponse<List<AdminAuditLogDto>>

    @Multipart
    @POST("admin/banners/upload")
    suspend fun uploadAdminBanner(@Part file: MultipartBody.Part): BaseApiResponse<BannerUploadResponseDto>

    // Agency / reseller
    @POST("agency/apply")
    suspend fun applyForAgency(@Body request: AgencyApplicationRequestDto): BaseApiResponse<AgencyApplicationDto>

    @POST("reseller/apply")
    suspend fun applyForReseller(): BaseApiResponse<ResellerApplicationDto>

    @GET("agency/applications")
    suspend fun getAgencyApplications(): BaseApiResponse<List<AgencyApplicationDto>>

    @GET("agency/applications/me")
    suspend fun getMyAgencyApplications(): BaseApiResponse<List<AgencyApplicationDto>>

    @POST("agency/applications/{id}/approve")
    suspend fun approveAgencyApplication(@Path("id") id: String): BaseApiResponse<Unit>

    @POST("agency/applications/{id}/reject")
    suspend fun rejectAgencyApplication(@Path("id") id: String): BaseApiResponse<Unit>

    @GET("reseller/applications/me")
    suspend fun getMyResellerApplications(): BaseApiResponse<List<ResellerApplicationDto>>

    @POST("teams")
    suspend fun createTeam(@Body request: TeamCreateRequestDto): BaseApiResponse<TeamDto>

    @GET("teams")
    suspend fun listTeams(): BaseApiResponse<List<TeamDto>>

    @POST("teams/{id}/join")
    suspend fun joinTeam(@Path("id") teamId: String): BaseApiResponse<Unit>

    @POST("teams/{id}/leave")
    suspend fun leaveTeam(@Path("id") teamId: String): BaseApiResponse<Unit>

    @GET("commissions/me")
    suspend fun getMyCommissions(@Query("limit") limit: Int? = null): BaseApiResponse<List<AgencyCommissionDto>>

    @GET("agency/rooms")
    suspend fun getAgencyRooms(): BaseApiResponse<List<AgencyRoomDto>>

    @GET("agency/hosts")
    suspend fun getAgencyHosts(): BaseApiResponse<List<AgencyHostDto>>

    // Reseller tools
    @GET("reseller/sellers")
    suspend fun getResellerSellers(): BaseApiResponse<List<ResellerSellerDto>>

    @POST("reseller/sellers")
    suspend fun addResellerSeller(@Body request: ResellerSellerRequestDto): BaseApiResponse<ResellerSellerDto>

    @GET("reseller/sellers/{id}/limits")
    suspend fun getResellerSellerLimits(@Path("id") sellerId: String): BaseApiResponse<ResellerSellerLimitDto?>

    @PUT("reseller/sellers/{id}/limits")
    suspend fun setResellerSellerLimits(
        @Path("id") sellerId: String,
        @Body request: ResellerSellerLimitRequestDto
    ): BaseApiResponse<ResellerSellerLimitDto>

    @GET("reseller/sales")
    suspend fun getResellerSales(): BaseApiResponse<List<ResellerSaleDto>>

    @POST("reseller/sales")
    suspend fun createResellerSale(@Body request: ResellerSaleRequestDto): BaseApiResponse<ResellerSaleDto>

    @GET("reseller/proofs")
    suspend fun getResellerProofs(): BaseApiResponse<List<ResellerProofDto>>

    @POST("reseller/proofs")
    suspend fun createResellerProof(@Body request: ResellerProofRequestDto): BaseApiResponse<ResellerProofDto>

    @POST("reseller/send-coins")
    suspend fun sendResellerCoins(@Body request: ResellerSendCoinsRequestDto): BaseApiResponse<ResellerSendCoinsResponseDto>
}
