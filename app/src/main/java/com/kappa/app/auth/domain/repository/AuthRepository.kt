package com.kappa.app.auth.domain.repository

import com.kappa.app.auth.domain.model.OtpInfo
import com.kappa.app.domain.user.User

/**
 * Authentication repository interface.
 */
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun signup(username: String, email: String, password: String, phone: String): Result<OtpInfo>
    suspend fun requestOtp(phone: String): Result<OtpInfo>
    suspend fun verifyOtp(phone: String, code: String): Result<User>
    suspend fun guestLogin(): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun isLoggedIn(): Boolean
}
