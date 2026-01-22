package com.kappa.app.audio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.audio.domain.usecase.GetAudioRoomsUseCase
import com.kappa.app.audio.domain.usecase.GetRoomGiftsUseCase
import com.kappa.app.audio.domain.usecase.GetRoomMessagesUseCase
import com.kappa.app.audio.domain.usecase.JoinAudioRoomUseCase
import com.kappa.app.audio.domain.usecase.LeaveAudioRoomUseCase
import com.kappa.app.audio.domain.usecase.SendGiftUseCase
import com.kappa.app.audio.domain.usecase.SendRoomMessageUseCase
import com.kappa.app.core.base.ViewState
import com.kappa.app.domain.audio.AudioRoom
import com.kappa.app.domain.audio.GiftLog
import com.kappa.app.domain.audio.RoomMessage
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
    val messages: List<RoomMessage> = emptyList(),
    val gifts: List<GiftLog> = emptyList(),
    val isLoading: Boolean = false,
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
    private val joinAudioRoomUseCase: JoinAudioRoomUseCase,
    private val leaveAudioRoomUseCase: LeaveAudioRoomUseCase,
    private val getRoomMessagesUseCase: GetRoomMessagesUseCase,
    private val sendRoomMessageUseCase: SendRoomMessageUseCase,
    private val getRoomGiftsUseCase: GetRoomGiftsUseCase,
    private val sendGiftUseCase: SendGiftUseCase
) : ViewModel() {
    
    private val _viewState = MutableStateFlow(AudioViewState())
    val viewState: StateFlow<AudioViewState> = _viewState.asStateFlow()
    
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

    fun joinRoom(roomId: String) {
        viewModelScope.launch {
            _viewState.value = _viewState.value.copy(isJoining = true, error = null)
            joinAudioRoomUseCase(roomId)
                .onSuccess { info ->
                    _viewState.value = _viewState.value.copy(
                        isJoining = false,
                        activeRoom = info.room,
                        livekitUrl = info.livekitUrl,
                        token = info.token
                    )
                    loadRoomMessages()
                    loadRoomGifts()
                }
                .onFailure { throwable ->
                    _viewState.value = _viewState.value.copy(
                        isJoining = false,
                        error = throwable.message
                    )
                }
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
                messages = emptyList(),
                gifts = emptyList()
            )
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
