package com.kappa.app.core.network.model

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val agencyId: String? = null
)
