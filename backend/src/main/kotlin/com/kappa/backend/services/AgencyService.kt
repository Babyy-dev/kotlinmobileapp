package com.kappa.backend.services

import com.kappa.backend.data.Agencies
import com.kappa.backend.data.AgencyAuditLogs
import com.kappa.backend.data.AgencyApplications
import com.kappa.backend.data.ResellerApplications
import com.kappa.backend.data.TeamGroups
import com.kappa.backend.data.TeamMembers
import com.kappa.backend.data.Users
import com.kappa.backend.models.AgencyLogResponse
import com.kappa.backend.models.AgencyApplicationResponse
import com.kappa.backend.models.AgencyResponse
import com.kappa.backend.models.AgencyUpdateRequest
import com.kappa.backend.models.ResellerApplicationResponse
import com.kappa.backend.models.TeamResponse
import com.kappa.backend.models.UserRole
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.SqlExpressionBuilder.inList
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.update
import org.jetbrains.exposed.sql.transactions.transaction
import java.util.UUID

class AgencyService {
    fun applyForAgency(userId: UUID, agencyName: String): AgencyApplicationResponse {
        require(agencyName.isNotBlank()) { "Agency name is required" }
        return transaction {
            val existing = AgencyApplications.select {
                (AgencyApplications.userId eq userId) and (AgencyApplications.status eq "PENDING")
            }.singleOrNull()
            if (existing != null) {
                return@transaction AgencyApplicationResponse(
                    id = existing[AgencyApplications.id].toString(),
                    userId = existing[AgencyApplications.userId].toString(),
                    agencyName = existing[AgencyApplications.agencyName],
                    status = existing[AgencyApplications.status],
                    createdAt = existing[AgencyApplications.createdAt],
                    reviewedAt = existing[AgencyApplications.reviewedAt]
                )
            }
            val now = System.currentTimeMillis()
            val appId = UUID.randomUUID()
            AgencyApplications.insert {
                it[id] = appId
                it[AgencyApplications.userId] = userId
                it[AgencyApplications.agencyName] = agencyName.trim()
                it[status] = "PENDING"
                it[createdAt] = now
                it[reviewedAt] = null
            }
            logAction(null, userId, "AGENCY_APPLY", "Agency application ${appId}")
            AgencyApplicationResponse(
                id = appId.toString(),
                userId = userId.toString(),
                agencyName = agencyName.trim(),
                status = "PENDING",
                createdAt = now
            )
        }
    }

    fun applyForReseller(userId: UUID): ResellerApplicationResponse {
        return transaction {
            val existing = ResellerApplications.select {
                (ResellerApplications.userId eq userId) and (ResellerApplications.status eq "PENDING")
            }.singleOrNull()
            if (existing != null) {
                return@transaction ResellerApplicationResponse(
                    id = existing[ResellerApplications.id].toString(),
                    userId = existing[ResellerApplications.userId].toString(),
                    status = existing[ResellerApplications.status],
                    createdAt = existing[ResellerApplications.createdAt],
                    reviewedAt = existing[ResellerApplications.reviewedAt]
                )
            }
            val now = System.currentTimeMillis()
            val appId = UUID.randomUUID()
            ResellerApplications.insert {
                it[id] = appId
                it[ResellerApplications.userId] = userId
                it[status] = "PENDING"
                it[createdAt] = now
                it[reviewedAt] = null
            }
            logAction(null, userId, "RESELLER_APPLY", "Reseller application ${appId}")
            ResellerApplicationResponse(
                id = appId.toString(),
                userId = userId.toString(),
                status = "PENDING",
                createdAt = now
            )
        }
    }

    fun listAgencyApplications(): List<AgencyApplicationResponse> {
        return transaction {
            AgencyApplications.selectAll()
                .orderBy(AgencyApplications.createdAt, SortOrder.DESC)
                .map { row ->
                    AgencyApplicationResponse(
                        id = row[AgencyApplications.id].toString(),
                        userId = row[AgencyApplications.userId].toString(),
                        agencyName = row[AgencyApplications.agencyName],
                        status = row[AgencyApplications.status],
                        createdAt = row[AgencyApplications.createdAt],
                        reviewedAt = row[AgencyApplications.reviewedAt]
                    )
                }
        }
    }

