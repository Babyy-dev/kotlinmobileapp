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
import com.kappa.app.R
import com.kappa.app.domain.audio.SeatStatus
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoomSeatsFragment : Fragment() {

    private val audioViewModel: AudioViewModel by activityViewModels()
    private lateinit var seatsAdapter: RoomSeatsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room_seats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recycler = view.findViewById<RecyclerView>(R.id.recycler_room_seats)
        seatsAdapter = RoomSeatsAdapter(
            onSeatClick = { seat ->
                when (seat.status) {
                    SeatStatus.FREE -> audioViewModel.takeSeat(seat.seatNumber)
                    SeatStatus.OCCUPIED -> {
                        val currentUserId = audioViewModel.currentUserId()
                        if (currentUserId != null && currentUserId == seat.userId) {
                            audioViewModel.leaveSeat(seat.seatNumber)
                        } else {
                            Snackbar.make(view, "Seat occupied", Snackbar.LENGTH_SHORT).show()
                        }
                    }
                    SeatStatus.BLOCKED -> {
                        Snackbar.make(view, "Seat locked", Snackbar.LENGTH_LONG)
                            .setAction("Request") {
                                audioViewModel.requestSeat(seat.seatNumber)
                            }
                            .show()
                    }
                }
            },
            onSeatLongClick = { seat ->
                when (seat.status) {
                    SeatStatus.FREE -> audioViewModel.lockSeat(seat.seatNumber)
                    SeatStatus.BLOCKED -> audioViewModel.unlockSeat(seat.seatNumber)
                    SeatStatus.OCCUPIED -> Snackbar.make(view, "Cannot lock occupied seat", Snackbar.LENGTH_SHORT).show()
                }
            }
        )
        recycler.layoutManager = GridLayoutManager(requireContext(), 4)
        recycler.adapter = seatsAdapter

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    seatsAdapter.submitList(state.seats)
                }
            }
        }
    }
}
