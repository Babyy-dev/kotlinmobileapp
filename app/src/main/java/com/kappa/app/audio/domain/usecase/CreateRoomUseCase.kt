package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.AudioRoom
import javax.inject.Inject

class CreateRoomUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(name: String, password: String? = null): Result<AudioRoom> {
        return audioRepository.createRoom(name, password)
    }
}
