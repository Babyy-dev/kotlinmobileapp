package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import javax.inject.Inject

class CloseRoomUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String): Result<Unit> {
        return audioRepository.closeRoom(roomId)
    }
}
