package com.kappa.app.domain.user

/**
 * User domain model.
 */
data class User(
    val id: String,
    val username: String,
    val email: String,
    val role: Role,
    val permissions: List<Permission> = emptyList()
)

/**
 * User roles.
 */
enum class Role {
    MASTER,
    RESELLER,
    AGENCY_OWNER,
    USER,
}

/**
 * UI-friendly role labels.
 */
fun Role.toDisplayName(): String {
    return when (this) {
        Role.MASTER -> "Admin"
        Role.RESELLER -> "Reseller"
        Role.AGENCY_OWNER -> "Agency"
        Role.USER -> "User"
    }
}

/**
 * Permission placeholder.
 */
enum class Permission {
    // Permissions will be defined in future phases
}
