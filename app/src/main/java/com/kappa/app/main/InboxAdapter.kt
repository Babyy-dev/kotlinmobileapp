package com.kappa.app.main

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R

data class InboxItem(
    val id: String,
    val name: String,
    val message: String,
    val badge: String? = null,
    val isOnline: Boolean = false,
    val unreadCount: Int = 0
)

class InboxAdapter(
    private val onItemClick: (InboxItem) -> Unit = {}
) : RecyclerView.Adapter<InboxAdapter.InboxViewHolder>() {

    private val items = mutableListOf<InboxItem>()

    fun submitList(newItems: List<InboxItem>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InboxViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_inbox_row, parent, false)
        return InboxViewHolder(view, onItemClick)
    }

    override fun onBindViewHolder(holder: InboxViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class InboxViewHolder(
        itemView: View,
        private val onItemClick: (InboxItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.text_inbox_name)
        private val messageText = itemView.findViewById<TextView>(R.id.text_inbox_message)
        private val badgeText = itemView.findViewById<TextView>(R.id.text_inbox_badge)
        private val unreadText = itemView.findViewById<TextView>(R.id.text_inbox_unread)
        private val statusView = itemView.findViewById<View>(R.id.view_inbox_status)
        private val actionIcon = itemView.findViewById<ImageView>(R.id.image_inbox_action)

        fun bind(item: InboxItem) {
            nameText.text = item.name
            messageText.text = item.message
            if (item.badge.isNullOrBlank()) {
                badgeText.visibility = View.GONE
            } else {
                badgeText.text = item.badge
                badgeText.visibility = View.VISIBLE
            }
            if (item.unreadCount > 0) {
                unreadText.visibility = View.VISIBLE
                unreadText.text = item.unreadCount.toString()
            } else {
                unreadText.visibility = View.GONE
            }
            statusView.setBackgroundResource(
                if (item.isOnline) R.drawable.bg_status_online else R.drawable.bg_status_offline
            )
            actionIcon.contentDescription = "Chat with ${item.name}"
            itemView.setOnClickListener { onItemClick(item) }
        }
    }
}
