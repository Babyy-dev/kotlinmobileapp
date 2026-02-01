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
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch

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
        val withdrawMinInput = view.findViewById<TextInputEditText>(R.id.input_admin_withdraw_min)
        val withdrawMultipleInput = view.findViewById<TextInputEditText>(R.id.input_admin_withdraw_multiple)
        val saveWithdrawButton = view.findViewById<MaterialButton>(R.id.button_admin_save_withdrawal)
        val addGameButton = view.findViewById<MaterialButton>(R.id.button_admin_add_game)
        val addUserButton = view.findViewById<MaterialButton>(R.id.button_admin_add_user)
        val addLockButton = view.findViewById<MaterialButton>(R.id.button_admin_add_lock)

        val gameAdapter = AdminGameConfigAdapter(
            onEdit = { config ->
                viewModel.updateGameConfig(config.copy(rtp = config.rtp + 0.1))
            },
            onDelete = { config -> viewModel.deleteGameConfig(config.id) }
        )
        val userAdapter = AdminUserConfigAdapter(
            onEdit = { config ->
                viewModel.updateUserConfig(config.copy(rtp = config.rtp + 0.1))
            },
            onDelete = { config -> viewModel.deleteUserConfig(config.id) }
        )
        val lockAdapter = AdminLockAdapter(
            onEdit = { rule -> viewModel.updateLockRule(rule.copy(cooldownMinutes = rule.cooldownMinutes + 5)) },
            onDelete = { rule -> viewModel.deleteLockRule(rule.id) }
        )
        val qualificationAdapter = QualificationConfigAdapter(
            onEdit = { config ->
                viewModel.updateQualificationConfig(config.copy(rtp = config.rtp + 0.5))
            }
        )

        gamesRecycler.adapter = gameAdapter
        usersRecycler.adapter = userAdapter
        locksRecycler.adapter = lockAdapter
        qualRecycler.adapter = qualificationAdapter

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

        addGameButton.setOnClickListener {
            viewModel.addGameConfig(
                AdminGameConfig(
                    id = "g${System.currentTimeMillis()}",
                    gameName = "New Game",
                    rtp = viewModel.viewState.value.globalRtp,
                    houseEdge = viewModel.viewState.value.globalHouseEdge
                )
            )
        }
        addUserButton.setOnClickListener {
            viewModel.addUserConfig(
                AdminUserConfig(
                    id = "u${System.currentTimeMillis()}",
                    userId = "user_${viewModel.viewState.value.userConfigs.size + 1}",
                    qualification = "Standard",
                    rtp = viewModel.viewState.value.globalRtp,
                    houseEdge = viewModel.viewState.value.globalHouseEdge
                )
            )
        }
        addLockButton.setOnClickListener {
            viewModel.addLockRule(
                AdminLockRule(
                    id = "r${System.currentTimeMillis()}",
                    name = "Rule ${viewModel.viewState.value.lockRules.size + 1}",
                    cooldownMinutes = 15,
                    minTurnover = 1000,
                    maxLoss = 500,
                    periodMinutes = 1440,
                    maxActionsPerPeriod = 5,
                    scope = "user",
                    actions = listOf("withdrawals", "gifts")
                )
            )
        }

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
}
