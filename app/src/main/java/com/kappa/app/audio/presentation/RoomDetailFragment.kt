package com.kappa.app.audio.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.kappa.app.R
import com.kappa.app.core.network.NetworkMonitor
import dagger.hilt.android.AndroidEntryPoint
import io.livekit.android.LiveKit
import io.livekit.android.RoomOptions
import io.livekit.android.room.Room
import io.livekit.android.room.track.LocalAudioTrackOptions
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class RoomDetailFragment : Fragment() {

    @Inject
    lateinit var networkMonitor: NetworkMonitor

    private val audioViewModel: AudioViewModel by activityViewModels()
    private var liveKitRoom: Room? = null
    private var hasAudioPermission: Boolean = false
    private var isOnline: Boolean = true
    private var isConnecting: Boolean = false
    private var reconnectJob: Job? = null
    private var lastErrorMessage: String? = null
    private var enableEchoCancellation: Boolean = true
    private var enableNoiseSuppression: Boolean = true
    private var lastGiftId: String? = null
    private var giftOverlayJob: Job? = null
    private var micEnabledOnJoin: Boolean = true
    private var hasSelectedMic: Boolean = false
    private var shouldLeaveOnDestroy: Boolean = true

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
        val agencyText = view.findViewById<TextView>(R.id.text_room_agency)
        val roomCodeText = view.findViewById<TextView>(R.id.text_room_code)
        val favoriteButton = view.findViewById<android.widget.ImageButton>(R.id.button_room_favorite)
        val minimizeButton = view.findViewById<android.widget.ImageButton>(R.id.button_room_minimize)
        val errorText = view.findViewById<TextView>(R.id.text_room_error)
        val giftOverlayText = view.findViewById<TextView>(R.id.text_gift_overlay)
        val giftOverlayIcon = view.findViewById<android.widget.ImageView>(R.id.image_gift_overlay)
        val leaveButton = view.findViewById<MaterialButton>(R.id.button_leave_room)
        val sectionAudio = view.findViewById<View>(R.id.card_section_audio)
        val sectionSeats = view.findViewById<View>(R.id.card_section_seats)
        val sectionChat = view.findViewById<View>(R.id.card_section_chat)
        val sectionGifts = view.findViewById<View>(R.id.card_section_gifts)
        val sectionTools = view.findViewById<View>(R.id.card_section_tools)
        val chatUnreadBadge = view.findViewById<TextView>(R.id.text_room_chat_unread)
        val actionSeat = view.findViewById<View>(R.id.button_room_action_seat)
        val actionChat = view.findViewById<View>(R.id.button_room_action_chat)
        val actionGift = view.findViewById<View>(R.id.button_room_action_gift)
        val actionTools = view.findViewById<View>(R.id.button_room_action_tools)

        val sectionCards = listOf(sectionAudio, sectionSeats, sectionChat, sectionGifts, sectionTools)
        val navController = getChildNavController()
        val navOptions = NavOptions.Builder()
            .setEnterAnim(R.anim.slide_in_right)
            .setExitAnim(R.anim.slide_out_left)
            .setPopEnterAnim(R.anim.slide_in_left)
            .setPopExitAnim(R.anim.slide_out_right)
            .build()

        fun updateSelection(selectedId: Int) {
            sectionCards.forEach { card ->
                val isSelected = card.id == selectedId
                card.isSelected = isSelected
                card.alpha = if (isSelected) 1.0f else 0.6f
            }
        }

        fun navigateTo(destinationId: Int, selectedCardId: Int) {
            navController?.navigate(destinationId, null, navOptions)
            updateSelection(selectedCardId)
        }

        sectionAudio.setOnClickListener { navigateTo(R.id.room_section_audio, R.id.card_section_audio) }
        sectionSeats.setOnClickListener { navigateTo(R.id.room_section_seats, R.id.card_section_seats) }
        sectionChat.setOnClickListener {
            navigateTo(R.id.room_section_chat, R.id.card_section_chat)
            audioViewModel.markMessagesSeen()
        }
        sectionGifts.setOnClickListener { navigateTo(R.id.room_section_gifts, R.id.card_section_gifts) }
        sectionTools.setOnClickListener { navigateTo(R.id.room_section_tools, R.id.card_section_tools) }

        navController?.addOnDestinationChangedListener { _, destination, _ ->
            val selected = when (destination.id) {
                R.id.room_section_audio -> R.id.card_section_audio
                R.id.room_section_seats -> R.id.card_section_seats
                R.id.room_section_gifts -> R.id.card_section_gifts
                R.id.room_section_tools -> R.id.card_section_tools
                else -> R.id.card_section_chat
            }
            updateSelection(selected)
        }

        leaveButton.setOnClickListener {
            shouldLeaveOnDestroy = true
            disconnectRoom()
            audioViewModel.leaveRoom()
            findNavController().popBackStack()
        }

        minimizeButton.setOnClickListener {
            shouldLeaveOnDestroy = false
            findNavController().navigate(R.id.navigation_rooms)
        }

        favoriteButton.setOnClickListener {
            val isFavorite = audioViewModel.viewState.value.activeRoom?.isFavorite ?: false
            audioViewModel.toggleFavoriteRoom(!isFavorite)
        }

        actionSeat.setOnClickListener { navigateTo(R.id.room_section_seats, R.id.card_section_seats) }
        actionChat.setOnClickListener {
            navigateTo(R.id.room_section_chat, R.id.card_section_chat)
            audioViewModel.markMessagesSeen()
        }
        actionGift.setOnClickListener { navigateTo(R.id.room_section_gifts, R.id.card_section_gifts) }
        actionTools.setOnClickListener { navigateTo(R.id.room_section_tools, R.id.card_section_tools) }

        hasAudioPermission = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasAudioPermission) {
            statusText.text = "Microphone permission required"
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }

        showMicChoice()

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                audioViewModel.viewState.collect { state ->
                    titleText.text = state.activeRoom?.name ?: "Room"
                    agencyText.text = state.activeRoom?.agencyName ?: "Agency"
                    roomCodeText.text = "ID: ${state.activeRoom?.roomCode ?: "-"}"
                    val favorite = state.activeRoom?.isFavorite == true
                    favoriteButton.setImageResource(R.drawable.ic_star)
                    favoriteButton.alpha = if (favorite) 1.0f else 0.5f
                    if (!isOnline) {
                        statusText.text = "No internet connection"
                    } else if (state.token == null || state.livekitUrl == null) {
                        statusText.text = state.error ?: "Joining room..."
                    } else if (liveKitRoom == null && hasAudioPermission) {
                        connectIfReady()
                        statusText.text = "Connecting..."
                    }
                    if (state.error != null && state.token != null && isOnline) {
                        statusText.text = state.error
                    }
                    if (state.error != null) {
                        errorText.text = state.error
                        errorText.visibility = View.VISIBLE
                        showErrorOnce(state.error)
                    } else {
                        errorText.visibility = View.GONE
                        lastErrorMessage = null
                    }
                    val latestGift = state.gifts.lastOrNull()
                    if (latestGift != null && latestGift.id != lastGiftId) {
                        lastGiftId = latestGift.id
                        showGiftOverlay(giftOverlayText, giftOverlayIcon, "Gift +${latestGift.amount} coins")
                    }
                    val unread = state.unreadRoomMessages
                    if (unread > 0) {
                        chatUnreadBadge.text = unread.coerceAtMost(99).toString()
                        chatUnreadBadge.visibility = View.VISIBLE
                    } else {
                        chatUnreadBadge.visibility = View.GONE
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                networkMonitor.isOnline.collect { online ->
                    isOnline = online
                    if (!online) {
                        statusText.text = "No internet connection"
                        disconnectRoom()
                    } else if (liveKitRoom == null) {
                        audioViewModel.reconnectRoom()
                        scheduleReconnect()
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
            audioViewModel.reconnectRoom()
            return
        }
        if (!hasAudioPermission) {
            return
        }
        if (!hasSelectedMic) {
            return
        }
        if (!isOnline) {
            return
        }
        if (liveKitRoom != null) {
            return
        }
        if (isConnecting) {
            return
        }
        isConnecting = true
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                liveKitRoom = LiveKit.connect(
                    requireContext(),
                    url,
                    token,
                    roomOptions = buildRoomOptions()
                )
                liveKitRoom?.localParticipant?.setMicrophoneEnabled(micEnabledOnJoin)
                view?.findViewById<TextView>(R.id.text_room_status)?.text = "Connected"
                cancelReconnect()
            } catch (throwable: Throwable) {
                liveKitRoom = null
                view?.findViewById<TextView>(R.id.text_room_status)?.text =
                    throwable.message ?: "Connection failed"
                if (isOnline) {
                    scheduleReconnect()
                }
            } finally {
                isConnecting = false
            }
        }
    }

    private fun disconnectRoom() {
        liveKitRoom?.disconnect()
        liveKitRoom?.release()
        liveKitRoom = null
        isConnecting = false
    }

    private fun buildRoomOptions(): RoomOptions {
        val audioOptions = LocalAudioTrackOptions(
            echoCancellation = enableEchoCancellation,
            noiseSuppression = enableNoiseSuppression,
            autoGainControl = true,
            highPassFilter = true,
            typingNoiseDetection = true
        )
        return RoomOptions(audioTrackCaptureDefaults = audioOptions)
    }

    private fun scheduleReconnect() {
        if (reconnectJob?.isActive == true) {
            return
        }
        if (!hasAudioPermission) {
            return
        }
        reconnectJob = viewLifecycleOwner.lifecycleScope.launch {
            var delayMs = 1000L
            while (isOnline && liveKitRoom == null && isAdded) {
                view?.findViewById<TextView>(R.id.text_room_status)?.text = "Reconnecting..."
                connectIfReady()
                delay(delayMs)
                delayMs = (delayMs * 2).coerceAtMost(10000L)
            }
        }
    }

    private fun cancelReconnect() {
        reconnectJob?.cancel()
        reconnectJob = null
    }

    private fun showErrorOnce(message: String) {
        if (message == lastErrorMessage) {
            return
        }
        lastErrorMessage = message
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
        audioViewModel.clearError()
    }

    private fun showGiftOverlay(view: TextView, icon: android.widget.ImageView, message: String) {
        giftOverlayJob?.cancel()
        view.text = message
        view.alpha = 0f
        view.scaleX = 0.9f
        view.scaleY = 0.9f
        view.visibility = View.VISIBLE
        icon.setImageResource(R.drawable.ic_gift)
        icon.alpha = 0f
        icon.scaleX = 0.8f
        icon.scaleY = 0.8f
        icon.rotation = -10f
        icon.visibility = View.VISIBLE
        view.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .setDuration(220)
            .start()
        icon.animate()
            .alpha(1f)
            .scaleX(1f)
            .scaleY(1f)
            .rotation(10f)
            .setDuration(220)
            .start()
        giftOverlayJob = viewLifecycleOwner.lifecycleScope.launch {
            delay(1800)
            view.animate()
                .alpha(0f)
                .setDuration(220)
                .withEndAction { view.visibility = View.GONE }
                .start()
            icon.animate()
                .alpha(0f)
                .rotation(0f)
                .setDuration(220)
                .withEndAction { icon.visibility = View.GONE }
                .start()
        }
    }

    override fun onDestroyView() {
        giftOverlayJob?.cancel()
        if (shouldLeaveOnDestroy) {
            audioViewModel.leaveRoom()
        }
        disconnectRoom()
        cancelReconnect()
        super.onDestroyView()
    }

    private fun showMicChoice() {
        if (!isAdded) return
        val dialog = com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
            .setTitle("Join with microphone")
            .setMessage("Choose your microphone state when joining this room.")
            .setPositiveButton("Mic On") { _, _ ->
                micEnabledOnJoin = true
                hasSelectedMic = true
                connectIfReady()
            }
            .setNegativeButton("Muted") { _, _ ->
                micEnabledOnJoin = false
                hasSelectedMic = true
                connectIfReady()
            }
            .setNeutralButton("Cancel", null)
            .create()
        dialog.show()
    }

    fun updateAudioOptions(echoCancellation: Boolean, noiseSuppression: Boolean) {
        enableEchoCancellation = echoCancellation
        enableNoiseSuppression = noiseSuppression
    }

    fun getAudioOptions(): Pair<Boolean, Boolean> {
        return Pair(enableEchoCancellation, enableNoiseSuppression)
    }

    fun applyAudioSettings() {
        disconnectRoom()
        connectIfReady()
    }

    private fun getChildNavController(): androidx.navigation.NavController? {
        val host = childFragmentManager.findFragmentById(R.id.room_section_nav_host)
        return if (host is androidx.navigation.fragment.NavHostFragment) {
            host.navController
        } else {
            null
        }
    }
}
