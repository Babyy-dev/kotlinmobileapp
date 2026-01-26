package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.audio.domain.repository.JoinRoomInfo
import javax.inject.Inject

class JoinAudioRoomUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String, password: String? = null): Result<JoinRoomInfo> {
        return audioRepository.joinRoom(roomId, password)
    }
}
