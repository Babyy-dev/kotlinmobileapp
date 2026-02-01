package com.kappa.app.game.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R

class GameHubFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_hub, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_games)
        val adapter = GameHubAdapter { game ->
            val bundle = Bundle().apply {
                putString("game_type", game.type.name)
                putString("game_title", game.title)
                putLong("game_fee", game.entryFee)
            }
            findNavController().navigate(R.id.navigation_game_detail, bundle)
        }
        recycler.layoutManager = LinearLayoutManager(requireContext())
        recycler.adapter = adapter

        adapter.submitList(
            listOf(
                GameCard(GameType.LUCKY_DRAW, "Lucky Draw", "Spin and win rewards", 200),
                GameCard(GameType.BATTLE, "Battle Arena", "Score more in 30 seconds", 300),
                GameCard(GameType.GIFT_RUSH, "Gift Rush", "Send gifts to climb the rank", 500),
                GameCard(GameType.TAP_SPEED, "Tap Speed", "Fastest taps win", 150)
            )
        )
    }
}
