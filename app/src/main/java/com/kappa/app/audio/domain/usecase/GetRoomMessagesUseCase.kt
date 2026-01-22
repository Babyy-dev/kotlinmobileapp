package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.RoomMessage
import javax.inject.Inject

class GetRoomMessagesUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String): Result<List<RoomMessage>> {
        return audioRepository.getRoomMessages(roomId)
    }
}
