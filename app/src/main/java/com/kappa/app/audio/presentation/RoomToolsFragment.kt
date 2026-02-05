package com.kappa.app.audio.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.kappa.app.R
import com.kappa.app.domain.audio.SeatStatus
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoomToolsFragment : Fragment() {

    private val audioViewModel: AudioViewModel by activityViewModels()
    private lateinit var seatsAdapter: RoomSeatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room_tools, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val seatsRecycler = view.findViewById<RecyclerView>(R.id.recycler_tools_seats)
        val userInput = view.findViewById<TextInputEditText>(R.id.input_tools_user)
        val muteButton = view.findViewById<MaterialButton>(R.id.button_tools_mute)
        val unmuteButton = view.findViewById<MaterialButton>(R.id.button_tools_unmute)
        val kickButton = view.findViewById<MaterialButton>(R.id.button_tools_kick)
        val banButton = view.findViewById<MaterialButton>(R.id.button_tools_ban)
        val statusText = view.findViewById<android.widget.TextView>(R.id.text_tools_status)

        seatsAdapter = RoomSeatsAdapter(
            onSeatClick = { seat ->
                if (seat.status == SeatStatus.OCCUPIED && seat.userId != null) {
                    userInput.setText(seat.userId)
                    Snackbar.make(view, "Selected ${seat.username ?: seat.userId}", Snackbar.LENGTH_SHORT).show()
                }
            }
        )
        seatsRecycler.layoutManager = GridLayoutManager(requireContext(), 4)
        seatsRecycler.adapter = seatsAdapter

        fun withUser(action: (String) -> Unit) {
            val userId = userInput.text?.toString()?.trim().orEmpty()
            if (userId.isBlank()) {
                statusText.text = "Enter a user id"
                statusText.visibility = View.VISIBLE
                return
            }
            statusText.visibility = View.GONE
            action(userId)
        }

        muteButton.setOnClickListener {
            withUser { userId -> audioViewModel.muteParticipant(userId, true) }
        }
        unmuteButton.setOnClickListener {
            withUser { userId -> audioViewModel.muteParticipant(userId, false) }
        }
        kickButton.setOnClickListener {
            withUser { userId -> audioViewModel.kickParticipant(userId) }
        }
        banButton.setOnClickListener {
            withUser { userId -> audioViewModel.banParticipant(userId) }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    seatsAdapter.submitList(state.seats)
                    if (state.error != null) {
                        statusText.text = state.error
                        statusText.visibility = View.VISIBLE
                    }
                }
            }
        }
    }
}
