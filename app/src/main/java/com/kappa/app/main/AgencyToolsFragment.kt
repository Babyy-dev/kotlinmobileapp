package com.kappa.app.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.kappa.app.R
import com.kappa.app.agency.presentation.AgencyApplicationAdapter
import com.kappa.app.agency.presentation.AgencyViewModel
import com.kappa.app.main.SimpleRowAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AgencyToolsFragment : Fragment() {

    private val agencyViewModel: AgencyViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_agency_tools, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val statusText = view.findViewById<TextView>(R.id.text_agency_status)
        val messageText = view.findViewById<TextView>(R.id.text_agency_message)
        val totalDiamondsText = view.findViewById<TextView>(R.id.text_agency_total_diamonds)
        val teamInput = view.findViewById<TextInputEditText>(R.id.input_agency_team)
        val createTeamButton = view.findViewById<MaterialButton>(R.id.button_agency_create_team)
        val applyAgencyButton = view.findViewById<MaterialButton>(R.id.button_agency_apply)
        val applyResellerButton = view.findViewById<MaterialButton>(R.id.button_reseller_apply)
        val refreshButton = view.findViewById<MaterialButton>(R.id.button_agency_refresh)
        val roomsRecycler = view.findViewById<RecyclerView>(R.id.recycler_agency_rooms)
        val hostsRecycler = view.findViewById<RecyclerView>(R.id.recycler_agency_hosts)
        val appsRecycler = view.findViewById<RecyclerView>(R.id.recycler_agency_applications)
        val teamsRecycler = view.findViewById<RecyclerView>(R.id.recycler_agency_teams)
        val commissionsRecycler = view.findViewById<RecyclerView>(R.id.recycler_agency_commissions)

        val roomsAdapter = SimpleRowAdapter()
        val hostsAdapter = SimpleRowAdapter()
        val appsAdapter = AgencyApplicationAdapter(
            onApprove = { app -> agencyViewModel.approveApplication(app.id) },
            onReject = { app -> agencyViewModel.rejectApplication(app.id) }
        )
        val teamsAdapter = SimpleRowAdapter()
        val commissionsAdapter = SimpleRowAdapter()
        roomsRecycler.adapter = roomsAdapter
        hostsRecycler.adapter = hostsAdapter
        appsRecycler.adapter = appsAdapter
        teamsRecycler.adapter = teamsAdapter
        commissionsRecycler.adapter = commissionsAdapter

        createTeamButton.setOnClickListener {
            val name = teamInput.text?.toString()?.trim().orEmpty()
            agencyViewModel.createTeam(name)
        }

        applyAgencyButton.setOnClickListener {
            val name = teamInput.text?.toString()?.trim().orEmpty().ifBlank { "My Agency" }
            agencyViewModel.applyForAgency(name)
        }

        applyResellerButton.setOnClickListener {
            agencyViewModel.applyForReseller()
        }

        refreshButton.setOnClickListener {
            agencyViewModel.refreshAll()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                agencyViewModel.viewState.collect { state ->
                    val status = "Applications: ${if (state.agencyApplication == null) 0 else 1}" +
                        " • Teams: ${state.teams.size} • Commissions: ${state.commissions.size}"
                    statusText.text = status
                    if (state.actionMessage != null) {
                        messageText.text = state.actionMessage
                        messageText.visibility = View.VISIBLE
                    } else if (state.error != null) {
                        messageText.text = state.error
                        messageText.visibility = View.VISIBLE
                    } else {
                        messageText.visibility = View.GONE
                    }
                    val totalDiamonds = state.commissions.sumOf { it.diamondsAmount }
                    totalDiamondsText.text = "Total: $totalDiamonds"
                    roomsAdapter.submitRows(state.rooms)
                    hostsAdapter.submitRows(state.hosts)
                    appsAdapter.submitItems(state.agencyApplications)
                    teamsAdapter.submitRows(state.teams.map { it.name to "Owner: ${it.ownerUserId}" })
                    commissionsAdapter.submitRows(
                        state.commissions.map { it.id to "Diamonds: ${it.diamondsAmount} • USD ${it.commissionUsd}" }
                    )
                    createTeamButton.isEnabled = !state.isRefreshing
                    applyAgencyButton.isEnabled = !state.isRefreshing
                    applyResellerButton.isEnabled = !state.isRefreshing
                    refreshButton.isEnabled = !state.isRefreshing
                }
            }
        }
    }
}
