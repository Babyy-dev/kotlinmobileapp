package com.kappa.app.agency.domain.model

data class AgencyApplication(
    val id: String,
    val agencyName: String,
    val status: String,
    val createdAt: Long,
    val reviewedAt: Long?
)

data class ResellerApplication(
    val id: String,
    val status: String,
    val createdAt: Long,
    val reviewedAt: Long?
)

data class Team(
    val id: String,
    val name: String,
    val ownerUserId: String,
    val agencyId: String?
)

data class AgencyCommission(
    val id: String,
    val diamondsAmount: Long,
    val commissionUsd: String,
    val createdAt: Long
)
