package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import com.kappa.app.admin.presentation.AdminDashboardViewModel
import com.kappa.app.admin.presentation.AdminGameConfig
import com.kappa.app.admin.presentation.AdminLockRule
import com.kappa.app.admin.presentation.AdminUserConfig
import com.kappa.app.admin.presentation.AdminGameConfigAdapter
import com.kappa.app.admin.presentation.AdminUserConfigAdapter
import com.kappa.app.admin.presentation.AdminLockAdapter
import com.kappa.app.admin.presentation.QualificationConfigAdapter
import com.kappa.app.admin.presentation.AdminAuditLogAdapter
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminDashboardFragment : Fragment() {

    private val viewModel: AdminDashboardViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_admin_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val rtpInput = view.findViewById<TextInputEditText>(R.id.input_admin_rtp)
        val houseInput = view.findViewById<TextInputEditText>(R.id.input_admin_house_edge)
        val saveGlobalButton = view.findViewById<MaterialButton>(R.id.button_admin_save_global)
        val messageText = view.findViewById<android.widget.TextView>(R.id.text_admin_message)
        val gamesRecycler = view.findViewById<RecyclerView>(R.id.recycler_admin_games)
        val usersRecycler = view.findViewById<RecyclerView>(R.id.recycler_admin_users)
        val locksRecycler = view.findViewById<RecyclerView>(R.id.recycler_admin_locks)
        val qualRecycler = view.findViewById<RecyclerView>(R.id.recycler_admin_qualifications)
        val auditRecycler = view.findViewById<RecyclerView>(R.id.recycler_admin_audit_logs)
        val withdrawMinInput = view.findViewById<TextInputEditText>(R.id.input_admin_withdraw_min)
        val withdrawMultipleInput = view.findViewById<TextInputEditText>(R.id.input_admin_withdraw_multiple)
        val saveWithdrawButton = view.findViewById<MaterialButton>(R.id.button_admin_save_withdrawal)
        val addGameButton = view.findViewById<MaterialButton>(R.id.button_admin_add_game)
        val addUserButton = view.findViewById<MaterialButton>(R.id.button_admin_add_user)
        val addLockButton = view.findViewById<MaterialButton>(R.id.button_admin_add_lock)

        val gameAdapter = AdminGameConfigAdapter(
            onEdit = { config -> showGameConfigDialog(config) },
            onDelete = { config -> viewModel.deleteGameConfig(config.id) }
        )
        val userAdapter = AdminUserConfigAdapter(
            onEdit = { config -> showUserConfigDialog(config) },
            onDelete = { config -> viewModel.deleteUserConfig(config.id) }
        )
        val lockAdapter = AdminLockAdapter(
            onEdit = { rule -> showLockRuleDialog(rule) },
            onDelete = { rule -> viewModel.deleteLockRule(rule.id) }
        )
        val qualificationAdapter = QualificationConfigAdapter(
            onEdit = { config ->
                viewModel.updateQualificationConfig(config.copy(rtp = config.rtp + 0.5))
            }
        )
        val auditAdapter = AdminAuditLogAdapter()

        gamesRecycler.adapter = gameAdapter
        usersRecycler.adapter = userAdapter
        locksRecycler.adapter = lockAdapter
        qualRecycler.adapter = qualificationAdapter
        auditRecycler.adapter = auditAdapter

        saveGlobalButton.setOnClickListener {
            val rtp = rtpInput.text?.toString()?.trim()?.toDoubleOrNull()
            val house = houseInput.text?.toString()?.trim()?.toDoubleOrNull()
            if (rtp != null && house != null) {
                viewModel.updateGlobal(rtp, house)
            }
        }

        saveWithdrawButton.setOnClickListener {
            val min = withdrawMinInput.text?.toString()?.trim()?.toDoubleOrNull()
            val multiple = withdrawMultipleInput.text?.toString()?.trim()?.toDoubleOrNull()
            if (min != null && multiple != null) {
                viewModel.updateWithdrawalRules(min, multiple)
            }
        }

        addGameButton.setOnClickListener { showGameConfigDialog(null) }
        addUserButton.setOnClickListener { showUserConfigDialog(null) }
        addLockButton.setOnClickListener { showLockRuleDialog(null) }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    if (rtpInput.text.isNullOrBlank()) {
                        rtpInput.setText(state.globalRtp.toString())
                    }
                    if (houseInput.text.isNullOrBlank()) {
                        houseInput.setText(state.globalHouseEdge.toString())
                    }
                    if (withdrawMinInput.text.isNullOrBlank()) {
                        withdrawMinInput.setText(state.withdrawalRules.minAmountUsd.toString())
                    }
                    if (withdrawMultipleInput.text.isNullOrBlank()) {
                        withdrawMultipleInput.setText(state.withdrawalRules.multipleUsd.toString())
                    }
                    gameAdapter.submitItems(state.gameConfigs)
                    userAdapter.submitItems(state.userConfigs)
                    lockAdapter.submitLocks(state.lockRules)
                    qualificationAdapter.submitItems(state.qualificationConfigs)
                    auditAdapter.submitLogs(state.auditLogs)
                    if (state.message != null) {
                        messageText.text = state.message
                        messageText.visibility = View.VISIBLE
                        viewModel.clearMessage()
                    } else {
                        messageText.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun showGameConfigDialog(existing: AdminGameConfig?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_admin_game_config, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_game_name)
        val rtpInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_game_rtp)
        val houseInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_game_house)
        val isEdit = existing != null
        if (existing != null) {
            nameInput.setText(existing.gameName)
            rtpInput.setText(existing.rtp.toString())
            houseInput.setText(existing.houseEdge.toString())
        } else {
            val defaults = viewModel.viewState.value
            rtpInput.setText(defaults.globalRtp.toString())
            houseInput.setText(defaults.globalHouseEdge.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) "Edit Game Config" else "Add Game Config")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text?.toString()?.trim().orEmpty()
                val rtp = rtpInput.text?.toString()?.trim()?.toDoubleOrNull()
                val house = houseInput.text?.toString()?.trim()?.toDoubleOrNull()
                if (name.isBlank() || rtp == null || house == null) {
                    return@setPositiveButton
                }
                val config = AdminGameConfig(
                    id = existing?.id ?: "g${System.currentTimeMillis()}",
                    gameName = name,
                    rtp = rtp,
                    houseEdge = house
                )
                if (isEdit) {
                    viewModel.updateGameConfig(config)
                } else {
                    viewModel.addGameConfig(config)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showUserConfigDialog(existing: AdminUserConfig?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_admin_user_config, null)
        val userIdInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_user_id)
        val qualificationInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_user_qualification)
        val rtpInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_user_rtp)
        val houseInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_user_house)
        val isEdit = existing != null
        if (existing != null) {
            userIdInput.setText(existing.userId)
            qualificationInput.setText(existing.qualification)
            rtpInput.setText(existing.rtp.toString())
            houseInput.setText(existing.houseEdge.toString())
        } else {
            val defaults = viewModel.viewState.value
            rtpInput.setText(defaults.globalRtp.toString())
            houseInput.setText(defaults.globalHouseEdge.toString())
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) "Edit User Config" else "Add User Config")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val userId = userIdInput.text?.toString()?.trim().orEmpty()
                val qualification = qualificationInput.text?.toString()?.trim().orEmpty()
                val rtp = rtpInput.text?.toString()?.trim()?.toDoubleOrNull()
                val house = houseInput.text?.toString()?.trim()?.toDoubleOrNull()
                if (userId.isBlank() || qualification.isBlank() || rtp == null || house == null) {
                    return@setPositiveButton
                }
                val config = AdminUserConfig(
                    id = existing?.id ?: "u${System.currentTimeMillis()}",
                    userId = userId,
                    qualification = qualification,
                    rtp = rtp,
                    houseEdge = house
                )
                if (isEdit) {
                    viewModel.updateUserConfig(config)
                } else {
                    viewModel.addUserConfig(config)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLockRuleDialog(existing: AdminLockRule?) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_admin_lock_rule, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_name)
        val cooldownInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_cooldown)
        val turnoverInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_turnover)
        val lossInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_loss)
        val periodInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_period)
        val maxActionsInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_max_actions)
        val scopeInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_scope)
        val actionsInput = dialogView.findViewById<TextInputEditText>(R.id.input_admin_lock_actions)
        val isEdit = existing != null

        if (existing != null) {
            nameInput.setText(existing.name)
            cooldownInput.setText(existing.cooldownMinutes.toString())
            turnoverInput.setText(existing.minTurnover.toString())
            lossInput.setText(existing.maxLoss.toString())
            periodInput.setText(existing.periodMinutes.toString())
            maxActionsInput.setText(existing.maxActionsPerPeriod.toString())
            scopeInput.setText(existing.scope)
            actionsInput.setText(existing.actions.joinToString(","))
        } else {
            cooldownInput.setText("15")
            turnoverInput.setText("1000")
            lossInput.setText("500")
            periodInput.setText("1440")
            maxActionsInput.setText("5")
            scopeInput.setText("user")
            actionsInput.setText("withdrawals,gifts")
        }

        AlertDialog.Builder(requireContext())
            .setTitle(if (isEdit) "Edit Lock Rule" else "Add Lock Rule")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = nameInput.text?.toString()?.trim().orEmpty()
                val cooldown = cooldownInput.text?.toString()?.trim()?.toIntOrNull() ?: 0
                val turnover = turnoverInput.text?.toString()?.trim()?.toLongOrNull() ?: 0
                val maxLoss = lossInput.text?.toString()?.trim()?.toLongOrNull() ?: 0
                val period = periodInput.text?.toString()?.trim()?.toIntOrNull() ?: 0
                val maxActions = maxActionsInput.text?.toString()?.trim()?.toIntOrNull() ?: 0
                val scope = scopeInput.text?.toString()?.trim().orEmpty()
                val actions = actionsInput.text?.toString()?.split(",")
                    ?.map { it.trim() }
                    ?.filter { it.isNotBlank() }
                    .orEmpty()
                if (name.isBlank()) {
                    return@setPositiveButton
                }
                val rule = AdminLockRule(
                    id = existing?.id ?: "r${System.currentTimeMillis()}",
                    name = name,
                    cooldownMinutes = cooldown,
                    minTurnover = turnover,
                    maxLoss = maxLoss,
                    periodMinutes = period,
                    maxActionsPerPeriod = maxActions,
                    scope = if (scope.isBlank()) "user" else scope,
                    actions = if (actions.isEmpty()) listOf("withdrawals") else actions
                )
                if (isEdit) {
                    viewModel.updateLockRule(rule)
                } else {
                    viewModel.addLockRule(rule)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
