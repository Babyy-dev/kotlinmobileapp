package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import javax.inject.Inject

class KickParticipantUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String, userId: String): Result<Unit> {
        return audioRepository.kickParticipant(roomId, userId)
    }
}
