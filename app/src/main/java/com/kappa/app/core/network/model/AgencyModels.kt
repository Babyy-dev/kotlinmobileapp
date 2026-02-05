package com.kappa.app.core.network.model

/**
 * DTOs for agency/reseller/team APIs.
 */
data class AgencyApplicationRequestDto(
    val agencyName: String
)

data class AgencyApplicationDto(
    val id: String,
    val userId: String,
    val agencyName: String,
    val status: String,
    val createdAt: Long,
    val reviewedAt: Long? = null
)

data class ResellerApplicationDto(
    val id: String,
    val userId: String,
    val status: String,
    val createdAt: Long,
    val reviewedAt: Long? = null
)

data class TeamCreateRequestDto(
    val name: String
)

data class TeamDto(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val agencyId: String? = null
)

data class AgencyCommissionDto(
    val id: String,
    val agencyId: String,
    val userId: String,
    val diamondsAmount: Long,
    val commissionUsd: String,
    val createdAt: Long
)

data class AgencyRoomDto(
    val id: String,
    val name: String,
    val status: String
)

data class AgencyHostDto(
    val id: String,
    val name: String,
    val diamonds: Long
)
