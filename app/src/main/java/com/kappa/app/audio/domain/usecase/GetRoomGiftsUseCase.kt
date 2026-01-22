package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.GiftLog
import javax.inject.Inject

class GetRoomGiftsUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String): Result<List<GiftLog>> {
        return audioRepository.getRoomGifts(roomId)
    }
}
