package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.RoomSeat
import javax.inject.Inject

class GetRoomSeatsUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String): Result<List<RoomSeat>> {
        return audioRepository.getRoomSeats(roomId)
    }
}
