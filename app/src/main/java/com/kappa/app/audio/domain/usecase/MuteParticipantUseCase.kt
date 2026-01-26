package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import javax.inject.Inject

class MuteParticipantUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String, userId: String, muted: Boolean): Result<Unit> {
        return audioRepository.muteParticipant(roomId, userId, muted)
    }
}
