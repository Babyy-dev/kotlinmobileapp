package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R

data class GiftRecipientItem(
    val userId: String,
    val name: String,
    val seatNumber: Int
)

class RoomGiftRecipientsAdapter(
    private val onToggle: (GiftRecipientItem) -> Unit
) : RecyclerView.Adapter<RoomGiftRecipientsAdapter.RecipientViewHolder>() {

    private val items = mutableListOf<GiftRecipientItem>()
    private val selected = mutableSetOf<String>()

    fun submitList(list: List<GiftRecipientItem>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    fun setSelected(ids: Set<String>) {
        selected.clear()
        selected.addAll(ids)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecipientViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gift_recipient, parent, false)
        return RecipientViewHolder(view, onToggle)
    }

    override fun onBindViewHolder(holder: RecipientViewHolder, position: Int) {
        holder.bind(items[position], selected.contains(items[position].userId))
    }

    override fun getItemCount(): Int = items.size

    class RecipientViewHolder(
        itemView: View,
        private val onToggle: (GiftRecipientItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.text_recipient_name)
        private val seatText: TextView = itemView.findViewById(R.id.text_recipient_seat)

        fun bind(item: GiftRecipientItem, isSelected: Boolean) {
            nameText.text = item.name
            seatText.text = "Seat ${item.seatNumber}"
            itemView.setBackgroundResource(
                if (isSelected) R.drawable.bg_gift_item_selected else R.drawable.bg_gift_item
            )
            itemView.setOnClickListener { onToggle(item) }
        }
    }
}
