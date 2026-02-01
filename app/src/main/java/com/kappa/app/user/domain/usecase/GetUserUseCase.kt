package com.kappa.app.user.domain.usecase

import com.kappa.app.domain.user.User
import com.kappa.app.user.domain.repository.UserRepository
import javax.inject.Inject

/**
 * UseCase for getting user information.
 */
class GetUserUseCase @Inject constructor(
    private val userRepository: UserRepository
) {
    suspend operator fun invoke(userId: String): Result<User> {
        return userRepository.getUser(userId)
    }
}
