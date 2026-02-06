package com.kappa.app.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.core.base.ViewState
import com.kappa.app.core.network.ApiService
import com.kappa.app.core.network.model.AdminGameConfigDto
import com.kappa.app.core.network.model.AdminGlobalConfigDto
import com.kappa.app.core.network.model.AdminLockRuleDto
import com.kappa.app.core.network.model.AdminQualificationConfigDto
import com.kappa.app.core.network.model.AdminUserConfigDto
import com.kappa.app.core.network.model.AdminAuditLogDto
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class AdminGameConfig(
    val id: String,
    val gameName: String,
    val rtp: Double,
    val houseEdge: Double
)

data class AdminUserConfig(
    val id: String,
    val userId: String,
    val qualification: String,
    val rtp: Double,
    val houseEdge: Double
)

data class QualificationConfig(
    val id: String,
    val qualification: String,
    val rtp: Double,
    val houseEdge: Double,
    val minPlayedUsd: Long = 0,
    val durationDays: Int = 0
)

data class AdminLockRule(
    val id: String,
    val name: String,
    val cooldownMinutes: Int,
    val minTurnover: Long,
    val maxLoss: Long,
    val periodMinutes: Int = 0,
    val maxActionsPerPeriod: Int = 0,
    val scope: String = "user",
    val actions: List<String> = listOf("withdrawals")
)

data class WithdrawalRules(
    val minAmountUsd: Double,
    val multipleUsd: Double
)

data class AdminAuditLog(
    val id: String,
    val actorId: String,
    val action: String,
    val message: String?,
    val createdAt: Long
)

data class AdminDashboardState(
    val globalRtp: Double = 70.0,
    val globalHouseEdge: Double = 30.0,
    val minRtp: Double = 60.0,
    val maxRtp: Double = 90.0,
    val gameConfigs: List<AdminGameConfig> = emptyList(),
    val userConfigs: List<AdminUserConfig> = emptyList(),
    val qualificationConfigs: List<QualificationConfig> = emptyList(),
    val withdrawalRules: WithdrawalRules = WithdrawalRules(10.0, 10.0),
    val lockRules: List<AdminLockRule> = emptyList(),
    val auditLogs: List<AdminAuditLog> = emptyList(),
    val message: String? = null
) : ViewState

