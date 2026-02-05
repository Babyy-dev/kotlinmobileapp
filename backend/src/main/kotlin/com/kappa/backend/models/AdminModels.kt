package com.kappa.backend.models

import kotlinx.serialization.Serializable

@Serializable
data class AdminGlobalConfig(
    val rtp: Double,
    val houseEdge: Double,
    val minRtp: Double,
    val maxRtp: Double
)

@Serializable
data class AdminGameConfig(
    val id: String,
    val gameName: String,
    val rtp: Double,
    val houseEdge: Double
)

@Serializable
data class AdminUserConfig(
    val id: String,
    val userId: String,
    val qualification: String,
    val rtp: Double,
    val houseEdge: Double
)

@Serializable
data class AdminQualificationConfig(
    val id: String,
    val qualification: String,
    val rtp: Double,
    val houseEdge: Double,
    val minPlayedUsd: Long,
    val durationDays: Int
)

@Serializable
data class AdminLockRule(
    val id: String,
    val name: String,
    val cooldownMinutes: Int,
    val minTurnover: Long,
    val maxLoss: Long,
    val periodMinutes: Int,
    val maxActionsPerPeriod: Int,
    val scope: String,
    val actions: List<String>
)

@Serializable
data class AdminAuditLog(
    val id: String,
    val actorId: String,
    val action: String,
    val message: String?,
    val createdAt: Long
)
