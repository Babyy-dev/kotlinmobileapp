package com.kappa.app.audio.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kappa.app.R
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.room.Room
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RoomDetailFragment : Fragment() {

    private val audioViewModel: AudioViewModel by activityViewModels()
    private var liveKitRoom: Room? = null
    private var hasAudioPermission: Boolean = false

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasAudioPermission = granted
        if (granted) {
            connectIfReady()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_room_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val titleText = view.findViewById<TextView>(R.id.text_room_title)
        val statusText = view.findViewById<TextView>(R.id.text_room_status)
        val leaveButton = view.findViewById<MaterialButton>(R.id.button_leave_room)

        leaveButton.setOnClickListener {
            disconnectRoom()
            audioViewModel.leaveRoom()
            findNavController().popBackStack()
        }

        hasAudioPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            statusText.text = "Microphone permission required"
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    titleText.text = state.activeRoom?.name ?: "Room"
                    if (state.token == null || state.livekitUrl == null) {
                        statusText.text = state.error ?: "Joining room..."
                    } else if (liveKitRoom == null && hasAudioPermission) {
                        connectIfReady()
                        statusText.text = "Connecting..."
                    }
                }
            }
        }
    }

    private fun connectIfReady() {
        val state = audioViewModel.viewState.value
        val token = state.token
        val url = state.livekitUrl
        if (token.isNullOrBlank() || url.isNullOrBlank()) {
            return
        }
        if (liveKitRoom != null) {
            return
        }
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                liveKitRoom = LiveKit.connect(requireContext(), url, token)
                liveKitRoom?.localParticipant?.setMicrophoneEnabled(true)
                view?.findViewById<TextView>(R.id.text_room_status)?.text = "Connected"
            } catch (throwable: Throwable) {
                liveKitRoom = null
                view?.findViewById<TextView>(R.id.text_room_status)?.text =
                    throwable.message ?: "Connection failed"
            }
        }
    }

    private fun disconnectRoom() {
        liveKitRoom?.disconnect()
        liveKitRoom?.release()
        liveKitRoom = null
    }

    override fun onDestroyView() {
        audioViewModel.leaveRoom()
        disconnectRoom()
        super.onDestroyView()
    }
}
