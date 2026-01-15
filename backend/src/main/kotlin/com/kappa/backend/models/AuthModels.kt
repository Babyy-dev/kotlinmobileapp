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
    val agencyId: String? = null
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
