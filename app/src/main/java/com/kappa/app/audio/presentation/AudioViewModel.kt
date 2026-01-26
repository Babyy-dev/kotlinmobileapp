package com.kappa.app.audio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.audio.domain.usecase.GetAudioRoomsUseCase
import com.kappa.app.audio.domain.usecase.GetRoomSeatsUseCase
import com.kappa.app.audio.domain.usecase.GetRoomGiftsUseCase
import com.kappa.app.audio.domain.usecase.GetRoomMessagesUseCase
import com.kappa.app.audio.domain.usecase.JoinAudioRoomUseCase
import com.kappa.app.audio.domain.usecase.LeaveAudioRoomUseCase
import com.kappa.app.audio.domain.usecase.CreateRoomUseCase
import com.kappa.app.audio.domain.usecase.CloseRoomUseCase
import com.kappa.app.audio.domain.usecase.TakeSeatUseCase
import com.kappa.app.audio.domain.usecase.LeaveSeatUseCase
import com.kappa.app.audio.domain.usecase.LockSeatUseCase
import com.kappa.app.audio.domain.usecase.UnlockSeatUseCase
import com.kappa.app.audio.domain.usecase.MuteParticipantUseCase
import com.kappa.app.audio.domain.usecase.KickParticipantUseCase
import com.kappa.app.audio.domain.usecase.BanParticipantUseCase
import com.kappa.app.audio.domain.usecase.SendGiftUseCase
import com.kappa.app.audio.domain.usecase.SendRoomMessageUseCase
import com.kappa.app.core.base.ViewState
import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomMessage
import com.kappa.app.domain.audio.RoomSeat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Audio ViewState.
 */
data class AudioViewState(
    val rooms: List<AudioRoom> = emptyList(),
    val activeRoom: AudioRoom? = null,
    val livekitUrl: String? = null,
    val token: String? = null,
    val seats: List<RoomSeat> = emptyList(),
    val messages: List<RoomMessage> = emptyList(),
    val gifts: List<GiftLog> = emptyList(),
    val isLoading: Boolean = false,
    val isSeatLoading: Boolean = false,
    val isRoomActionLoading: Boolean = false,
    val isJoining: Boolean = false,
    val isSendingMessage: Boolean = false,
    val isSendingGift: Boolean = false,
    val error: String? = null
) : ViewState

