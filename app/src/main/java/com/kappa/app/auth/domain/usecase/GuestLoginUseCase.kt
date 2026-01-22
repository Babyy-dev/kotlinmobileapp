package com.kappa.app.auth.domain.usecase

import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.domain.user.User
import javax.inject.Inject

class GuestLoginUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): Result<User> {
        return authRepository.guestLogin()
    }
}
