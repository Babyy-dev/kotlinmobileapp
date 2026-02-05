package com.kappa.app.core.network.model

data class AdminGlobalConfigDto(
    val rtp: Double,
    val houseEdge: Double,
    val minRtp: Double,
    val maxRtp: Double
)

data class AdminGameConfigDto(
    val id: String,
    val gameName: String,
    val rtp: Double,
    val houseEdge: Double
)

data class AdminUserConfigDto(
    val id: String,
    val userId: String,
    val qualification: String,
    val rtp: Double,
    val houseEdge: Double
)

data class AdminQualificationConfigDto(
    val id: String,
    val qualification: String,
    val rtp: Double,
    val houseEdge: Double,
    val minPlayedUsd: Long,
    val durationDays: Int
)

data class AdminLockRuleDto(
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

data class AdminAuditLogDto(
    val id: String,
    val actorId: String,
    val action: String,
    val message: String?,
    val createdAt: Long
)
