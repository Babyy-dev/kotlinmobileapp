package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
enum class UserRole {
    ADMIN,
    RESELLER,
    AGENCY,
    HOST,
    TEAM,
    USER;

    companion object {
        fun fromStorage(value: String?): UserRole {
            return fromApi(value) ?: USER
        }

        fun fromApi(value: String?): UserRole? {
            return when (value?.uppercase()) {
                "ADMIN", "MASTER" -> ADMIN
                "RESELLER" -> RESELLER
                "AGENCY", "AGENCY_OWNER" -> AGENCY
                "HOST" -> HOST
                "TEAM" -> TEAM
                "USER" -> USER
                else -> null
            }
        }
    }
}

@Serializable
data class UserResponse(
    val id: String,
    val username: String,
    val email: String,
    val role: UserRole,
    val phone: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null,
    val isGuest: Boolean = false,
    val agencyId: String? = null
)

@Serializable
data class RoleUpdateRequest(
    val role: String
)
