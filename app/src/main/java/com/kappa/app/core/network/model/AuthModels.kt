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
    val agencyId: String? = null
)

data class LoginResponse(
    val accessToken: String,
    val refreshToken: String,
    val user: UserDto
)
