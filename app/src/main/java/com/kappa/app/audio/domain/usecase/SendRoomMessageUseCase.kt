package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.RoomMessage
import javax.inject.Inject

class SendRoomMessageUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String, message: String): Result<RoomMessage> {
        return audioRepository.sendRoomMessage(roomId, message)
    }
}