    fun listResellerApplications(): List<ResellerApplicationResponse> {
        return transaction {
            ResellerApplications.selectAll()
                .orderBy(ResellerApplications.createdAt, SortOrder.DESC)
                .map { row ->
                    ResellerApplicationResponse(
                        id = row[ResellerApplications.id].toString(),
                        userId = row[ResellerApplications.userId].toString(),
                        status = row[ResellerApplications.status],
                        createdAt = row[ResellerApplications.createdAt],
                        reviewedAt = row[ResellerApplications.reviewedAt]
                    )
                }
        }
    }

    fun reviewAgencyApplication(appId: UUID, approved: Boolean): AgencyApplicationResponse? {
        return transaction {
            val row = AgencyApplications.select { AgencyApplications.id eq appId }.singleOrNull()
                ?: return@transaction null
            if (row[AgencyApplications.status] != "PENDING") {
                return@transaction null
            }
            val now = System.currentTimeMillis()
            val newStatus = if (approved) "APPROVED" else "REJECTED"
            AgencyApplications.update({ AgencyApplications.id eq appId }) {
                it[status] = newStatus
                it[reviewedAt] = now
            }
            if (approved) {
                val agencyId = UUID.randomUUID()
                Agencies.insert {
                    it[id] = agencyId
                    it[name] = row[AgencyApplications.agencyName]
                    it[ownerUserId] = row[AgencyApplications.userId]
                    it[commissionValueUsd] = 2.20.toBigDecimal()
                    it[commissionBlockDiamonds] = 620000
                    it[status] = "active"
                    it[createdAt] = now
                }
                Users.update({ Users.id eq row[AgencyApplications.userId] }) {
                    it[Users.role] = UserRole.AGENCY.name
                    it[Users.agencyId] = agencyId
                }
                logAction(agencyId, row[AgencyApplications.userId], "AGENCY_APPROVED", "Agency ${agencyId}")
            } else {
                logAction(null, row[AgencyApplications.userId], "AGENCY_REJECTED", "Agency application ${appId}")
            }
            AgencyApplicationResponse(
                id = row[AgencyApplications.id].toString(),
                userId = row[AgencyApplications.userId].toString(),
                agencyName = row[AgencyApplications.agencyName],
                status = newStatus,
                createdAt = row[AgencyApplications.createdAt],
                reviewedAt = now
            )
        }
    }

    fun reviewResellerApplication(appId: UUID, approved: Boolean): ResellerApplicationResponse? {
        return transaction {
            val row = ResellerApplications.select { ResellerApplications.id eq appId }.singleOrNull()
                ?: return@transaction null
            if (row[ResellerApplications.status] != "PENDING") {
                return@transaction null
            }
            val now = System.currentTimeMillis()
            val newStatus = if (approved) "APPROVED" else "REJECTED"
            ResellerApplications.update({ ResellerApplications.id eq appId }) {
                it[status] = newStatus
                it[reviewedAt] = now
            }
            if (approved) {
                Users.update({ Users.id eq row[ResellerApplications.userId] }) {
                    it[Users.role] = UserRole.RESELLER.name
                }
                logAction(null, row[ResellerApplications.userId], "RESELLER_APPROVED", "Reseller ${row[ResellerApplications.userId]}")
            } else {
                logAction(null, row[ResellerApplications.userId], "RESELLER_REJECTED", "Reseller application ${appId}")
            }
            ResellerApplicationResponse(
                id = row[ResellerApplications.id].toString(),
                userId = row[ResellerApplications.userId].toString(),
                status = newStatus,
                createdAt = row[ResellerApplications.createdAt],
                reviewedAt = now
            )
        }
    }

    fun listAgencyApplicationsForUser(userId: UUID): List<AgencyApplicationResponse> {
        return transaction {
            AgencyApplications.select { AgencyApplications.userId eq userId }
                .orderBy(AgencyApplications.createdAt, SortOrder.DESC)
                .map { row ->
                    AgencyApplicationResponse(
                        id = row[AgencyApplications.id].toString(),
                        userId = row[AgencyApplications.userId].toString(),
                        agencyName = row[AgencyApplications.agencyName],
                        status = row[AgencyApplications.status],
                        createdAt = row[AgencyApplications.createdAt],
                        reviewedAt = row[AgencyApplications.reviewedAt]
                    )
                }
        }
    }

