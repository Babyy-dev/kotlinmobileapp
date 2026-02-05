package com.kappa.app.agency.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kappa.app.R
import com.kappa.app.agency.domain.model.AgencyApplication

class AgencyApplicationAdapter(
    private val onApprove: (AgencyApplication) -> Unit,
    private val onReject: (AgencyApplication) -> Unit
) : RecyclerView.Adapter<AgencyApplicationAdapter.ApplicationViewHolder>() {

    private val items = mutableListOf<AgencyApplication>()

    fun submitItems(apps: List<AgencyApplication>) {
        items.clear()
        items.addAll(apps)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ApplicationViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_agency_application, parent, false)
        return ApplicationViewHolder(view, onApprove, onReject)
    }

    override fun onBindViewHolder(holder: ApplicationViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ApplicationViewHolder(
        itemView: View,
        private val onApprove: (AgencyApplication) -> Unit,
        private val onReject: (AgencyApplication) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.text_agency_app_name)
        private val statusText = itemView.findViewById<TextView>(R.id.text_agency_app_status)
        private val approveButton = itemView.findViewById<MaterialButton>(R.id.button_agency_app_approve)
        private val rejectButton = itemView.findViewById<MaterialButton>(R.id.button_agency_app_reject)

        fun bind(application: AgencyApplication) {
            nameText.text = application.agencyName
            statusText.text = "Status: ${application.status}"
            val pending = application.status.equals("PENDING", ignoreCase = true)
            approveButton.isEnabled = pending
            rejectButton.isEnabled = pending
            approveButton.setOnClickListener { onApprove(application) }
            rejectButton.setOnClickListener { onReject(application) }
        }
    }
}
