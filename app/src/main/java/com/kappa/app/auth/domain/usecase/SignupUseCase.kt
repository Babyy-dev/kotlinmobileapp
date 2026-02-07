package com.kappa.app.auth.domain.usecase

import com.kappa.app.auth.domain.repository.AuthRepository
import com.kappa.app.auth.domain.model.OtpInfo
import javax.inject.Inject

/**
 * UseCase for signup functionality.
 */
class SignupUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(username: String, email: String, password: String, phone: String): Result<OtpInfo> {
        return authRepository.signup(username, email, password, phone)
    }
}
