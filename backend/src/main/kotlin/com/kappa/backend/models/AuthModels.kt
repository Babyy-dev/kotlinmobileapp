package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val username: String,
    val password: String
)

@Serializable
data class SignupRequest(
    val username: String,
    val email: String,
    val password: String,
    val role: UserRole? = null,
    val agencyId: String? = null,
    val phone: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null
)

@Serializable
data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserResponse
)

@Serializable
data class RefreshRequest(
    val refreshToken: String
)

@Serializable
data class PhoneOtpRequest(
    val phone: String
)

@Serializable
data class PhoneOtpVerifyRequest(
    val phone: String,
    val code: String,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null
)

@Serializable
data class PhoneOtpResponse(
    val phone: String,
    val code: String,
    val expiresAt: Long
)

@Serializable
data class GuestLoginRequest(
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null
)

@Serializable
data class ProfileUpdateRequest(
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null
)
