package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import javax.inject.Inject

class UnlockSeatUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String, seat: Int): Result<Unit> {
        return audioRepository.unlockSeat(roomId, seat)
    }
}
