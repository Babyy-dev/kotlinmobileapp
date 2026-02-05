package com.kappa.app.agency.domain.repository

import com.kappa.app.agency.domain.model.AgencyApplication
import com.kappa.app.agency.domain.model.AgencyCommission
import com.kappa.app.agency.domain.model.ResellerApplication
import com.kappa.app.agency.domain.model.Team
import kotlin.Result

interface AgencyRepository {
    suspend fun getAgencyApplications(): Result<List<AgencyApplication>>
    suspend fun getResellerApplications(): Result<List<ResellerApplication>>
    suspend fun applyForAgency(name: String): Result<AgencyApplication>
    suspend fun applyForReseller(): Result<ResellerApplication>
    suspend fun listTeams(): Result<List<Team>>
    suspend fun createTeam(name: String): Result<Team>
    suspend fun joinTeam(teamId: String): Result<Unit>
    suspend fun leaveTeam(teamId: String): Result<Unit>
    suspend fun listMyCommissions(limit: Int = 20): Result<List<AgencyCommission>>
    suspend fun approveAgencyApplication(id: String): Result<Unit>
    suspend fun rejectAgencyApplication(id: String): Result<Unit>
    suspend fun listAgencyRooms(): Result<List<Pair<String, String>>>
    suspend fun listAgencyHosts(): Result<List<Pair<String, String>>>
}
