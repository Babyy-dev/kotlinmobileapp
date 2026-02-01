package com.kappa.app.admin.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kappa.app.R

class AdminGameConfigAdapter(
    private val onEdit: (AdminGameConfig) -> Unit,
    private val onDelete: (AdminGameConfig) -> Unit
) : RecyclerView.Adapter<AdminGameConfigAdapter.ConfigViewHolder>() {

    private val items = mutableListOf<AdminGameConfig>()

    fun submitItems(configs: List<AdminGameConfig>) {
        items.clear()
        items.addAll(configs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_config, parent, false)
        return ConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConfigViewHolder, position: Int) {
        val item = items[position]
        holder.bind(
            title = item.gameName,
            subtitle = "RTP ${item.rtp}% | House ${item.houseEdge}%",
            onEdit = { onEdit(item) },
            onDelete = { onDelete(item) }
        )
    }

    override fun getItemCount(): Int = items.size

    class ConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.text_admin_config_title)
        private val subtitle = itemView.findViewById<TextView>(R.id.text_admin_config_subtitle)
        private val editButton = itemView.findViewById<MaterialButton>(R.id.button_admin_edit)
        private val deleteButton = itemView.findViewById<MaterialButton>(R.id.button_admin_delete)

        fun bind(title: String, subtitle: String, onEdit: () -> Unit, onDelete: () -> Unit) {
            this.title.text = title
            this.subtitle.text = subtitle
            editButton.setOnClickListener { onEdit() }
            deleteButton.setOnClickListener { onDelete() }
        }
    }
}

class AdminUserConfigAdapter(
    private val onEdit: (AdminUserConfig) -> Unit,
    private val onDelete: (AdminUserConfig) -> Unit
) : RecyclerView.Adapter<AdminUserConfigAdapter.ConfigViewHolder>() {

    private val items = mutableListOf<AdminUserConfig>()

    fun submitItems(configs: List<AdminUserConfig>) {
        items.clear()
        items.addAll(configs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_config, parent, false)
        return ConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConfigViewHolder, position: Int) {
        val item = items[position]
        holder.bind(
            title = item.userId,
            subtitle = "${item.qualification} | RTP ${item.rtp}% | House ${item.houseEdge}%",
            onEdit = { onEdit(item) },
            onDelete = { onDelete(item) }
        )
    }

    override fun getItemCount(): Int = items.size

    class ConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.text_admin_config_title)
        private val subtitle = itemView.findViewById<TextView>(R.id.text_admin_config_subtitle)
        private val editButton = itemView.findViewById<MaterialButton>(R.id.button_admin_edit)
        private val deleteButton = itemView.findViewById<MaterialButton>(R.id.button_admin_delete)

        fun bind(title: String, subtitle: String, onEdit: () -> Unit, onDelete: () -> Unit) {
            this.title.text = title
            this.subtitle.text = subtitle
            editButton.setOnClickListener { onEdit() }
            deleteButton.setOnClickListener { onDelete() }
        }
    }
}

class AdminLockAdapter(
    private val onEdit: (AdminLockRule) -> Unit,
    private val onDelete: (AdminLockRule) -> Unit
) : RecyclerView.Adapter<AdminLockAdapter.LockViewHolder>() {

    private val items = mutableListOf<AdminLockRule>()

    fun submitLocks(rules: List<AdminLockRule>) {
        items.clear()
        items.addAll(rules)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LockViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_config, parent, false)
        return LockViewHolder(view)
    }

    override fun onBindViewHolder(holder: LockViewHolder, position: Int) {
        val item = items[position]
        val periodText = if (item.periodMinutes > 0 && item.maxActionsPerPeriod > 0) {
            " | Limit ${item.maxActionsPerPeriod} per ${item.periodMinutes}m"
        } else {
            ""
        }
        holder.bind(
            title = item.name,
            subtitle = "Scope ${item.scope} | Actions ${item.actions.joinToString()} | Cooldown ${item.cooldownMinutes}m | Min turnover ${item.minTurnover} | Max loss ${item.maxLoss}$periodText",
            onEdit = { onEdit(item) },
            onDelete = { onDelete(item) }
        )
    }

    override fun getItemCount(): Int = items.size

    class LockViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.text_admin_config_title)
        private val subtitle = itemView.findViewById<TextView>(R.id.text_admin_config_subtitle)
        private val editButton = itemView.findViewById<MaterialButton>(R.id.button_admin_edit)
        private val deleteButton = itemView.findViewById<MaterialButton>(R.id.button_admin_delete)

        fun bind(title: String, subtitle: String, onEdit: () -> Unit, onDelete: () -> Unit) {
            this.title.text = title
            this.subtitle.text = subtitle
            editButton.setOnClickListener { onEdit() }
            deleteButton.setOnClickListener { onDelete() }
        }
    }
}

class QualificationConfigAdapter(
    private val onEdit: (QualificationConfig) -> Unit
) : RecyclerView.Adapter<QualificationConfigAdapter.ConfigViewHolder>() {

    private val items = mutableListOf<QualificationConfig>()

    fun submitItems(configs: List<QualificationConfig>) {
        items.clear()
        items.addAll(configs)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConfigViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_admin_config, parent, false)
        return ConfigViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConfigViewHolder, position: Int) {
        val item = items[position]
        val vipDetails = if (item.minPlayedUsd > 0 && item.durationDays > 0) {
            " | Min USD ${item.minPlayedUsd} | ${item.durationDays} days"
        } else {
            ""
        }
        holder.bind(
            title = item.qualification,
            subtitle = "RTP ${item.rtp}% | House ${item.houseEdge}%$vipDetails",
            onEdit = { onEdit(item) }
        )
    }

    override fun getItemCount(): Int = items.size

    class ConfigViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.text_admin_config_title)
        private val subtitle = itemView.findViewById<TextView>(R.id.text_admin_config_subtitle)
        private val editButton = itemView.findViewById<MaterialButton>(R.id.button_admin_edit)
        private val deleteButton = itemView.findViewById<MaterialButton>(R.id.button_admin_delete)

        fun bind(title: String, subtitle: String, onEdit: () -> Unit) {
            this.title.text = title
            this.subtitle.text = subtitle
            editButton.setOnClickListener { onEdit() }
            deleteButton.visibility = View.GONE
        }
    }
}
