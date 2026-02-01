package com.kappa.app.game.presentation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R

class GamePlayersAdapter : RecyclerView.Adapter<GamePlayersAdapter.PlayerViewHolder>() {

    private val items = mutableListOf<GamePlayer>()

    fun submitList(players: List<GamePlayer>) {
        items.clear()
        items.addAll(players)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlayerViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_game_player, parent, false)
        return PlayerViewHolder(view)
    }

    override fun onBindViewHolder(holder: PlayerViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class PlayerViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText = itemView.findViewById<TextView>(R.id.text_player_name)
        private val scoreText = itemView.findViewById<TextView>(R.id.text_player_score)

        fun bind(player: GamePlayer) {
            nameText.text = player.name
            scoreText.text = "Score: ${player.score}"
        }
    }
}
