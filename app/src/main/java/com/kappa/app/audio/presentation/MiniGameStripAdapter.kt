package com.kappa.app.audio.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import com.kappa.app.domain.home.MiniGame

class MiniGameStripAdapter(
    private val onClick: (MiniGame) -> Unit
) : RecyclerView.Adapter<MiniGameStripAdapter.GameViewHolder>() {

    private val items = mutableListOf<MiniGame>()

    fun submitList(list: List<MiniGame>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_mini_game, parent, false)
        return GameViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GameViewHolder(
        itemView: View,
        private val onClick: (MiniGame) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.text_mini_game_title)
        private val desc: TextView = itemView.findViewById(R.id.text_mini_game_desc)
        private val fee: TextView = itemView.findViewById(R.id.text_mini_game_fee)

        fun bind(item: MiniGame) {
            title.text = item.title
            desc.text = item.description
            fee.text = "${item.entryFee} coins"
            itemView.setOnClickListener { onClick(item) }
        }
    }
}