    fun listResellerApplicationsForUser(userId: UUID): List<ResellerApplicationResponse> {
        return transaction {
            ResellerApplications.select { ResellerApplications.userId eq userId }
                .orderBy(ResellerApplications.createdAt, SortOrder.DESC)
                .map { row ->
                    ResellerApplicationResponse(
                        id = row[ResellerApplications.id].toString(),
                        userId = row[ResellerApplications.userId].toString(),
                        status = row[ResellerApplications.status],
                        createdAt = row[ResellerApplications.createdAt],
                        reviewedAt = row[ResellerApplications.reviewedAt]
                    )
                }
        }
    }

    fun createTeam(userId: UUID, name: String): TeamResponse {
        require(name.isNotBlank()) { "Team name is required" }
        return transaction {
            val now = System.currentTimeMillis()
            val user = Users.select { Users.id eq userId }.singleOrNull()
                ?: throw IllegalArgumentException("User not found")
            val teamId = UUID.randomUUID()
            TeamGroups.insert {
                it[id] = teamId
                it[TeamGroups.name] = name.trim()
                it[TeamGroups.ownerUserId] = userId
                it[TeamGroups.agencyId] = user[Users.agencyId]
                it[createdAt] = now
            }
            TeamMembers.insert {
                it[TeamMembers.teamId] = teamId
                it[TeamMembers.userId] = userId
                it[TeamMembers.role] = "OWNER"
                it[TeamMembers.joinedAt] = now
            }
            logAction(user[Users.agencyId], userId, "TEAM_CREATE", "Team ${teamId}")
            TeamResponse(
                id = teamId.toString(),
                name = name.trim(),
                ownerUserId = userId.toString(),
                agencyId = user[Users.agencyId]?.toString()
            )
        }
    }

    fun joinTeam(userId: UUID, teamId: UUID): Boolean {
        return transaction {
            val exists = TeamGroups.select { TeamGroups.id eq teamId }.any()
            if (!exists) {
                return@transaction false
            }
            val already = TeamMembers.select {
                (TeamMembers.teamId eq teamId) and (TeamMembers.userId eq userId)
            }.any()
            if (already) {
                return@transaction true
            }
            TeamMembers.insert {
                it[TeamMembers.teamId] = teamId
                it[TeamMembers.userId] = userId
                it[TeamMembers.role] = "MEMBER"
                it[TeamMembers.joinedAt] = System.currentTimeMillis()
            }
            val agencyId = TeamGroups.select { TeamGroups.id eq teamId }
                .singleOrNull()
                ?.get(TeamGroups.agencyId)
            logAction(agencyId, userId, "TEAM_JOIN", "Team ${teamId}")
            true
        }
    }

    fun leaveTeam(userId: UUID, teamId: UUID): Boolean {
        return transaction {
            val deleted = TeamMembers.deleteWhere { (TeamMembers.teamId eq teamId) and (TeamMembers.userId eq userId) } > 0
            if (deleted) {
                val agencyId = TeamGroups.select { TeamGroups.id eq teamId }
                    .singleOrNull()
                    ?.get(TeamGroups.agencyId)
                logAction(agencyId, userId, "TEAM_LEAVE", "Team ${teamId}")
            }
            deleted
        }
    }

    fun listTeams(userId: UUID): List<TeamResponse> {
        return transaction {
            val teamIds = TeamMembers.select { TeamMembers.userId eq userId }
                .map { it[TeamMembers.teamId] }
            if (teamIds.isEmpty()) {
                return@transaction emptyList()
            }
            TeamGroups.select { TeamGroups.id inList teamIds }
                .map { row ->
                    TeamResponse(
                        id = row[TeamGroups.id].toString(),
                        name = row[TeamGroups.name],
                        ownerUserId = row[TeamGroups.ownerUserId].toString(),
                        agencyId = row[TeamGroups.agencyId]?.toString()
                    )
                }
        }
    }

