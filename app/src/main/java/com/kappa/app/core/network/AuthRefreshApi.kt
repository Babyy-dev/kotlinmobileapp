package com.kappa.app.core.network

import com.kappa.app.core.network.model.LoginResponse
import com.kappa.app.core.network.model.RefreshRequest
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthRefreshApi {
    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequest): BaseApiResponse<LoginResponse>
}
