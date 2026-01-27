package com.kappa.app.agency.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.agency.domain.model.AgencyApplication
import com.kappa.app.agency.domain.model.AgencyCommission
import com.kappa.app.agency.domain.model.ResellerApplication
import com.kappa.app.agency.domain.model.Team
import com.kappa.app.agency.domain.repository.AgencyRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AgencyViewState(
    val agencyApplication: AgencyApplication? = null,
    val resellerApplication: ResellerApplication? = null,
    val teams: List<Team> = emptyList(),
    val commissions: List<AgencyCommission> = emptyList(),
    val isRefreshing: Boolean = false,
    val error: String? = null,
    val actionMessage: String? = null
) 

@HiltViewModel
class AgencyViewModel @Inject constructor(
    private val repository: AgencyRepository
) : ViewModel() {

    private val _viewState = MutableStateFlow(AgencyViewState())
    val viewState: StateFlow<AgencyViewState> = _viewState.asStateFlow()

    init {
        refreshAll()
    }

    fun refreshAll() {
        viewModelScope.launch {
            _viewState.update { it.copy(isRefreshing = true, error = null, actionMessage = null) }

            val agencyApps = repository.getAgencyApplications().getOrElse {
                emitError(it.message)
                emptyList()
            }
            val resellerApps = repository.getResellerApplications().getOrElse {
                emitError(it.message)
                emptyList()
            }
            val teams = repository.listTeams().getOrElse {
                emitError(it.message)
                emptyList()
            }
            val commissions = repository.listMyCommissions(20).getOrElse {
                emitError(it.message)
                emptyList()
            }

            _viewState.update {
                it.copy(
                    agencyApplication = agencyApps.firstOrNull(),
                    resellerApplication = resellerApps.firstOrNull(),
                    teams = teams,
                    commissions = commissions,
                    isRefreshing = false
                )
            }
        }
    }

    fun applyForAgency(name: String) {
        viewModelScope.launch {
            _viewState.update { it.copy(isRefreshing = true, actionMessage = null, error = null) }
            repository.applyForAgency(name)
                .onSuccess { application ->
                    _viewState.update {
                        it.copy(
                            agencyApplication = application,
                            actionMessage = "Agency application submitted"
                        )
                    }
                }
                .onFailure { emitError(it.message) }
            refreshAll()
        }
    }

    fun applyForReseller() {
        viewModelScope.launch {
            _viewState.update { it.copy(isRefreshing = true, actionMessage = null, error = null) }
            repository.applyForReseller()
                .onSuccess { response ->
                    _viewState.update {
                        it.copy(
                            resellerApplication = response,
                            actionMessage = "Reseller application submitted"
                        )
                    }
                }
                .onFailure { emitError(it.message) }
            refreshAll()
        }
    }

    fun createTeam(name: String) {
        if (name.isBlank()) {
            emitError("Team name required")
            return
        }
        viewModelScope.launch {
            _viewState.update { it.copy(isRefreshing = true, actionMessage = null, error = null) }
            repository.createTeam(name)
                .onSuccess { team ->
                    _viewState.update {
                        it.copy(
                            teams = it.teams + team,
                            actionMessage = "Team \"${team.name}\" created"
                        )
                    }
                }
                .onFailure { emitError(it.message) }
            refreshAll()
        }
    }

    fun joinTeam(teamId: String) {
        viewModelScope.launch {
            _viewState.update { it.copy(isRefreshing = true, actionMessage = null, error = null) }
            repository.joinTeam(teamId)
                .onSuccess {
                    _viewState.update { it.copy(actionMessage = "Joined team") }
                }
                .onFailure { emitError(it.message) }
            refreshAll()
        }
    }

    fun leaveTeam(teamId: String) {
        viewModelScope.launch {
            _viewState.update { it.copy(isRefreshing = true, actionMessage = null, error = null) }
            repository.leaveTeam(teamId)
                .onSuccess {
                    _viewState.update { it.copy(actionMessage = "Left team") }
                }
                .onFailure { emitError(it.message) }
            refreshAll()
        }
    }

    private fun emitError(message: String?) {
        if (message.isNullOrBlank()) return
        _viewState.update { it.copy(error = message, isRefreshing = false) }
    }
}
