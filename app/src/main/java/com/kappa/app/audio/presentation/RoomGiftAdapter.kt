package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R

data class GiftItem(
    val id: String,
    val name: String,
    val price: Long,
    val conversionRate: Double,
    val category: GiftCategory,
    val giftType: String
)

enum class GiftCategory {
    MULTIPLIER,
    UNIQUE
}

class RoomGiftAdapter(
    private val onSelected: (GiftItem) -> Unit
) : RecyclerView.Adapter<RoomGiftAdapter.GiftViewHolder>() {

    private val items = mutableListOf<GiftItem>()
    private var selectedId: String? = null

    fun submitList(gifts: List<GiftItem>) {
        items.clear()
        items.addAll(gifts)
        notifyDataSetChanged()
    }

    fun setSelected(id: String?) {
        selectedId = id
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GiftViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_gift, parent, false)
        return GiftViewHolder(view, onSelected)
    }

    override fun onBindViewHolder(holder: GiftViewHolder, position: Int) {
        holder.bind(items[position], items[position].id == selectedId)
    }

    override fun getItemCount(): Int = items.size

    class GiftViewHolder(
        itemView: View,
        private val onSelected: (GiftItem) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val icon = itemView.findViewById<ImageView>(R.id.image_gift_icon)
        private val nameText = itemView.findViewById<TextView>(R.id.text_gift_name)
        private val priceText = itemView.findViewById<TextView>(R.id.text_gift_price)

        fun bind(item: GiftItem, selected: Boolean) {
            nameText.text = item.name
            priceText.text = item.price.toString()
            itemView.setBackgroundResource(
                if (selected) R.drawable.bg_gift_item_selected else R.drawable.bg_gift_item
            )
            icon.setOnClickListener { onSelected(item) }
            itemView.setOnClickListener { onSelected(item) }
        }
    }
}
