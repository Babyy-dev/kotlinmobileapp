package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.AudioRoom
import javax.inject.Inject

/**
 * UseCase for getting audio rooms.
 */
class GetAudioRoomsUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(): Result<List<AudioRoom>> {
        return audioRepository.getAudioRooms()
    }
}