/**
 * Audio ViewModel.
 */
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val getAudioRoomsUseCase: GetAudioRoomsUseCase,
    private val createRoomUseCase: CreateRoomUseCase,
    private val joinAudioRoomUseCase: JoinAudioRoomUseCase,
    private val leaveAudioRoomUseCase: LeaveAudioRoomUseCase,
    private val closeRoomUseCase: CloseRoomUseCase,
    private val getRoomSeatsUseCase: GetRoomSeatsUseCase,
    private val takeSeatUseCase: TakeSeatUseCase,
    private val leaveSeatUseCase: LeaveSeatUseCase,
    private val lockSeatUseCase: LockSeatUseCase,
    private val unlockSeatUseCase: UnlockSeatUseCase,
    private val muteParticipantUseCase: MuteParticipantUseCase,
    private val kickParticipantUseCase: KickParticipantUseCase,
    private val banParticipantUseCase: BanParticipantUseCase,
    private val getRoomMessagesUseCase: GetRoomMessagesUseCase,
    private val sendRoomMessageUseCase: SendRoomMessageUseCase,
    private val getRoomGiftsUseCase: GetRoomGiftsUseCase,
    private val sendGiftUseCase: SendGiftUseCase
) : ViewModel() {
    
    private val _viewState = MutableStateFlow(AudioViewState())
    val viewState: StateFlow<AudioViewState> = _viewState.asStateFlow()
    private var lastJoinPassword: String? = null
    
    fun loadAudioRooms() {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isLoading = true, error = null)
            
            getAudioRoomsUseCase()
                .onSuccess { rooms ->
                    _viewState.value = _viewState.value.copy(
                        isLoading = false,
                        rooms = rooms
                    )
                }
                .onFailure { throwable ->
                    val message = throwable.message ?: "Unexpected error"
                    val shouldClearRooms = message.contains("No internet", ignoreCase = true) ||
                        message.contains("timeout", ignoreCase = true)
                    val current = _viewState.value
                    _viewState.value = if (shouldClearRooms) {
                        current.copy(
                            isLoading = false,
                            rooms = emptyList(),
                            error = message
                        )
                    } else {
                        current.copy(
                            isLoading = false,
                            error = message
                        )
                    }
                }
        }
    }

    fun createRoom(name: String, password: String?) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            createRoomUseCase(name, password)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    loadAudioRooms()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun joinRoom(roomId: String, password: String? = null) {
        viewModelScope.launch {
            lastJoinPassword = password
            _viewState.value = _viewState.value.copy(isJoining = true, error = null)
            joinAudioRoomUseCase(roomId, password)
                .onSuccess { info ->
                    _viewState.value = _viewState.value.copy(
                        isJoining = false,
                        activeRoom = info.room,
                        livekitUrl = info.livekitUrl,
                        token = info.token
                    )
                    loadRoomMessages()
                    loadRoomGifts()
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isJoining = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun reconnectRoom() {
        val roomId = _viewState.value.activeRoom?.id ?: return
        if (_viewState.value.isJoining) {
            return
        }
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isJoining = true, error = null)
            joinAudioRoomUseCase(roomId, lastJoinPassword)
                .onSuccess { info ->
                    _viewState.value = _viewState.value.copy(
                        isJoining = false,
                        activeRoom = info.room,
                        livekitUrl = info.livekitUrl,
                        token = info.token
                    )
                    loadRoomMessages()
                    loadRoomGifts()
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isJoining = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun clearError() {
        if (_viewState.value.error != null) {
            _viewState.value = _viewState.value.copy(error = null)
        }
    }

    fun leaveRoom() {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            leaveAudioRoomUseCase(roomId)
            _viewState.value = _viewState.value.copy(
                activeRoom = null,
                livekitUrl = null,
                token = null,
                seats = emptyList(),
                messages = emptyList(),
                gifts = emptyList()
            )
        }
    }

    fun closeRoom() {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            closeRoomUseCase(roomId)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    leaveRoom()
                    loadAudioRooms()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun loadRoomSeats() {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isSeatLoading = true, error = null)
            getRoomSeatsUseCase(roomId)
                .onSuccess { seats ->
                    _viewState.value = _viewState.value.copy(
                        isSeatLoading = false,
                        seats = seats
                    )
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isSeatLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun takeSeat(seatNumber: Int) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            takeSeatUseCase(roomId, seatNumber)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun leaveSeat(seatNumber: Int) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            leaveSeatUseCase(roomId, seatNumber)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun lockSeat(seatNumber: Int) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            lockSeatUseCase(roomId, seatNumber)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun unlockSeat(seatNumber: Int) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            unlockSeatUseCase(roomId, seatNumber)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun muteParticipant(userId: String, muted: Boolean) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            muteParticipantUseCase(roomId, userId, muted)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun kickParticipant(userId: String) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            kickParticipantUseCase(roomId, userId)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun banParticipant(userId: String) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isRoomActionLoading = true, error = null)
            banParticipantUseCase(roomId, userId)
                .onSuccess {
                    _viewState.value = _viewState.value.copy(isRoomActionLoading = false)
                    loadRoomSeats()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isRoomActionLoading = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun loadRoomMessages() {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            getRoomMessagesUseCase(roomId)
                .onSuccess { messages ->
                    _viewState.value = _viewState.value.copy(messages = messages)
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(error = throwable.message)
                }
        }
    }

    fun sendMessage(message: String) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        val trimmed = message.trim()
        if (trimmed.isBlank()) {
            _viewState.value = _viewState.value.copy(error = "Message cannot be empty")
            return
        }
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isSendingMessage = true, error = null)
            sendRoomMessageUseCase(roomId, trimmed)
                .onSuccess { newMessage ->
                    val updated = _viewState.value.messages + newMessage
                    _viewState.value = _viewState.value.copy(
                        isSendingMessage = false,
                        messages = updated
                    )
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isSendingMessage = false,
                        error = throwable.message
                    )
                }
        }
    }

    fun loadRoomGifts() {
        val roomId = _viewState.value.activeRoom?.id ?: return
        viewModelScope.launch {
            getRoomGiftsUseCase(roomId)
                .onSuccess { gifts ->
                    _viewState.value = _viewState.value.copy(gifts = gifts)
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(error = throwable.message)
                }
        }
    }

    fun sendGift(amount: Long, recipientId: String?) {
        val roomId = _viewState.value.activeRoom?.id ?: return
        if (amount <= 0) {
            _viewState.value = _viewState.value.copy(error = "Gift amount must be greater than 0")
            return
        }
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isSendingGift = true, error = null)
            sendGiftUseCase(roomId, amount, recipientId)
                .onSuccess { gift ->
                    val updated = _viewState.value.gifts + gift
                    _viewState.value = _viewState.value.copy(
                        isSendingGift = false,
                        gifts = updated
                    )
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isSendingGift = false,
                        error = throwable.message
                    )
                }
        }
    }
}
