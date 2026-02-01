package com.kappa.app.game.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kappa.app.R

class GameHubAdapter(
    private val onClick: (GameCard) -> Unit
) : RecyclerView.Adapter<GameHubAdapter.GameViewHolder>() {

    private val items = mutableListOf<GameCard>()

    fun submitList(cards: List<GameCard>) {
        items.clear()
        items.addAll(cards)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GameViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_card, parent, false)
        return GameViewHolder(view, onClick)
    }

    override fun onBindViewHolder(holder: GameViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class GameViewHolder(
        itemView: View,
        private val onClick: (GameCard) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val title = itemView.findViewById<TextView>(R.id.text_game_title)
        private val subtitle = itemView.findViewById<TextView>(R.id.text_game_subtitle)
        private val fee = itemView.findViewById<TextView>(R.id.text_game_fee)
        private val button = itemView.findViewById<MaterialButton>(R.id.button_game_join)

        fun bind(card: GameCard) {
            title.text = card.title
            subtitle.text = card.subtitle
            fee.text = "Entry: ${card.entryFee} coins"
            button.setOnClickListener { onClick(card) }
        }
    }
}
