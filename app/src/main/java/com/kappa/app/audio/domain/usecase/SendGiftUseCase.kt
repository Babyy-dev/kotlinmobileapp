package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.GiftLog
import javax.inject.Inject

class SendGiftUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String, amount: Long, recipientId: String?): Result<GiftLog> {
        return audioRepository.sendGift(roomId, amount, recipientId)
    }
}
