package com.kappa.app.user.data.repository

import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.toDomain
import com.kappa.app.domain.user.User
import com.kappa.app.user.domain.repository.UserRepository
import javax.inject.Inject

class RemoteUserRepository @Inject constructor(
    private val apiService: ApiService,
    private val errorMapper: ErrorMapper
) : UserRepository {
    override suspend fun getUser(userId: String): Result<User> {
        return try {
            val response = if (userId == "me") {
                apiService.getCurrentUser()
            } else {
                apiService.getUser(userId)
            }
            val user = response.data
            if (!response.success || user == null) {
                Result.failure(Exception(response.error ?: "User not found"))
            } else {
                Result.success(user.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun updateUser(user: User): Result<User> {
        return Result.failure(Exception("Not implemented"))
    }
}
