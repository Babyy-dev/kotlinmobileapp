package com.kappa.app.auth.data.repository

import com.kappa.app.auth.domain.model.OtpInfo
import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.GuestLoginRequest
import com.kappa.app.core.network.model.LoginRequest
import com.kappa.app.core.network.model.PhoneOtpRequest
import com.kappa.app.core.network.model.PhoneOtpVerifyRequest
import com.kappa.app.core.network.model.RefreshRequest
import com.kappa.app.core.network.model.SignupRequest
import com.kappa.app.core.network.model.toDomain
import com.kappa.app.core.storage.PreferencesManager
import com.kappa.app.domain.user.User
import retrofit2.HttpException
import javax.inject.Inject

class RemoteAuthRepository @Inject constructor(
    private val apiService: ApiService,
    private val preferencesManager: PreferencesManager,
    private val errorMapper: ErrorMapper
) : AuthRepository {
    override suspend fun login(username: String, password: String): Result<User> {
        return try {
            val response = apiService.login(LoginRequest(username, password))
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Login failed"))
            } else {
                preferencesManager.saveAccessToken(data.accessToken)
                preferencesManager.saveRefreshToken(data.refreshToken)
                preferencesManager.saveUserId(data.user.id)
                Result.success(data.user.toDomain())
            }
        } catch (throwable: Throwable) {
            if (throwable is HttpException && throwable.code() == 401) {
                return Result.failure(Exception("Invalid credentials"))
            }
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun signup(username: String, email: String, password: String): Result<User> {
        return try {
            val response = apiService.signup(SignupRequest(username = username, email = email, password = password))
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Signup failed"))
            } else {
                preferencesManager.saveAccessToken(data.accessToken)
                preferencesManager.saveRefreshToken(data.refreshToken)
                preferencesManager.saveUserId(data.user.id)
                Result.success(data.user.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun requestOtp(phone: String): Result<OtpInfo> {
        return try {
            val response = apiService.requestOtp(PhoneOtpRequest(phone))
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "OTP request failed"))
            } else {
                Result.success(data.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun verifyOtp(phone: String, code: String): Result<User> {
        return try {
            val response = apiService.verifyOtp(PhoneOtpVerifyRequest(phone, code))
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "OTP verification failed"))
            } else {
                preferencesManager.saveAccessToken(data.accessToken)
                preferencesManager.saveRefreshToken(data.refreshToken)
                preferencesManager.saveUserId(data.user.id)
                Result.success(data.user.toDomain())
            }
        } catch (throwable: Throwable) {
            if (throwable is HttpException && throwable.code() == 401) {
                return Result.failure(Exception("Invalid or expired OTP"))
            }
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun guestLogin(): Result<User> {
        return try {
            val response = apiService.guestLogin(GuestLoginRequest())
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Guest login failed"))
            } else {
                preferencesManager.saveAccessToken(data.accessToken)
                preferencesManager.saveRefreshToken(data.refreshToken)
                preferencesManager.saveUserId(data.user.id)
                Result.success(data.user.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun logout(): Result<Unit> {
        return try {
            val refreshToken = preferencesManager.getRefreshTokenOnce()
            if (!refreshToken.isNullOrBlank()) {
                runCatching { apiService.logout(RefreshRequest(refreshToken)) }
            }
            preferencesManager.clearAllTokens()
            Result.success(Unit)
        } catch (throwable: Throwable) {
            preferencesManager.clearAllTokens()
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun getCurrentUser(): User? {
        return try {
            val response = apiService.getCurrentUser()
            response.data?.toDomain()
        } catch (throwable: Throwable) {
            if (throwable is HttpException && throwable.code() == 401) {
                preferencesManager.clearAllTokens()
            }
            null
        }
    }

    override suspend fun isLoggedIn(): Boolean {
        return !preferencesManager.getAccessTokenOnce().isNullOrBlank()
    }
}
