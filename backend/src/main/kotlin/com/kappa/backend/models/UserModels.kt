package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    MASTER,
    RESELLER,
    AGENCY_OWNER,
    USER
}

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val agencyId: String? = null
)
