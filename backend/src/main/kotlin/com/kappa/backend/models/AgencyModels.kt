package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class AgencyApplicationRequest(
    val agencyName: String
)

@Serializable
data class AgencyApplicationResponse(
    val id: String,
    val userId: String,
    val agencyName: String,
    val status: String,
    val createdAt: Long,
    val reviewedAt: Long? = null
)

@Serializable
data class ResellerApplicationResponse(
    val id: String,
    val userId: String,
    val status: String,
    val createdAt: Long,
    val reviewedAt: Long? = null
)

@Serializable
data class TeamCreateRequest(
    val name: String
)

@Serializable
data class TeamResponse(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val agencyId: String? = null
)

@Serializable
data class AgencyResponse(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val commissionValueUsd: String,
    val commissionBlockDiamonds: Long,
    val status: String,
    val createdAt: Long
)

@Serializable
data class AgencyUpdateRequest(
    val commissionValueUsd: String? = null,
    val commissionBlockDiamonds: Long? = null,
    val status: String? = null
)
