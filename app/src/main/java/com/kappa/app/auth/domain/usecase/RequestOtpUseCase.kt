package com.kappa.app.auth.domain.usecase

import com.kappa.app.auth.domain.model.OtpInfo
import com.kappa.app.auth.domain.repository.AuthRepository
import javax.inject.Inject

class RequestOtpUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(phone: String): Result<OtpInfo> {
        return authRepository.requestOtp(phone)
    }
}
