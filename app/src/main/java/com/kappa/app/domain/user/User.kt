package com.kappa.app.domain.user

/**
 * User domain model.
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: Role,
    val phone: String? = null,
    val nickname: String? = null,
    val avatarUrl: String? = null,
    val country: String? = null,
    val language: String? = null,
    val isGuest: Boolean = false,
    val permissions: List<Permission> = emptyList()
)

/**
 * User roles.
 */
enum class Role {
    ADMIN,
    RESELLER,
    AGENCY,
    HOST,
    TEAM,
    USER,
    ;

    companion object {
        fun fromApi(value: String): Role {
            return when (value.uppercase()) {
                "ADMIN", "MASTER" -> ADMIN
                "RESELLER" -> RESELLER
                "AGENCY", "AGENCY_OWNER" -> AGENCY
                "HOST" -> HOST
                "TEAM" -> TEAM
                "USER" -> USER
                else -> USER
            }
        }
    }
}

/**
 * UI-friendly role labels.
 */
fun Role.toDisplayName(): String {
    return when (this) {
        Role.ADMIN -> "Admin"
        Role.RESELLER -> "Reseller"
        Role.AGENCY -> "Agency"
        Role.HOST -> "Host"
        Role.TEAM -> "Team"
        Role.USER -> "User"
    }
}

/**
 * Permission placeholder.
 */
enum class Permission {
    // Permissions will be defined in future phases
}
