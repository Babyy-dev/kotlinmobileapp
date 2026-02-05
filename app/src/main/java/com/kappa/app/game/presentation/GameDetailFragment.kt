package com.kappa.app.game.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.kappa.app.R
import com.kappa.app.audio.presentation.AudioViewModel
import com.kappa.app.core.storage.PreferencesManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GameDetailFragment : Fragment() {

    private val playersAdapter = GamePlayersAdapter()
    private val viewModel: GameViewModel by viewModels()
    private val audioViewModel: AudioViewModel by activityViewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_game_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val titleText = view.findViewById<TextView>(R.id.text_game_detail_title)
        val subtitleText = view.findViewById<TextView>(R.id.text_game_detail_subtitle)
        val timerText = view.findViewById<TextView>(R.id.text_game_timer)
        val balanceText = view.findViewById<TextView>(R.id.text_game_balance)
        val scoreText = view.findViewById<TextView>(R.id.text_game_score)
        val joinButton = view.findViewById<MaterialButton>(R.id.button_game_join_now)
        val startButton = view.findViewById<MaterialButton>(R.id.button_game_start)
        val actionButton = view.findViewById<MaterialButton>(R.id.button_game_action)
        val actionAltButton = view.findViewById<MaterialButton>(R.id.button_game_action_alt)
        val playersList = view.findViewById<RecyclerView>(R.id.recycler_game_players)

        val typeName = arguments?.getString("game_type")
        val title = arguments?.getString("game_title") ?: "Game"
        val entryFee = arguments?.getLong("game_fee") ?: 0L
        val gameType = runCatching { GameType.valueOf(typeName ?: "") }.getOrDefault(GameType.LUCKY_DRAW)

        titleText.text = title
        subtitleText.text = buildSubtitle(gameType)

        playersList.layoutManager = LinearLayoutManager(requireContext())
        playersList.adapter = playersAdapter

        joinButton.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                val userId = preferencesManager.getUserIdOnce() ?: "guest"
                val roomId = audioViewModel.viewState.value.activeRoom?.id
                if (roomId.isNullOrBlank()) {
                    subtitleText.text = "Join a room to play this game"
                    return@launch
                }
                viewModel.configure(
                    gameId = "game_${gameType.name.lowercase()}",
                    title = title,
                    type = gameType,
                    balance = viewModel.viewState.value.balance,
                    userId = userId,
                    roomId = roomId
                )
                viewModel.join(entryFee)
            }
        }

        startButton.setOnClickListener {
            // Server-driven start; actions are allowed once joined
            actionButton.isEnabled = true
            actionAltButton.isEnabled = true
        }

        actionButton.setOnClickListener {
            val actionType = when (gameType) {
                GameType.LUCKY_DRAW -> "SPIN"
                GameType.BATTLE -> "ATTACK"
                GameType.GIFT_RUSH -> "GIFT"
                GameType.TAP_SPEED -> "TAP"
            }
            viewModel.sendAction(actionType)
            if (gameType == GameType.GIFT_RUSH) {
                val giftId = arguments?.getString("gift_id")
                if (giftId.isNullOrBlank()) {
                    viewModel.setMessage("Missing gift id")
                } else {
                    viewModel.sendGiftPlay(giftId, 1)
                }
            }
        }

        actionAltButton.setOnClickListener {
            if (gameType == GameType.TAP_SPEED) {
                viewModel.sendAction("TAP")
            }
        }

        actionButton.text = when (gameType) {
            GameType.LUCKY_DRAW -> "Spin"
            GameType.BATTLE -> "Attack"
            GameType.GIFT_RUSH -> "Send Gift"
            GameType.TAP_SPEED -> "Tap"
        }
        actionAltButton.visibility = if (gameType == GameType.TAP_SPEED) View.VISIBLE else View.GONE

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.viewState.collect { state ->
                    timerText.text = "00:${state.timeLeft.toString().padStart(2, '0')}"
                    balanceText.text = "Balance: ${state.balance}"
                    val me = state.players.firstOrNull { it.id == state.userId }
                    scoreText.text = "Score: ${me?.score ?: 0}"
                    playersAdapter.submitList(state.players)
                    if (state.message != null) {
                        subtitleText.text = state.message
                        viewModel.clearMessage()
                    }
                }
            }
        }
    }

    private fun buildSubtitle(type: GameType): String {
        return when (type) {
            GameType.LUCKY_DRAW -> "Result is server-driven; animation is visual"
            GameType.BATTLE -> "Score the highest in 30 seconds"
            GameType.GIFT_RUSH -> "Gifts add points to the leaderboard"
            GameType.TAP_SPEED -> "Fast taps win (rate limited)"
        }
    }
}
