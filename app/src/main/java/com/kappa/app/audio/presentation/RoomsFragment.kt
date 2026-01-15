package com.kappa.app.audio.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.kappa.app.R
import com.kappa.app.core.network.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RoomsFragment : Fragment() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val audioViewModel: AudioViewModel by activityViewModels()
    private lateinit var roomsAdapter: RoomsAdapter
    private var lastNavigatedRoomId: String? = null
    private var shouldRefreshOnReconnect = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_rooms, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val recyclerView = view.findViewById<RecyclerView>(R.id.recycler_rooms)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_rooms)
        val errorText = view.findViewById<TextView>(R.id.text_rooms_error)
        val refreshButton = view.findViewById<View>(R.id.button_refresh_rooms)

        roomsAdapter = RoomsAdapter { room ->
            audioViewModel.joinRoom(room.id)
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = roomsAdapter

        audioViewModel.loadAudioRooms()

        refreshButton.setOnClickListener {
            audioViewModel.loadAudioRooms()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    progressBar.visibility = if (state.isLoading || state.isJoining) View.VISIBLE else View.GONE
                    if (state.error != null) {
                        errorText.text = state.error
                        errorText.visibility = View.VISIBLE
                        if (state.error.contains("No internet", ignoreCase = true) ||
                            state.error.contains("timeout", ignoreCase = true)
                        ) {
                            shouldRefreshOnReconnect = true
                        }
                    } else {
                        errorText.visibility = View.GONE
                    }
                    roomsAdapter.submitList(state.rooms)

                    val activeRoom = state.activeRoom
                    if (activeRoom == null) {
                        lastNavigatedRoomId = null
                    }
                    if (activeRoom != null && state.token != null) {
                        if (lastNavigatedRoomId != activeRoom.id &&
                            findNavController().currentDestination?.id == R.id.navigation_rooms
                        ) {
                            lastNavigatedRoomId = activeRoom.id
                            findNavController().navigate(R.id.navigation_room_detail)
                        }
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline.collect { isOnline ->
                    if (isOnline && shouldRefreshOnReconnect) {
                        shouldRefreshOnReconnect = false
                        audioViewModel.loadAudioRooms()
                    }
                }
            }
        }
    }
}
