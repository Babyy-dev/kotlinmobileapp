package com.kappa.app.core.network

import com.kappa.app.core.network.model.CoinBalanceDto
import com.kappa.app.core.network.model.JoinRoomDto
import com.kappa.app.core.network.model.LoginRequest
import com.kappa.app.core.network.model.LoginResponse
import com.kappa.app.core.network.model.RefreshRequest
import com.kappa.app.core.network.model.RoomDto
import com.kappa.app.core.network.model.SignupRequest
import com.kappa.app.core.network.model.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/**
 * Base API service interface.
 */
interface ApiService {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): BaseApiResponse<LoginResponse>

    @POST("auth/signup")
    suspend fun signup(@Body request: SignupRequest): BaseApiResponse<LoginResponse>

    @POST("auth/logout")
    suspend fun logout(@Body request: RefreshRequest): BaseApiResponse<Unit>

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): BaseApiResponse<LoginResponse>

    @GET("users/me")
    suspend fun getCurrentUser(): BaseApiResponse<UserDto>

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: String): BaseApiResponse<UserDto>

    @GET("coins/balance")
    suspend fun getCoinBalance(): BaseApiResponse<CoinBalanceDto>

    @GET("rooms")
    suspend fun getRooms(): BaseApiResponse<List<RoomDto>>

    @POST("rooms/{id}/join")
    suspend fun joinRoom(@Path("id") id: String): BaseApiResponse<JoinRoomDto>

    @POST("rooms/{id}/leave")
    suspend fun leaveRoom(@Path("id") id: String): BaseApiResponse<Unit>
}
