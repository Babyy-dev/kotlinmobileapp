package com.kappa.app.auth.domain.usecase

import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.domain.user.User
import javax.inject.Inject

class VerifyOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String, code: String): Result<User> {
        return authRepository.verifyOtp(phone, code)
    }
}
