package com.kappa.app.user.domain.usecase

import com.kappa.app.domain.user.User
import com.kappa.app.user.domain.repository.UserRepository
import javax.inject.Inject

class UploadAvatarUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(bytes: ByteArray, fileName: String, mimeType: String): Result<User> {
        return userRepository.uploadAvatar(bytes, fileName, mimeType)
    }
}
