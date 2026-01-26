package com.kappa.app.audio.presentation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import com.kappa.app.domain.audio.AudioRoom
import com.google.android.material.textfield.TextInputEditText
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
        val createButton = view.findViewById<View>(R.id.button_create_room)

        roomsAdapter = RoomsAdapter { room ->
            if (room.requiresPassword) {
                showPasswordPrompt(room)
            } else {
                audioViewModel.joinRoom(room.id)
            }
        }

        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = roomsAdapter

        audioViewModel.loadAudioRooms()

        refreshButton.setOnClickListener {
            audioViewModel.loadAudioRooms()
        }

        createButton.setOnClickListener {
            showCreateRoomDialog()
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

    private fun showCreateRoomDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_name)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_password)

        AlertDialog.Builder(requireContext())
            .setTitle("Create Room")
            .setView(dialogView)
            .setPositiveButton("Create") { _, _ ->
                val name = nameInput.text?.toString()?.trim().orEmpty()
                if (name.isBlank()) {
                    Toast.makeText(requireContext(), "Room name is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val password = passwordInput.text?.toString()?.trim().orEmpty().ifBlank { null }
                audioViewModel.createRoom(name, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showPasswordPrompt(room: AudioRoom) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_create_room, null)
        val nameInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_name)
        val passwordInput = dialogView.findViewById<TextInputEditText>(R.id.input_room_password)
        nameInput.setText(room.name)
        nameInput.isEnabled = false

        AlertDialog.Builder(requireContext())
            .setTitle("Room Password")
            .setView(dialogView)
            .setPositiveButton("Join") { _, _ ->
                val password = passwordInput.text?.toString()?.trim().orEmpty()
                if (password.isBlank()) {
                    Toast.makeText(requireContext(), "Password is required", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                audioViewModel.joinRoom(room.id, password)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
