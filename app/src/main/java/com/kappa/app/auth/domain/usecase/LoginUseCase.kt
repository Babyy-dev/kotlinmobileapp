package com.kappa.app.auth.domain.usecase

import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.domain.user.User
import javax.inject.Inject

/**
 * UseCase for login functionality.
 */
class LoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, password: String): Result<User> {
        return authRepository.login(username, password)
    }
}
