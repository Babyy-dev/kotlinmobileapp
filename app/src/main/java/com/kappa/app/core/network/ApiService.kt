package com.kappa.app.core.network

import com.kappa.app.core.network.model.CoinBalanceDto
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
import okhttp3.MultipartBody
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
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

    @GET("rooms")
    suspend fun getRooms(): BaseApiResponse<List<RoomDto>>

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

    @POST("rooms/{id}/join")
    suspend fun joinRoom(@Path("id") id: String, @Body request: JoinRoomRequest? = null): BaseApiResponse<JoinRoomDto>

    @POST("rooms/{id}/leave")
    suspend fun leaveRoom(@Path("id") id: String): BaseApiResponse<Unit>

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
}
