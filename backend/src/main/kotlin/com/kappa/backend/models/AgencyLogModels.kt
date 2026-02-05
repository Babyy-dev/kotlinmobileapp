package com.kappa.backend.models

data class AgencyLogResponse(
    val id: String,
    val agencyId: String,
    val actorId: String,
    val action: String,
    val message: String? = null,
    val createdAt: Long
)