@HiltViewModel
class AdminDashboardViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _viewState = MutableStateFlow(
        AdminDashboardState()
    )
    val viewState: StateFlow<AdminDashboardState> = _viewState.asStateFlow()

    fun updateGlobal(rtp: Double, houseEdge: Double) {
        val state = _viewState.value
        if (rtp < state.minRtp || rtp > state.maxRtp) {
            _viewState.value = state.copy(message = "RTP must be between ${state.minRtp} and ${state.maxRtp}")
            return
        }
        viewModelScope.launch {
            val response = runCatching {
                apiService.setAdminGlobalConfig(
                    AdminGlobalConfigDto(
                        rtp = rtp,
                        houseEdge = houseEdge,
                        minRtp = state.minRtp,
                        maxRtp = state.maxRtp
                    )
                )
            }.getOrNull()
            _viewState.value = _viewState.value.copy(
                globalRtp = rtp,
                globalHouseEdge = houseEdge,
                message = if (response?.success == true) "Global settings updated" else response?.error
            )
            loadAuditLogs()
        }
    }

    fun addGameConfig(config: AdminGameConfig) {
        if (!isRtpValid(config.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        viewModelScope.launch {
            apiService.upsertAdminGameConfig(config.toDto())
            _viewState.value = _viewState.value.copy(
                gameConfigs = _viewState.value.gameConfigs + config,
                message = "Game config added"
            )
            loadAuditLogs()
        }
    }

    fun updateGameConfig(updated: AdminGameConfig) {
        if (!isRtpValid(updated.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        viewModelScope.launch {
            apiService.upsertAdminGameConfig(updated.toDto())
            _viewState.value = _viewState.value.copy(
                gameConfigs = _viewState.value.gameConfigs.map { if (it.id == updated.id) updated else it },
                message = "Game config updated"
            )
            loadAuditLogs()
        }
    }

    fun deleteGameConfig(id: String) {
        viewModelScope.launch {
            apiService.deleteAdminGameConfig(id)
            _viewState.value = _viewState.value.copy(
                gameConfigs = _viewState.value.gameConfigs.filterNot { it.id == id },
                message = "Game config removed"
            )
            loadAuditLogs()
        }
    }

    fun addUserConfig(config: AdminUserConfig) {
        if (!isRtpValid(config.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        viewModelScope.launch {
            apiService.upsertAdminUserConfig(config.toDto())
            _viewState.value = _viewState.value.copy(
                userConfigs = _viewState.value.userConfigs + config,
                message = "User config added"
            )
            loadAuditLogs()
        }
    }

    fun updateUserConfig(updated: AdminUserConfig) {
        if (!isRtpValid(updated.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        viewModelScope.launch {
            apiService.upsertAdminUserConfig(updated.toDto())
            _viewState.value = _viewState.value.copy(
                userConfigs = _viewState.value.userConfigs.map { if (it.id == updated.id) updated else it },
                message = "User config updated"
            )
            loadAuditLogs()
        }
    }

    fun deleteUserConfig(id: String) {
        viewModelScope.launch {
            apiService.deleteAdminUserConfig(id)
            _viewState.value = _viewState.value.copy(
                userConfigs = _viewState.value.userConfigs.filterNot { it.id == id },
                message = "User config removed"
            )
            loadAuditLogs()
        }
    }

    fun addLockRule(rule: AdminLockRule) {
        viewModelScope.launch {
            apiService.upsertAdminLockRule(rule.toDto())
            _viewState.value = _viewState.value.copy(
                lockRules = _viewState.value.lockRules + rule,
                message = "Lock rule added"
            )
            loadAuditLogs()
        }
    }

    fun updateLockRule(updated: AdminLockRule) {
        viewModelScope.launch {
            apiService.upsertAdminLockRule(updated.toDto())
            _viewState.value = _viewState.value.copy(
                lockRules = _viewState.value.lockRules.map { if (it.id == updated.id) updated else it },
                message = "Lock rule updated"
            )
            loadAuditLogs()
        }
    }

    fun deleteLockRule(id: String) {
        viewModelScope.launch {
            apiService.deleteAdminLockRule(id)
            _viewState.value = _viewState.value.copy(
                lockRules = _viewState.value.lockRules.filterNot { it.id == id },
                message = "Lock rule removed"
            )
            loadAuditLogs()
        }
    }

    fun updateWithdrawalRules(minAmountUsd: Double, multipleUsd: Double) {
        _viewState.value = _viewState.value.copy(
            withdrawalRules = WithdrawalRules(minAmountUsd, multipleUsd),
            message = "Withdrawal rules updated"
        )
    }

    fun updateQualificationConfig(updated: QualificationConfig) {
        if (!isRtpValid(updated.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        viewModelScope.launch {
            apiService.upsertAdminQualificationConfig(updated.toDto())
            _viewState.value = _viewState.value.copy(
                qualificationConfigs = _viewState.value.qualificationConfigs.map {
                    if (it.id == updated.id) updated else it
                },
                message = "Qualification config updated"
            )
            loadAuditLogs()
        }
    }

    private fun isRtpValid(rtp: Double): Boolean {
        val state = _viewState.value
        return rtp in state.minRtp..state.maxRtp
    }

    fun clearMessage() {
        _viewState.value = _viewState.value.copy(message = null)
    }

    init {
        viewModelScope.launch {
            val global = runCatching { apiService.getAdminGlobalConfig() }.getOrNull()
            val games = runCatching { apiService.getAdminGameConfigs() }.getOrNull()
            val users = runCatching { apiService.getAdminUserConfigs() }.getOrNull()
            val quals = runCatching { apiService.getAdminQualificationConfigs() }.getOrNull()
            val locks = runCatching { apiService.getAdminLockRules() }.getOrNull()
            val audits = runCatching { apiService.getAdminAuditLogs() }.getOrNull()

            val state = _viewState.value
            _viewState.value = state.copy(
                globalRtp = global?.data?.rtp ?: state.globalRtp,
                globalHouseEdge = global?.data?.houseEdge ?: state.globalHouseEdge,
                minRtp = global?.data?.minRtp ?: state.minRtp,
                maxRtp = global?.data?.maxRtp ?: state.maxRtp,
                gameConfigs = games?.data?.map { it.toDomain() } ?: state.gameConfigs,
                userConfigs = users?.data?.map { it.toDomain() } ?: state.userConfigs,
                qualificationConfigs = quals?.data?.map { it.toDomain() } ?: state.qualificationConfigs,
                lockRules = locks?.data?.map { it.toDomain() } ?: state.lockRules,
                auditLogs = audits?.data?.map { it.toDomain() } ?: state.auditLogs
            )
        }
    }

    private fun AdminGameConfig.toDto() = AdminGameConfigDto(id, gameName, rtp, houseEdge)
    private fun AdminUserConfig.toDto() = AdminUserConfigDto(id, userId, qualification, rtp, houseEdge)
    private fun QualificationConfig.toDto() =
        AdminQualificationConfigDto(id, qualification, rtp, houseEdge, minPlayedUsd, durationDays)
    private fun AdminLockRule.toDto() =
        AdminLockRuleDto(id, name, cooldownMinutes, minTurnover, maxLoss, periodMinutes, maxActionsPerPeriod, scope, actions)

    private fun AdminGameConfigDto.toDomain() = AdminGameConfig(id, gameName, rtp, houseEdge)
    private fun AdminUserConfigDto.toDomain() = AdminUserConfig(id, userId, qualification, rtp, houseEdge)
    private fun AdminQualificationConfigDto.toDomain() =
        QualificationConfig(id, qualification, rtp, houseEdge, minPlayedUsd, durationDays)
    private fun AdminLockRuleDto.toDomain() =
        AdminLockRule(id, name, cooldownMinutes, minTurnover, maxLoss, periodMinutes, maxActionsPerPeriod, scope, actions)

    private fun AdminAuditLogDto.toDomain() =
        AdminAuditLog(id, actorId, action, message, createdAt)

    private fun loadAuditLogs() {
        viewModelScope.launch {
            val audits = runCatching { apiService.getAdminAuditLogs() }.getOrNull()
            val mapped = audits?.data?.map { it.toDomain() }
            if (mapped != null) {
                _viewState.value = _viewState.value.copy(auditLogs = mapped)
            }
        }
    }
}
