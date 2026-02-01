package com.kappa.app.admin.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.core.base.ViewState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    val message: String? = null
) : ViewState

class AdminDashboardViewModel : ViewModel() {

    private val _viewState = MutableStateFlow(
        AdminDashboardState(
            gameConfigs = listOf(
                AdminGameConfig("bean_growth", "Bean Growth", 70.0, 30.0),
                AdminGameConfig("egg_smash", "Egg Smash", 70.0, 30.0),
                AdminGameConfig("slot_x", "Slot", 70.0, 30.0),
                AdminGameConfig("crash_x", "Crash", 70.0, 30.0),
                AdminGameConfig("roulette_x", "Roulette", 70.0, 30.0),
                AdminGameConfig("lucky77", "Lucky77", 70.0, 30.0),
                AdminGameConfig("tap_speed", "Tap Speed", 70.0, 30.0),
                AdminGameConfig("gift_rush", "Gift Rush", 70.0, 30.0),
                AdminGameConfig("battle_arena", "Battle Arena", 70.0, 30.0),
                AdminGameConfig("spin_wheel", "Spin Wheel", 70.0, 30.0)
            ),
            qualificationConfigs = listOf(
                QualificationConfig("q_normal", "normal", 70.0, 30.0),
                QualificationConfig("q_vip1", "VIP1", 70.0, 30.0, 500, 10),
                QualificationConfig("q_vip2", "VIP2", 70.0, 30.0, 1000, 10),
                QualificationConfig("q_vip3", "VIP3", 70.0, 30.0, 1500, 10),
                QualificationConfig("q_vip4", "VIP4", 70.0, 30.0, 2000, 10),
                QualificationConfig("q_vip5", "VIP5 stars", 70.0, 30.0, 3000, 30),
                QualificationConfig("q_agency", "agency", 70.0, 30.0),
                QualificationConfig("q_reseller", "reseller", 70.0, 30.0)
            ),
            userConfigs = listOf(
                AdminUserConfig("u1", "user_001", "VIP", 97.0, 3.0)
            ),
            lockRules = listOf(
                AdminLockRule("r1", "Withdrawal cooldown", 30, 0, 0, 0, 0, "user", listOf("withdrawals")),
                AdminLockRule("r2", "Min turnover", 0, 1000, 0, 0, 0, "user", listOf("withdrawals", "gifts")),
                AdminLockRule("r3", "Max loss", 0, 0, 500, 0, 0, "user", listOf("mini_games")),
                AdminLockRule("r4", "Daily withdrawal limit", 0, 0, 0, 1440, 3, "user", listOf("withdrawals")),
                AdminLockRule("r5", "Agency gift cooldown", 60, 0, 0, 0, 0, "agency", listOf("gifts")),
                AdminLockRule("r6", "Reseller coin lock", 0, 0, 0, 0, 0, "reseller", listOf("coins"))
            )
        )
    )
    val viewState: StateFlow<AdminDashboardState> = _viewState.asStateFlow()

    fun updateGlobal(rtp: Double, houseEdge: Double) {
        val state = _viewState.value
        if (rtp < state.minRtp || rtp > state.maxRtp) {
            _viewState.value = state.copy(message = "RTP must be between ${state.minRtp} and ${state.maxRtp}")
            return
        }
        _viewState.value = _viewState.value.copy(
            globalRtp = rtp,
            globalHouseEdge = houseEdge,
            message = "Global settings updated"
        )
    }

    fun addGameConfig(config: AdminGameConfig) {
        if (!isRtpValid(config.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        _viewState.value = _viewState.value.copy(
            gameConfigs = _viewState.value.gameConfigs + config,
            message = "Game config added"
        )
    }

    fun updateGameConfig(updated: AdminGameConfig) {
        if (!isRtpValid(updated.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        _viewState.value = _viewState.value.copy(
            gameConfigs = _viewState.value.gameConfigs.map { if (it.id == updated.id) updated else it },
            message = "Game config updated"
        )
    }

    fun deleteGameConfig(id: String) {
        _viewState.value = _viewState.value.copy(
            gameConfigs = _viewState.value.gameConfigs.filterNot { it.id == id },
            message = "Game config removed"
        )
    }

    fun addUserConfig(config: AdminUserConfig) {
        if (!isRtpValid(config.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        _viewState.value = _viewState.value.copy(
            userConfigs = _viewState.value.userConfigs + config,
            message = "User config added"
        )
    }

    fun updateUserConfig(updated: AdminUserConfig) {
        if (!isRtpValid(updated.rtp)) {
            _viewState.value = _viewState.value.copy(message = "RTP must be between ${_viewState.value.minRtp} and ${_viewState.value.maxRtp}")
            return
        }
        _viewState.value = _viewState.value.copy(
            userConfigs = _viewState.value.userConfigs.map { if (it.id == updated.id) updated else it },
            message = "User config updated"
        )
    }

    fun deleteUserConfig(id: String) {
        _viewState.value = _viewState.value.copy(
            userConfigs = _viewState.value.userConfigs.filterNot { it.id == id },
            message = "User config removed"
        )
    }

    fun addLockRule(rule: AdminLockRule) {
        _viewState.value = _viewState.value.copy(
            lockRules = _viewState.value.lockRules + rule,
            message = "Lock rule added"
        )
    }

    fun updateLockRule(updated: AdminLockRule) {
        _viewState.value = _viewState.value.copy(
            lockRules = _viewState.value.lockRules.map { if (it.id == updated.id) updated else it },
            message = "Lock rule updated"
        )
    }

    fun deleteLockRule(id: String) {
        _viewState.value = _viewState.value.copy(
            lockRules = _viewState.value.lockRules.filterNot { it.id == id },
            message = "Lock rule removed"
        )
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
        _viewState.value = _viewState.value.copy(
            qualificationConfigs = _viewState.value.qualificationConfigs.map {
                if (it.id == updated.id) updated else it
            },
            message = "Qualification config updated"
        )
    }

    private fun isRtpValid(rtp: Double): Boolean {
        val state = _viewState.value
        return rtp in state.minRtp..state.maxRtp
    }

    fun clearMessage() {
        _viewState.value = _viewState.value.copy(message = null)
    }
}
