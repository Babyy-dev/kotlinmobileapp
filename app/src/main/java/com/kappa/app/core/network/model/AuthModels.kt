package com.kappa.app.core.network.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class SignupRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: String? = null,
    val agencyId: String? = null,
    val phone: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null
)

data class SignupResponse(
    val userId: String,
    val otp: PhoneOtpResponse
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)

data class PhoneOtpRequest(
    val phone: String
)

data class PhoneOtpVerifyRequest(
    val phone: String,
    val code: String
)

data class PhoneOtpResponse(
    val phone: String,
    val code: String,
    val expiresAt: Long
)

data class GuestLoginRequest(
    val nickname: String? = null,
    val avatarUrl: String? = null
)

data class ProfileUpdateRequest(
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null
)
