package com.kappa.app.user.domain.repository

import com.kappa.app.domain.user.User

/**
 * User repository interface.
 */
interface UserRepository {
    suspend fun getUser(userId: String): Result<User>
    suspend fun updateUser(user: User): Result<User>
    suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): Result<User>
}
