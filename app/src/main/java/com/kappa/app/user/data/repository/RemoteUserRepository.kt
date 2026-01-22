package com.kappa.app.user.data.repository

import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.ProfileUpdateRequest
import com.kappa.app.core.network.model.toDomain
import com.kappa.app.domain.user.User
import com.kappa.app.user.domain.repository.UserRepository
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
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
        return try {
            val response = apiService.updateProfile(
                ProfileUpdateRequest(
                    nickname = user.nickname,
                    avatarUrl = user.avatarUrl,
                    country = user.country,
                    language = user.language
                )
            )
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Profile update failed"))
            } else {
                Result.success(data.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }

    override suspend fun uploadAvatar(bytes: ByteArray, fileName: String, mimeType: String): Result<User> {
        return try {
            val body = bytes.toRequestBody(mimeType.toMediaTypeOrNull())
            val part = MultipartBody.Part.createFormData("avatar", fileName, body)
            val response = apiService.uploadAvatar(part)
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Avatar upload failed"))
            } else {
                Result.success(data.toDomain())
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }
}
