package com.kappa.app.audio.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.kappa.app.audio.domain.usecase.GetAudioRoomsUseCase
import com.kappa.app.audio.domain.usecase.JoinAudioRoomUseCase
import com.kappa.app.audio.domain.usecase.LeaveAudioRoomUseCase
import com.kappa.app.core.base.ViewState
import com.kappa.app.domain.audio.AudioRoom
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
    val isLoading: Boolean = false,
    val isJoining: Boolean = false,
    val error: String? = null
) : ViewState

/**
 * Audio ViewModel.
 */
@HiltViewModel
class AudioViewModel @Inject constructor(
    private val getAudioRoomsUseCase: GetAudioRoomsUseCase,
    private val joinAudioRoomUseCase: JoinAudioRoomUseCase,
    private val leaveAudioRoomUseCase: LeaveAudioRoomUseCase
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
                token = null
            )
        }
    }
}
