package com.kappa.app.auth.domain.repository

import com.kappa.app.domain.user.User

/**
 * Authentication repository interface.
 */
interface AuthRepository {
    suspend fun login(username: String, password: String): Result<User>
    suspend fun signup(username: String, email: String, password: String): Result<User>
    suspend fun logout(): Result<Unit>
    suspend fun getCurrentUser(): User?
    suspend fun isLoggedIn(): Boolean
}