    fun listAgencies(status: String?, limit: Int): List<AgencyResponse> {
        val resolvedLimit = limit.coerceIn(1, 200)
        val normalizedStatus = status?.let { normalizeAgencyStatus(it) }
        return transaction {
            val query = if (normalizedStatus == null) {
                Agencies.selectAll()
            } else {
                Agencies.select { Agencies.status eq normalizedStatus }
            }
            query.orderBy(Agencies.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row -> row.toAgencyResponse() }
        }
    }

    fun listLogs(agencyId: UUID, limit: Int): List<AgencyLogResponse> {
        val resolvedLimit = limit.coerceIn(1, 200)
        return transaction {
            AgencyAuditLogs.select { AgencyAuditLogs.agencyId eq agencyId }
                .orderBy(AgencyAuditLogs.createdAt, SortOrder.DESC)
                .limit(resolvedLimit)
                .map { row ->
                    AgencyLogResponse(
                        id = row[AgencyAuditLogs.id].toString(),
                        agencyId = row[AgencyAuditLogs.agencyId].toString(),
                        actorId = row[AgencyAuditLogs.actorId].toString(),
                        action = row[AgencyAuditLogs.action],
                        message = row[AgencyAuditLogs.message],
                        createdAt = row[AgencyAuditLogs.createdAt]
                    )
                }
        }
    }

    fun updateAgency(agencyId: UUID, request: AgencyUpdateRequest): AgencyResponse? {
        val commissionValue = request.commissionValueUsd?.let { value ->
            value.toBigDecimalOrNull() ?: throw IllegalArgumentException("Invalid commission value")
        }
        val commissionBlock = request.commissionBlockDiamonds?.let { value ->
            if (value <= 0) {
                throw IllegalArgumentException("Commission block must be greater than 0")
            }
            value
        }
        val status = request.status?.let { normalizeAgencyStatus(it) }

        return transaction {
            val row = Agencies.select { Agencies.id eq agencyId }.singleOrNull()
                ?: return@transaction null
            if (commissionValue == null && commissionBlock == null && status == null) {
                return@transaction row.toAgencyResponse()
            }
            Agencies.update({ Agencies.id eq agencyId }) {
                if (commissionValue != null) {
                    it[commissionValueUsd] = commissionValue
                }
                if (commissionBlock != null) {
                    it[commissionBlockDiamonds] = commissionBlock
                }
                if (status != null) {
                    it[Agencies.status] = status
                }
            }
            Agencies.select { Agencies.id eq agencyId }.single().toAgencyResponse()
        }
    }

    private fun normalizeAgencyStatus(value: String): String {
        val normalized = value.trim().lowercase()
        return if (normalized in setOf("active", "inactive", "suspended")) {
            normalized
        } else {
            throw IllegalArgumentException("Invalid status")
        }
    }

    private fun org.jetbrains.exposed.sql.ResultRow.toAgencyResponse(): AgencyResponse {
        return AgencyResponse(
            id = this[Agencies.id].toString(),
            name = this[Agencies.name],
            ownerUserId = this[Agencies.ownerUserId].toString(),
            commissionValueUsd = this[Agencies.commissionValueUsd].toPlainString(),
            commissionBlockDiamonds = this[Agencies.commissionBlockDiamonds],
            status = this[Agencies.status],
            createdAt = this[Agencies.createdAt]
        )
    }

    private fun logAction(agencyId: UUID?, actorId: UUID, action: String, message: String?) {
        val resolvedAgency = agencyId ?: Users.select { Users.id eq actorId }
            .singleOrNull()
            ?.get(Users.agencyId)
        val now = System.currentTimeMillis()
        if (resolvedAgency == null) {
            return
        }
        AgencyAuditLogs.insert {
            it[id] = UUID.randomUUID()
            it[AgencyAuditLogs.agencyId] = resolvedAgency
            it[AgencyAuditLogs.actorId] = actorId
            it[AgencyAuditLogs.action] = action
            it[AgencyAuditLogs.message] = message
            it[AgencyAuditLogs.createdAt] = now
        }
    }
}
