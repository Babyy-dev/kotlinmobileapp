package com.kappa.app.audio.domain.usecase

import com.kappa.app.audio.domain.repository.AudioRepository
import com.kappa.app.domain.audio.AudioRoom
import javax.inject.Inject

class ToggleRoomFavoriteUseCase @Inject constructor(
    private val audioRepository: AudioRepository
) {
    suspend operator fun invoke(roomId: String, favorite: Boolean): Result<AudioRoom> {
        return audioRepository.toggleRoomFavorite(roomId, favorite)
    }
}
