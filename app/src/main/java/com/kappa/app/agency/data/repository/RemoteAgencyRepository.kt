package com.kappa.app.agency.data.repository

import com.kappa.app.agency.domain.model.AgencyApplication
import com.kappa.app.agency.domain.model.AgencyCommission
import com.kappa.app.agency.domain.model.ResellerApplication
import com.kappa.app.agency.domain.model.Team
import com.kappa.app.agency.domain.repository.AgencyRepository
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.BaseApiResponse
import com.kappa.app.core.network.ErrorMapper
import com.kappa.app.core.network.model.AgencyApplicationDto
import com.kappa.app.core.network.model.AgencyApplicationRequestDto
import com.kappa.app.core.network.model.AgencyCommissionDto
import com.kappa.app.core.network.model.ResellerApplicationDto
import com.kappa.app.core.network.model.TeamDto
import com.kappa.app.core.network.model.TeamCreateRequestDto
import com.kappa.app.core.network.model.toDomain
import javax.inject.Inject

class RemoteAgencyRepository @Inject constructor(
    private val apiService: ApiService,
    private val errorMapper: ErrorMapper
) : AgencyRepository {

    override suspend fun getAgencyApplications(): Result<List<AgencyApplication>> {
        return safeCall<List<AgencyApplicationDto>, List<AgencyApplication>>(
            call = { apiService.getAgencyApplications() }
        ) { list ->
            list.map(AgencyApplicationDto::toDomain)
        }
    }

    override suspend fun applyForAgency(name: String): Result<AgencyApplication> {
        return safeCall<AgencyApplicationDto, AgencyApplication>(
            call = { apiService.applyForAgency(AgencyApplicationRequestDto(name)) }
        ) { it.toDomain() }
    }

    override suspend fun applyForReseller(): Result<ResellerApplication> {
        return safeCall<ResellerApplicationDto, ResellerApplication>(
            call = { apiService.applyForReseller() }
        ) { it.toDomain() }
    }

    override suspend fun getResellerApplications(): Result<List<ResellerApplication>> {
        return safeCall<List<ResellerApplicationDto>, List<ResellerApplication>>(
            call = { apiService.getMyResellerApplications() }
        ) { list -> list.map(ResellerApplicationDto::toDomain) }
    }

    override suspend fun listTeams(): Result<List<Team>> {
        return safeCall<List<TeamDto>, List<Team>>(
            call = { apiService.listTeams() }
        ) { list -> list.map(TeamDto::toDomain) }
    }

    override suspend fun createTeam(name: String): Result<Team> {
        return safeCall<TeamDto, Team>(
            call = { apiService.createTeam(TeamCreateRequestDto(name)) }
        ) { it.toDomain() }
    }

    override suspend fun joinTeam(teamId: String): Result<Unit> {
        return safeCall(
            call = { apiService.joinTeam(teamId) }
        ) { Unit }
    }

    override suspend fun leaveTeam(teamId: String): Result<Unit> {
        return safeCall(
            call = { apiService.leaveTeam(teamId) }
        ) { Unit }
    }

    override suspend fun listMyCommissions(limit: Int): Result<List<AgencyCommission>> {
        return safeCall<List<AgencyCommissionDto>, List<AgencyCommission>>(
            call = { apiService.getMyCommissions(limit) }
        ) { list -> list.map(AgencyCommissionDto::toDomain) }
    }

    private suspend fun <T, R> safeCall(
        call: suspend () -> BaseApiResponse<T>,
        transform: (T) -> R
    ): Result<R> {
        return try {
            val response = call()
            val data = response.data
            if (!response.success || data == null) {
                Result.failure(Exception(response.error ?: "Failed to load data"))
            } else {
                Result.success(transform(data))
            }
        } catch (throwable: Throwable) {
            val message = errorMapper.mapToUserMessage(errorMapper.mapToNetworkError(throwable))
            Result.failure(Exception(message))
        }
    }
}
