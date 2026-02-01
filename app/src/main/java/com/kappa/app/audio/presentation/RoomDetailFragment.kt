package com.kappa.app.audio.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.kappa.app.R
import com.kappa.app.core.network.NetworkMonitor
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomSeat
import com.kappa.app.domain.audio.SeatStatus
import com.kappa.app.gift.presentation.GiftCatalog
import com.google.gson.Gson
import java.io.IOException
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
    private val messagesAdapter = RoomMessagesAdapter()
    private val seatsAdapter = RoomSeatsAdapter()
    private lateinit var giftAdapter: RoomGiftAdapter
    private var isOnline: Boolean = true
    private var isConnecting: Boolean = false
    private var reconnectJob: Job? = null
    private var lastErrorMessage: String? = null
    private var enableEchoCancellation: Boolean = true
    private var enableNoiseSuppression: Boolean = true
    private var selectedGift: GiftItem? = null
    private var selectedCategory: GiftCategory = GiftCategory.MULTIPLIER
    private var giftQuantity: Int = 1

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
        val errorText = view.findViewById<TextView>(R.id.text_room_error)
        val leaveButton = view.findViewById<MaterialButton>(R.id.button_leave_room)
        val messageList = view.findViewById<RecyclerView>(R.id.recycler_room_messages)
        val messageInput = view.findViewById<EditText>(R.id.input_message)
        val sendMessageButton = view.findViewById<MaterialButton>(R.id.button_send_message)
        val giftLogText = view.findViewById<TextView>(R.id.text_gift_log)
        val giftRecipientInput = view.findViewById<EditText>(R.id.input_gift_recipient)
        val sendGiftButton = view.findViewById<MaterialButton>(R.id.button_send_gift)
        val giftList = view.findViewById<RecyclerView>(R.id.recycler_gifts)
        val categoryCommon = view.findViewById<MaterialButton>(R.id.button_gift_category_common)
        val categoryParty = view.findViewById<MaterialButton>(R.id.button_gift_category_party)
        val categoryVip = view.findViewById<MaterialButton>(R.id.button_gift_category_vip)
        val selectedGiftText = view.findViewById<TextView>(R.id.text_selected_gift)
        val selectedGiftPrice = view.findViewById<TextView>(R.id.text_selected_gift_price)
        val quantityMinus = view.findViewById<MaterialButton>(R.id.button_gift_quantity_minus)
        val quantityPlus = view.findViewById<MaterialButton>(R.id.button_gift_quantity_plus)
        val quantityText = view.findViewById<TextView>(R.id.text_gift_quantity)
        val refreshButton = view.findViewById<MaterialButton>(R.id.button_refresh_room)
        val seatsList = view.findViewById<RecyclerView>(R.id.recycler_room_seats)
        val refreshSeatsButton = view.findViewById<MaterialButton>(R.id.button_refresh_seats)
        val seatNumberInput = view.findViewById<EditText>(R.id.input_seat_number)
        val takeSeatButton = view.findViewById<MaterialButton>(R.id.button_take_seat)
        val leaveSeatButton = view.findViewById<MaterialButton>(R.id.button_leave_seat)
        val lockSeatButton = view.findViewById<MaterialButton>(R.id.button_lock_seat)
        val unlockSeatButton = view.findViewById<MaterialButton>(R.id.button_unlock_seat)
        val targetUserInput = view.findViewById<EditText>(R.id.input_target_user)
        val muteUserButton = view.findViewById<MaterialButton>(R.id.button_mute_user)
        val unmuteUserButton = view.findViewById<MaterialButton>(R.id.button_unmute_user)
        val kickUserButton = view.findViewById<MaterialButton>(R.id.button_kick_user)
        val banUserButton = view.findViewById<MaterialButton>(R.id.button_ban_user)
        val closeRoomButton = view.findViewById<MaterialButton>(R.id.button_close_room)
        val echoSwitch = view.findViewById<SwitchMaterial>(R.id.switch_echo_cancel)
        val noiseSwitch = view.findViewById<SwitchMaterial>(R.id.switch_noise_suppression)
        val applyAudioButton = view.findViewById<MaterialButton>(R.id.button_apply_audio_settings)

        messageList.layoutManager = LinearLayoutManager(requireContext())
        messageList.adapter = messagesAdapter
        seatsList.layoutManager = GridLayoutManager(requireContext(), 5)
        seatsList.adapter = seatsAdapter
        giftAdapter = RoomGiftAdapter { gift ->
            selectedGift = gift
            giftAdapter.setSelected(gift.id)
            updateGiftSummary()
        }
        giftList.layoutManager = GridLayoutManager(requireContext(), 4)
        giftList.adapter = giftAdapter

        val allGifts = loadGiftCatalogs()
        fun updateGiftList() {
            val filtered = allGifts.filter { it.category == selectedCategory }
            giftAdapter.submitList(filtered)
            if (selectedGift == null || selectedGift?.category != selectedCategory) {
                selectedGift = filtered.firstOrNull()
                giftAdapter.setSelected(selectedGift?.id)
            }
            updateGiftSummary(selectedGiftText, selectedGiftPrice, quantityText)
        }
        updateGiftList()

        fun setCategory(category: GiftCategory) {
            selectedCategory = category
            updateGiftList()
        }
        categoryCommon.text = "Multiplier"
        categoryParty.text = "Unique"
        categoryVip.visibility = View.GONE
        categoryCommon.setOnClickListener { setCategory(GiftCategory.MULTIPLIER) }
        categoryParty.setOnClickListener { setCategory(GiftCategory.UNIQUE) }

        quantityMinus.setOnClickListener {
            if (giftQuantity > 1) {
                giftQuantity -= 1
                updateGiftSummary(selectedGiftText, selectedGiftPrice, quantityText)
            }
        }
        quantityPlus.setOnClickListener {
            giftQuantity += 1
            updateGiftSummary(selectedGiftText, selectedGiftPrice, quantityText)
        }

        echoSwitch.isChecked = enableEchoCancellation
        noiseSwitch.isChecked = enableNoiseSuppression
        echoSwitch.setOnCheckedChangeListener { _, isChecked ->
            enableEchoCancellation = isChecked
        }
        noiseSwitch.setOnCheckedChangeListener { _, isChecked ->
            enableNoiseSuppression = isChecked
        }

        leaveButton.setOnClickListener {
            disconnectRoom()
            audioViewModel.leaveRoom()
            findNavController().popBackStack()
        }

        sendMessageButton.setOnClickListener {
            val text = messageInput.text?.toString().orEmpty()
            audioViewModel.sendMessage(text)
            messageInput.setText("")
        }

        sendGiftButton.setOnClickListener {
            val gift = selectedGift
            if (gift == null) {
                Toast.makeText(requireContext(), "Select a gift first", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val amount = gift.price * giftQuantity
            val recipient = giftRecipientInput.text?.toString()?.trim().orEmpty().ifBlank { null }
            if (gift.category == GiftCategory.UNIQUE && recipient == null) {
                Toast.makeText(requireContext(), "Unique gifts require a seated recipient", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.sendGift(amount, recipient)
            giftRecipientInput.setText("")
        }

        refreshButton.setOnClickListener {
            audioViewModel.loadRoomMessages()
            audioViewModel.loadRoomGifts()
        }

        refreshSeatsButton.setOnClickListener {
            audioViewModel.loadRoomSeats()
        }

        takeSeatButton.setOnClickListener {
            val seatNumber = seatNumberInput.text?.toString()?.trim()?.toIntOrNull()
            if (seatNumber == null) {
                Toast.makeText(requireContext(), "Enter a valid seat number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.takeSeat(seatNumber)
        }

        leaveSeatButton.setOnClickListener {
            val seatNumber = seatNumberInput.text?.toString()?.trim()?.toIntOrNull()
            if (seatNumber == null) {
                Toast.makeText(requireContext(), "Enter a valid seat number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.leaveSeat(seatNumber)
        }

        lockSeatButton.setOnClickListener {
            val seatNumber = seatNumberInput.text?.toString()?.trim()?.toIntOrNull()
            if (seatNumber == null) {
                Toast.makeText(requireContext(), "Enter a valid seat number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.lockSeat(seatNumber)
        }

        unlockSeatButton.setOnClickListener {
            val seatNumber = seatNumberInput.text?.toString()?.trim()?.toIntOrNull()
            if (seatNumber == null) {
                Toast.makeText(requireContext(), "Enter a valid seat number", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.unlockSeat(seatNumber)
        }

        muteUserButton.setOnClickListener {
            val userId = targetUserInput.text?.toString()?.trim().orEmpty()
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "Enter a user id", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.muteParticipant(userId, true)
        }

        unmuteUserButton.setOnClickListener {
            val userId = targetUserInput.text?.toString()?.trim().orEmpty()
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "Enter a user id", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.muteParticipant(userId, false)
        }

        kickUserButton.setOnClickListener {
            val userId = targetUserInput.text?.toString()?.trim().orEmpty()
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "Enter a user id", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.kickParticipant(userId)
        }

        banUserButton.setOnClickListener {
            val userId = targetUserInput.text?.toString()?.trim().orEmpty()
            if (userId.isBlank()) {
                Toast.makeText(requireContext(), "Enter a user id", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            audioViewModel.banParticipant(userId)
        }

        closeRoomButton.setOnClickListener {
            audioViewModel.closeRoom()
        }

        applyAudioButton.setOnClickListener {
            disconnectRoom()
            connectIfReady()
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
                    messagesAdapter.submitList(state.messages)
                    giftLogText.text = buildGiftLog(state.gifts)
                    seatsAdapter.submitList(padSeats(state.seats, 20))
                    val hasMessages = state.messages.isNotEmpty()
                    if (hasMessages) {
                        messageList.scrollToPosition(state.messages.size - 1)
                    }
                    sendMessageButton.isEnabled = !state.isSendingMessage
                    sendMessageButton.text = if (state.isSendingMessage) "Sending..." else "Send Message"
                    sendGiftButton.isEnabled = !state.isSendingGift
                    sendGiftButton.text = if (state.isSendingGift) "Sending..." else "Send Gift"
                    val roomBusy = state.isRoomActionLoading || state.isSeatLoading
                    refreshSeatsButton.isEnabled = !state.isSeatLoading
                    takeSeatButton.isEnabled = !roomBusy
                    leaveSeatButton.isEnabled = !roomBusy
                    lockSeatButton.isEnabled = !roomBusy
                    unlockSeatButton.isEnabled = !roomBusy
                    muteUserButton.isEnabled = !roomBusy
                    unmuteUserButton.isEnabled = !roomBusy
                    kickUserButton.isEnabled = !roomBusy
                    banUserButton.isEnabled = !roomBusy
                    closeRoomButton.isEnabled = !roomBusy
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
                liveKitRoom?.localParticipant?.setMicrophoneEnabled(true)
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

    private fun buildGiftLog(gifts: List<GiftLog>): String {
        if (gifts.isEmpty()) {
            return "No gifts yet"
        }
        return gifts.joinToString(separator = "\n") { gift ->
            val target = gift.recipientId?.take(8) ?: "room"
            "Gift ${gift.amount} coins to $target | Balance ${gift.senderBalance}"
        }
    }

    private fun loadGiftCatalogs(): List<GiftItem> {
        val multiplier = loadCatalogFromAsset("multiplier_gifts.json")
        val unique = loadCatalogFromAsset("unique_gifts.json")
        val items = mutableListOf<GiftItem>()
        items.addAll(mapGiftDefinitions(multiplier, GiftCategory.MULTIPLIER))
        items.addAll(mapGiftDefinitions(unique, GiftCategory.UNIQUE))
        return items
    }

    private fun loadCatalogFromAsset(fileName: String): GiftCatalog? {
        val context = context ?: return null
        return try {
            context.assets.open(fileName).bufferedReader().use { reader ->
                Gson().fromJson(reader.readText(), GiftCatalog::class.java)
            }
        } catch (_: IOException) {
            null
        }
    }

    private fun mapGiftDefinitions(
        catalog: GiftCatalog?,
        category: GiftCategory
    ): List<GiftItem> {
        if (catalog == null) return emptyList()
        return catalog.gifts.map { def ->
            GiftItem(
                id = "${catalog.type}_${def.name.lowercase().replace(" ", "_")}",
                name = def.name,
                price = def.value,
                conversionRate = catalog.conversion_rate,
                category = category
            )
        }
    }

    private fun updateGiftSummary(
        nameText: TextView? = view?.findViewById(R.id.text_selected_gift),
        priceText: TextView? = view?.findViewById(R.id.text_selected_gift_price),
        quantityText: TextView? = view?.findViewById(R.id.text_gift_quantity)
    ) {
        val gift = selectedGift
        if (gift == null) {
            nameText?.text = "Selecione um presente"
            priceText?.text = "0"
            quantityText?.text = giftQuantity.toString()
            return
        }
        nameText?.text = gift.name
        val totalCoins = gift.price * giftQuantity
        val diamonds = (totalCoins * gift.conversionRate).toLong()
        priceText?.text = "Coins $totalCoins | Diamonds +$diamonds"
        quantityText?.text = giftQuantity.toString()
    }

    private fun padSeats(seats: List<RoomSeat>, totalSeats: Int): List<RoomSeat> {
        if (seats.size >= totalSeats) {
            return seats
        }
        val existingNumbers = seats.map { it.seatNumber }.toSet()
        val padded = seats.toMutableList()
        for (seatNumber in 1..totalSeats) {
            if (!existingNumbers.contains(seatNumber)) {
                padded.add(RoomSeat(seatNumber = seatNumber, status = SeatStatus.FREE))
            }
        }
        return padded.sortedBy { it.seatNumber }
    }

    override fun onDestroyView() {
        audioViewModel.leaveRoom()
        disconnectRoom()
        cancelReconnect()
        super.onDestroyView()
    }
}
