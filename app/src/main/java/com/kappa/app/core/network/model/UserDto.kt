package com.kappa.app.core.network.model

data class UserDto(
    val id: String,
    val username: String,
    val email: String,
    val role: String,
    val phone: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null,
    val isGuest: Boolean = false,
    val agencyId: String? = null
)
