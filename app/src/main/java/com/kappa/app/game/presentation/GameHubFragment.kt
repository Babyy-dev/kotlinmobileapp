package com.kappa.app.game.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class GameHubFragment : Fragment() {

    private val viewModel: GameHubViewModel by viewModels()

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

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.state.collect { state ->
                    val mapped = state.games.map { game ->
                        val type = when (game.id.lowercase()) {
                            "lucky_draw" -> GameType.LUCKY_DRAW
                            "battle_arena" -> GameType.BATTLE
                            "gift_rush" -> GameType.GIFT_RUSH
                            "tap_speed" -> GameType.TAP_SPEED
                            else -> GameType.LUCKY_DRAW
                        }
                        GameCard(type, game.title, game.description, game.entryFee)
                    }
                    adapter.submitList(mapped)
                }
            }
        }
    }
}
